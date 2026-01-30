package hyun9.song_finder.repository;

import hyun9.song_finder.domain.SubscribedArtist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscribedArtistRepository extends JpaRepository<SubscribedArtist, Long> {
    Optional<SubscribedArtist> findByUserIdAndChannelId(String userId, String channelId);
}
