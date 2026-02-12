package hyun9.song_finder.service;

import hyun9.song_finder.domain.DumpSong;
import hyun9.song_finder.repository.DumpSongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DumpServiceImpl implements DumpService {

    private final DumpSongRepository dumpSongRepository;

    @Transactional
    public void dump(
            String userId,
            String channelId,
            String normalizedTitle
    ) {
        if (dumpSongRepository.existsByUserIdAndChannelIdAndNormalizedTitle(
                userId, channelId, normalizedTitle)) {
            return; // 이미 dump된 경우 무시
        }

        dumpSongRepository.save(
                new DumpSong(userId, channelId, normalizedTitle)
        );
    }

    @Transactional
    public void undo(
            String userId,
            String channelId,
            String normalizedTitle
    ) {
        dumpSongRepository.deleteByUserIdAndChannelIdAndNormalizedTitle(
                userId, channelId, normalizedTitle
        );
    }

    @Transactional(readOnly = true)
    public Set<String> getDumpedTitles(String userId, String channelId) {
        return dumpSongRepository
                .findByUserIdAndChannelId(userId, channelId)
                .stream()
                .map(DumpSong::getNormalizedTitle)
                .collect(Collectors.toSet());
    }
}

