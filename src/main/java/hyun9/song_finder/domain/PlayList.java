package hyun9.song_finder.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class PlayList {

    @Id @GeneratedValue
    private long id;

    private String name;
}
