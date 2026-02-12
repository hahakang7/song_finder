package hyun9.song_finder.service;

import hyun9.song_finder.domain.*;
import hyun9.song_finder.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

        // 1. 구독 메타 저장 (기존 그대로)
        SubscribedPlaylist sub =
                subscribedPlaylistRepository
                        .findByUserIdAndPlaylistId(userId, playlistId)
                        .orElseGet(() ->
                                subscribedPlaylistRepository.save(
                                        new SubscribedPlaylist(
                                                userId,
                                                playlistId,
                                                playlistTitle
                                        )
                                )
                        );

        // 2. 플레이리스트 영상 조회
        Set<String> videoIds =
                youtubeService.getAllVideoIdsInPlaylist(accessToken, playlistId);

        List<Map<String, Object>> videosDetailed =
                youtubeService.getVideosByIdsWithDetails(
                        accessToken,
                        new ArrayList<>(videoIds)
                );

        // =========================
        // ✅ 여기부터 핵심 수정 부분
        // =========================

        // normalizedTitle -> thumbnailUrl
        Map<String, String> titleToThumb = new HashMap<>();

        for (Map<String, Object> v : videosDetailed) {
            Map<String, Object> snippet =
                    (Map<String, Object>) v.get("snippet");
            if (snippet == null) continue;

            String rawTitle = (String) snippet.get("title");
            String channelTitle = (String) snippet.get("channelTitle");

            String normalized =
                    youtubeService.normalizeSongTitle(rawTitle, channelTitle);
            if (normalized.isBlank()) continue;

            // 🔹 여기서 1번에서 만든 메서드 사용
            String thumbnailUrl =
                    youtubeService.extractDefaultThumbnailUrl(snippet);

            // 중복 title 방지 (이미 있으면 무시)
            titleToThumb.putIfAbsent(normalized, thumbnailUrl);
        }

        // 3. Replace 전략
        playlistSongRepository.deleteByUserIdAndPlaylistId(userId, playlistId);

        List<PlaylistSong> rows = new ArrayList<>(titleToThumb.size());
        for (Map.Entry<String, String> e : titleToThumb.entrySet()) {
            rows.add(new PlaylistSong(
                    userId,
                    playlistId,
                    e.getKey(),
                    e.getValue()
            ));
        }

        playlistSongRepository.saveAll(rows);

        // 4. 동기화 시각 갱신
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

        // 1. 아티스트 구독 메타 저장
        SubscribedArtist sub =
                subscribedArtistRepository
                        .findByUserIdAndChannelId(userId, channelId)
                        .orElseGet(() ->
                                subscribedArtistRepository.save(
                                        new SubscribedArtist(
                                                userId,
                                                channelId,
                                                artistName
                                        )
                                )
                        );

        // 2. 아티스트 곡 영상 수집
        // (지금은 uploads 기준, Topic 전략을 쓰고 있다면 거기서 받아온 리스트)
        String uploadsPlaylistId =
                youtubeService.getUploadsPlaylistId(channelId);

        if (uploadsPlaylistId == null) {
            return; // 방어 (정상 채널이 아닌 경우)
        }

        Set<String> videoIds =
                youtubeService.getAllVideoIdsInPlaylist(accessToken, uploadsPlaylistId);

        List<Map<String, Object>> videosDetailed =
                youtubeService.getVideosByIdsWithDetails(
                        accessToken,
                        new ArrayList<>(videoIds)
                );

        List<Map<String, Object>> artistSongs =
                youtubeService.filterLikelySongs(videosDetailed);

        // =========================
        // ✅ 여기부터 핵심 로직
        // =========================

        // normalizedTitle -> thumbnailUrl
        Map<String, String> titleToThumb = new HashMap<>();

        for (Map<String, Object> v : artistSongs) {
            Map<String, Object> snippet =
                    (Map<String, Object>) v.get("snippet");
            if (snippet == null) continue;

            String rawTitle = (String) snippet.get("title");
            if (rawTitle == null) continue;

            // ❗ artistName은 고정값 사용
            String normalized =
                    youtubeService.normalizeSongTitle(rawTitle, artistName);
            if (normalized.isBlank()) continue;

            String thumbnailUrl =
                    youtubeService.extractDefaultThumbnailUrl(snippet);

            // 동일 곡 중복 방지
            titleToThumb.putIfAbsent(normalized, thumbnailUrl);
        }

        // 3. Replace 전략
        artistSongRepository.deleteByChannelId(channelId);

        List<ArtistSong> rows = new ArrayList<>(titleToThumb.size());
        for (Map.Entry<String, String> e : titleToThumb.entrySet()) {
            rows.add(new ArtistSong(
                    channelId,          // ❗ 항상 channelId
                    e.getKey(),         // normalizedTitle
                    e.getValue()        // thumbnailUrl
            ));
        }

        artistSongRepository.saveAll(rows);

        // 4. 동기화 시각 갱신
        sub.markSynced();
        subscribedArtistRepository.save(sub);


    }

    @Override
    @Transactional
    public void unsubscribePlaylist(String userId, String playlistId) {

        // 구독 메타 삭제
        subscribedPlaylistRepository.deleteByUserIdAndPlaylistId(userId, playlistId);

        // 스냅샷 삭제
        playlistSongRepository.deleteByUserIdAndPlaylistId(userId, playlistId);
    }


}
