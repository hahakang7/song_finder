package hyun9.song_finder.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Song {

    @Id @GeneratedValue
    private long id;

    private String title;

    @ManyToOne
    private Artist artist;

}
