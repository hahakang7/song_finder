package hyun9.song_finder;

import hyun9.song_finder.service.YoutubeService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RequiredArgsConstructor
@SpringBootTest
public class SongTitleNormalizerTest {

    private final SongTitleNormalizer normalizer = new SongTitleNormalizer();

    @Test
    @DisplayName("일본어 곡 제목 + 가수명 패턴에서 가수명이 남지 않아야 한다")
    void japaneseTitle_shouldRemoveTrailingArtistName() {
        // given
        String rawTitle = "ずっとラブソング / Vaundy：MUSIC VIDEO";
        String artistName = "Vaundy";

        // when
        String normalized = normalizer.normalizeSongTitle(rawTitle, artistName);

        // then
        assertEquals("ずっとラブソング", normalized);
    }

    public class SongTitleNormalizer{

        //곡 제목을 정제하는 함수
        public String normalizeSongTitle(String rawTitle, String artistName) {
            if (rawTitle == null) return "";

            String t = rawTitle.toLowerCase();

            // 1. 괄호 제거
            t = t.replaceAll("\\(.*?\\)", "");
            t = t.replaceAll("\\[.*?\\]", "");

            // 2. 고정 키워드 제거
            String[] removeKeywords = {
                    "official", "mv", "m/v", "music video",
                    "audio", "ver.", "version", "live"
            };
            for (String k : removeKeywords) {
                t = t.replace(k, "");
            }

            // 3. 아티스트명 제거
            if (artistName != null) {
                String a = Pattern.quote(artistName.toLowerCase());

                // 앞: "artist - title"
                t = t.replaceAll("^\\s*" + a + "\\s*[-|:/：]\\s*", "");

                // 뒤: "title - artist"
                t = t.replaceAll("\\s*[-|:/：]\\s*" + a + "\\s*$", "");
            }


            // 4. 특수문자 제거
            t = t.replaceAll("[^\\p{L}\\p{N} ]", " ");

            // 5. 공백 정리
            t = t.replaceAll("\\s+", " ").trim();

            // 안전장치
            if (artistName != null && !t.isBlank()) {
                String a = artistName.toLowerCase();

                List<String> tokens = new ArrayList<>(Arrays.asList(t.split(" ")));

                // 뒤에서부터 가수명 토큰 제거
                while (!tokens.isEmpty() && tokens.get(tokens.size() - 1).equals(a)) {
                    tokens.remove(tokens.size() - 1);
                }

                t = String.join(" ", tokens).trim();
            }



            return t;
        }

}


}
