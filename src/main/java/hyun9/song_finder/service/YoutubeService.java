package hyun9.song_finder.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class YoutubeService {

    public Map<String, Object> getPaginatedPlaylists(String accessToken, String pageToken) {
        String url = "https://www.googleapis.com/youtube/v3/playlists"
                + "?part=snippet&mine=true&maxResults=5";

        if (pageToken != null && !pageToken.isEmpty()) {
            url += "&pageToken=" + pageToken;
        }

        // 원본 응답
        Map<String, Object> response = callYouTubeApi(url, accessToken);

        // 가공해서 필요한 값만 꺼내서 정리
        Map<String, Object> result = new HashMap<>();
        result.put("playlists", response.get("items"));  // <-- 이걸 추가해야 Thymeleaf에서 playlists 쓸 수 있음
        result.put("nextPageToken", response.get("nextPageToken"));
        result.put("prevPageToken", pageToken);

        return result;
    }


    public Map<String, Object> getPlaylistItems(String accessToken, String playlistId, String pageToken) {
        String url = "https://www.googleapis.com/youtube/v3/playlistItems"
                + "?part=snippet&playlistId=" + playlistId + "&maxResults=10";

        if (pageToken != null && !pageToken.isEmpty()) {
            url += "&pageToken=" + pageToken;
        }

        return callYouTubeApi(url, accessToken);
    }

    private Map<String, Object> callYouTubeApi(String url, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        System.out.println("API 응답 전체: " + response.getBody());

        return response.getBody();
    }
}
