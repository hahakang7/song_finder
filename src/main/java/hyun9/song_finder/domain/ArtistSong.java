package hyun9.song_finder.domain;

import jakarta.persistence.*;
import lombok.Getter;

/**
 * 아티스트 곡 스냅샷
 *
 * 이 아티스트가 갖고있는 곡은 무엇인가?
 * **/

@Getter
@Entity
@Table(
        name = "artist_song",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"channel_id", "normalized_title"})
        }
)
public class ArtistSong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "channel_id", nullable = false, length = 100)
    private String channelId;

    @Column(name = "normalized_title", nullable = false)
    private String normalizedTitle;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl; // ✅ 추가

    protected ArtistSong() {}

    public ArtistSong(String channelId, String normalizedTitle, String thumbnailUrl) {
        this.channelId = channelId;
        this.normalizedTitle = normalizedTitle;
        this.thumbnailUrl = thumbnailUrl;
    }
}


