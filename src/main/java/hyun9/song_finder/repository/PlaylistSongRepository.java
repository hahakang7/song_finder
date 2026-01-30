package hyun9.song_finder.repository;

import hyun9.song_finder.domain.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {
    void deleteByPlaylistId(String playlistId);
    List<PlaylistSong> findByPlaylistId(String playlistId);

}
