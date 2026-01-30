package hyun9.song_finder.service;

import hyun9.song_finder.domain.*;
import hyun9.song_finder.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SubscriptionSyncServiceImpl implements SubscriptionSyncService {

    private final YoutubeService youtubeService;

    private final SubscribedPlaylistRepository subscribedPlaylistRepository;
    private final PlaylistSongRepository playlistSongRepository;

    private final SubscribedArtistRepository subscribedArtistRepository;
    private final ArtistSongRepository artistSongRepository;

    /**
     * 플레이리스트 구독 + 곡 스냅샷 동기화
     */
    @Override
    @Transactional
    public void subscribeAndSyncPlaylist(
            String userId,
            String accessToken,
            String playlistId,
            String playlistTitle
    ) {
        // 1) 구독 메타 upsert
        SubscribedPlaylist sub = subscribedPlaylistRepository
                .findByUserIdAndPlaylistId(userId, playlistId)
                .orElseGet(() -> subscribedPlaylistRepository.save(
                        new SubscribedPlaylist(userId, playlistId, playlistTitle)
                ));

        // 2) YouTube API로 플레이리스트 전체 곡 가져오기 (네 기존 로직 재사용)
        Set<String> videoIds = youtubeService.getAllVideoIdsInPlaylist(accessToken, playlistId);

        List<Map<String, Object>> videosDetailed = youtubeService.getVideosByIdsWithDetails(
                accessToken, new ArrayList<>(videoIds)
        );

        // 3) normalizedTitle Set 만들기 (중복 제거)
        Set<String> normalizedTitles = new HashSet<>();
        for (Map<String, Object> v : videosDetailed) {
            Map<String, Object> snippet = (Map<String, Object>) v.get("snippet");
            if (snippet == null) continue;

            String rawTitle = (String) snippet.get("title");
            String channelTitle = (String) snippet.get("channelTitle");

            String normalized = youtubeService.normalizeSongTitle(rawTitle, channelTitle);
            if (!normalized.isBlank()) normalizedTitles.add(normalized);
        }

        // 4) Replace 전략: 기존 스냅샷 삭제 후 재삽입
        playlistSongRepository.deleteByPlaylistId(playlistId);

        List<PlaylistSong> rows = new ArrayList<>(normalizedTitles.size());
        for (String t : normalizedTitles) {
            rows.add(new PlaylistSong(playlistId, t));
        }
        playlistSongRepository.saveAll(rows);

        // 5) lastSyncedAt 갱신
        sub.markSynced();
        subscribedPlaylistRepository.save(sub);
    }

    /**
     * 아티스트 구독 + 곡 스냅샷 동기화
     * - Topic 채널 우선
     * - 없으면 공식 uploads fallback
     */
    @Override
    @Transactional
    public void subscribeAndSyncArtist(
            String userId,
            String accessToken,
            String channelId,
            String artistName
    ) {
        // 1) 구독 메타 upsert
        SubscribedArtist sub = subscribedArtistRepository
                .findByUserIdAndChannelId(userId, channelId)
                .orElseGet(() -> subscribedArtistRepository.save(
                        new SubscribedArtist(userId, channelId, artistName)
                ));

        // 2) Topic 우선으로 곡 목록 수집 (네가 이미 만든 메서드 기반)
        List<Map<String, Object>> artistSongs = youtubeService.loadSongsFromTopicChannel(artistName, accessToken);

        // fallback: 공식 uploads
        if (artistSongs.isEmpty()) {
            String uploadsPlaylistId = youtubeService.getUploadsPlaylistId(channelId);
            if (uploadsPlaylistId != null) {
                Set<String> ids = youtubeService.getAllVideoIdsInPlaylist(accessToken, uploadsPlaylistId);
                List<Map<String, Object>> detailed = youtubeService.getVideosByIdsWithDetails(accessToken, new ArrayList<>(ids));
                artistSongs = youtubeService.filterLikelySongs(detailed);
            }
        }

        // 3) normalizedTitle Set 만들기 (중복 제거)
        Set<String> normalizedTitles = new HashSet<>();
        for (Map<String, Object> v : artistSongs) {
            Map<String, Object> snippet = (Map<String, Object>) v.get("snippet");
            if (snippet == null) continue;

            String rawTitle = (String) snippet.get("title");
            String channelTitle = (String) snippet.get("channelTitle");

            String normalized = youtubeService.normalizeSongTitle(rawTitle, channelTitle);
            if (!normalized.isBlank()) normalizedTitles.add(normalized);
        }

        // 4) Replace 전략: 기존 스냅샷 삭제 후 재삽입
        artistSongRepository.deleteByChannelId(channelId);

        List<ArtistSong> rows = new ArrayList<>(normalizedTitles.size());
        for (String t : normalizedTitles) {
            rows.add(new ArtistSong(channelId, t));
        }
        artistSongRepository.saveAll(rows);

        // 5) lastSyncedAt 갱신
        sub.markSynced();
        subscribedArtistRepository.save(sub);
    }
}
