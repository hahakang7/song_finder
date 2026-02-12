package hyun9.song_finder.repository;

import hyun9.song_finder.domain.SubscribedPlaylist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscribedPlaylistRepository extends JpaRepository<SubscribedPlaylist, Long> {
    Optional<SubscribedPlaylist> findByUserIdAndPlaylistId(String userId, String playlistId);
    boolean existsByUserIdAndPlaylistId(String userId, String playlistId);
    void deleteByUserIdAndPlaylistId(String userId, String playlistId);
    List<SubscribedPlaylist> findByUserId(String userId);

}
