package hyun9.song_finder.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table (name = "app_user")
@Setter
@Getter
public class User {

    @Id
    @GeneratedValue
    private long id;

    private String userName;

    private String password;


}
