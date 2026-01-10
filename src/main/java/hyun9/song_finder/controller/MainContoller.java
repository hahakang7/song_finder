package hyun9.song_finder.controller;

import hyun9.song_finder.dto.FollowedArtistDto;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

public class MainContoller {

    @GetMapping("/")
    public String showMain(Model model) {
        List<FollowedArtistDto> followed = List.of(
                new FollowedArtistDto("IU", true, "Love wins all"),
                new FollowedArtistDto("NewJeans", false, null),
                new FollowedArtistDto("태연", true, "To. X")
        );
        model.addAttribute("followedArtists", followed);
        return "main";
    }
    //expdwa

}
