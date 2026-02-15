package hyun9.song_finder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DummyTrack {
    private String id;
    private String title;
    private String artistName;
    private int durationSec;
}
