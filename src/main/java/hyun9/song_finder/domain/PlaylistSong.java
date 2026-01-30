package hyun9.song_finder.domain;

import jakarta.persistence.*;

/**
 * 사용자의 플레이리스트 곡 스냅샷
 * */


@Entity
@Table(
        name = "playlist_song",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"playlist_id", "normalized_title"})
        }
)
public class PlaylistSong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "playlist_id", nullable = false, length = 100)
    private String playlistId;

    @Column(name = "normalized_title", nullable = false)
    private String normalizedTitle;

    protected PlaylistSong() {}

    public PlaylistSong(String playlistId, String normalizedTitle) {
        this.playlistId = playlistId;
        this.normalizedTitle = normalizedTitle;
    }
}

