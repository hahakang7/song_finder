package hyun9.song_finder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class DummyPlaylist {
    private String id;
    private String title;
    private String thumbnailUrl;
    private int trackCount;
    private LocalDateTime lastSyncedAt;
    private boolean subscribed;
    private List<DummyTrack> tracks;
}
