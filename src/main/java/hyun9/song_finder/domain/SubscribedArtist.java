package hyun9.song_finder.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자가 어떤 아티스트를 구독했는지
 *
 * 이 사용자는 어떤 아티스트를 구독했는가?
 * 이 아티스트의 곡 목록은 언제 마지막으로 갱신했는가?
 * **/

@Entity
@Getter
@Table(
        name = "subscribed_artist",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "channel_id"})
        }
)
public class SubscribedArtist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "channel_id", nullable = false, length = 100)
    private String channelId;

    @Column(name = "artist_name", nullable = false)
    private String artistName;

    @Column(name = "last_synced_at", nullable = false)
    private LocalDateTime lastSyncedAt;

    protected SubscribedArtist() {}

    public SubscribedArtist(String userId, String channelId, String artistName) {
        this.userId = userId;
        this.channelId = channelId;
        this.artistName = artistName;
        this.lastSyncedAt = LocalDateTime.now();
    }

    public void markSynced() {
        this.lastSyncedAt = LocalDateTime.now();
    }

}
