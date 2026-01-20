package hyun9.song_finder.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ArtistController {

    // 1단계: 검색폼 렌더링
    @GetMapping("/artist")
    public String showArtistSearchPage() {
        return "artist_search";
    }

    // 2단계에서 사용할 검색결과 처리
    @GetMapping("/artist/search")
    public String handleArtistSearch(@RequestParam("artistName") String artistName, Model model) {
        model.addAttribute("artistName", artistName);
        // 2단계에서 이 이름으로 유튜브 검색 처리 예정
        return "artist_result";
    }
}
