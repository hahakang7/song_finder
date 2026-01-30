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
@Transactional(readOnly = true)
public class DumpServiceImpl implements DumpService {

    private final DumpSongRepository dumpSongRepository;

    @Override
    public Set<String> getDumpedTitles(String userId, String channelId) {
        return dumpSongRepository
                .findByUserIdAndChannelId(userId, channelId)
                .stream()
                .map(DumpSong::getNormalizedTitle)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void dumpSong(String userId, String channelId, String normalizedTitle) {

        // 이미 dump 되어 있으면 아무 것도 하지 않는다 (idempotent)
        boolean exists =
                dumpSongRepository
                        .findByUserIdAndChannelIdAndNormalizedTitle(
                                userId, channelId, normalizedTitle)
                        .isPresent();

        if (exists) {
            return;
        }

        DumpSong dumpSong =
                new DumpSong(userId, channelId, normalizedTitle);

        dumpSongRepository.save(dumpSong);
    }
}
