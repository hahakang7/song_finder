package hyun9.song_finder.repository;

import hyun9.song_finder.domain.DumpSong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DumpSongRepository extends JpaRepository<DumpSong, Long> {
    List<DumpSong> findByUserIdAndChannelId(String userId, String channelId);

    Optional<DumpSong> findByUserIdAndChannelIdAndNormalizedTitle(
            String userId,
            String channelId,
            String normalizedTitle
    );
}
