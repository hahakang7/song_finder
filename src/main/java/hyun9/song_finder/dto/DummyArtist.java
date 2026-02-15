package hyun9.song_finder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DummyArtist {
    private String id;
    private String name;
    private String channelUrl;
    private String avatarUrl;
    private LocalDateTime lastSyncedAt;
    private LocalDateTime subscribedAt;
}
