package hyun9.song_finder.repository;

import hyun9.song_finder.domain.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {

    List<PlaylistSong> findByUserIdAndPlaylistId(String userId, String playlistId);

    void deleteByUserIdAndPlaylistId(String userId, String playlistId);

    boolean existsByUserIdAndPlaylistId(String userId, String playlistId);
}

