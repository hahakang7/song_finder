package hyun9.song_finder.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "dump_song",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"user_id", "channel_id", "normalized_title"}
                )
        }
)
public class DumpSong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "channel_id", nullable = false, length = 100)
    private String channelId;

    @Column(name = "normalized_title", nullable = false, length = 255)
    private String normalizedTitle;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected DumpSong() {
        // JPA 기본 생성자
    }

    public DumpSong(String userId, String channelId, String normalizedTitle) {
        this.userId = userId;
        this.channelId = channelId;
        this.normalizedTitle = normalizedTitle;
    }

}
