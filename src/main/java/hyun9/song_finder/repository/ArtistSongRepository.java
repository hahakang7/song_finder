package hyun9.song_finder.repository;

import hyun9.song_finder.domain.ArtistSong;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistSongRepository extends JpaRepository<ArtistSong, Long> {
    void deleteByChannelId(String channelId);
}
