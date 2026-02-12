package hyun9.song_finder.repository;

import hyun9.song_finder.domain.ArtistSong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArtistSongRepository extends JpaRepository<ArtistSong, Long> {
    void deleteByChannelIdAndUserId(String channelId, String userId);
    List<ArtistSong> findByChannelId(String channelId);

}
