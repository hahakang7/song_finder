package hyun9.song_finder.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 사용자의 어떤 플레이리스트를 구독했는지
 *
 * 이 사용자는 어떤 플레이리스트를 구독했는가?
 * 이 플레이리스트는 마지막으로 언제 동기화했는가
 * */

@Entity
@Table(
        name = "subscribed_playlist",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "playlist_id"})
        }
)
public class SubscribedPlaylist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "playlist_id", nullable = false, length = 100)
    private String playlistId;

    @Column(name = "playlist_title", nullable = false)
    private String playlistTitle;

    @Column(name = "last_synced_at", nullable = false)
    private LocalDateTime lastSyncedAt;

    protected SubscribedPlaylist() {}

    public SubscribedPlaylist(String userId, String playlistId, String playlistTitle) {
        this.userId = userId;
        this.playlistId = playlistId;
        this.playlistTitle = playlistTitle;
        this.lastSyncedAt = LocalDateTime.now();
    }

    public void markSynced() {
        this.lastSyncedAt = LocalDateTime.now();
    }
}

