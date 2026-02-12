package hyun9.song_finder.domain;

import jakarta.persistence.*;
import lombok.Getter;

/**
 * 사용자의 플레이리스트 곡 스냅샷
 * */

@Getter
@Entity
@Table(
        name = "playlist_song",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "playlist_id", "normalized_title"})
        }
)
public class PlaylistSong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "playlist_id", nullable = false)
    private String playlistId;

    @Column(name = "normalized_title", nullable = false)
    private String normalizedTitle;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    protected PlaylistSong() {}

    public PlaylistSong(String userId, String playlistId, String normalizedTitle, String thumbnailUrl) {
        this.userId = userId;
        this.playlistId = playlistId;
        this.normalizedTitle = normalizedTitle;
        this.thumbnailUrl = thumbnailUrl;
    }
}



