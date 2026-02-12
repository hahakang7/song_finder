package hyun9.song_finder.repository;

import hyun9.song_finder.domain.SubscribedArtist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscribedArtistRepository extends JpaRepository<SubscribedArtist, Long> {
    Optional<SubscribedArtist> findByUserIdAndChannelId(String userId, String channelId);
    boolean existsByUserIdAndChannelId(String userId, String channelId);
    void deleteByUserIdAndChannelId(String userId, String playlistId);

    List<SubscribedArtist> findByUserId(String userId);
}
