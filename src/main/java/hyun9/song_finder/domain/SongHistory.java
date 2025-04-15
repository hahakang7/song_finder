package hyun9.song_finder.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class SongHistory {

    @Id
    @GeneratedValue
    private long id;
}
