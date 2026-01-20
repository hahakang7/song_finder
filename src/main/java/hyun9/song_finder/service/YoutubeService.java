package hyun9.song_finder.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class YoutubeService {

    @Value("${youtube.api.key}")
    private String API_KEY;


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

    //검색기능은 추후 개발 잠정폐지
    /*
    public List<Map<String, Object>> searchChannelsByArtist(String artistName) {
        String url = "https://www.googleapis.com/youtube/v3/search"
                + "?part=snippet"
                + "&type=channel"
                + "&maxResults=10"
                + "&q=" + URLEncoder.encode(artistName, StandardCharsets.UTF_8)
                + "&key=" + API_KEY;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        return (List<Map<String, Object>>) response.getBody().get("items");
    }
     */

    public String extractChannelId(String inputUrl) {
        if (inputUrl == null) return null;
        String url = inputUrl.trim();
        if (url.isEmpty()) return null;

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            return null;
        }

        String path = uri.getPath(); // 예: "/@officialhigedandism"
        if (path == null) return null;

        // 1) /channel/UCxxxx
        if (path.startsWith("/channel/")) {
            String rest = path.substring("/channel/".length());
            return rest.split("[/?]")[0];
        }

        // 2) /@handle
        if (path.startsWith("/@")) {
            String handle = path.substring(2).split("[/?]")[0]; // "officialhigedandism"
            return resolveHandleToChannelId(handle); // ✅ forHandle 기반
        }

        return null;
    }


    public String resolveHandleToChannelId(String handle) {
        // handle: "@officialhigedandism" or "officialhigedandism"
        String forHandle = handle.startsWith("@") ? handle.substring(1) : handle;

        String url = "https://www.googleapis.com/youtube/v3/channels"
                + "?part=id"
                + "&forHandle=" + URLEncoder.encode(forHandle, StandardCharsets.UTF_8)
                + "&key=" + API_KEY;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
        if (items == null || items.isEmpty()) return null;

        return (String) items.get(0).get("id");
    }


    public String resolveQueryToChannelId(String query) {
        String url = "https://www.googleapis.com/youtube/v3/search"
                + "?part=snippet"
                + "&type=channel"
                + "&maxResults=1"
                + "&q=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&key=" + API_KEY;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
        if (items == null || items.isEmpty()) return null;

        Map<String, Object> idObj = (Map<String, Object>) items.get(0).get("id");
        return (String) idObj.get("channelId");
    }



    public Map<String, Object> fetchChannelInfo(String channelId) {
        String url = "https://www.googleapis.com/youtube/v3/channels"
                + "?part=snippet"
                + "&id=" + channelId
                + "&key=" + API_KEY;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");

        return (items != null && !items.isEmpty()) ? items.get(0) : null;
    }

    public String getUploadsPlaylistId(String channelId) {
        String url = "https://www.googleapis.com/youtube/v3/channels"
                + "?part=contentDetails"
                + "&id=" + channelId
                + "&key=" + API_KEY; // 공개 호출이므로 apiKey 사용

        RestTemplate rt = new RestTemplate();
        ResponseEntity<Map> res = rt.getForEntity(url, Map.class);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) res.getBody().get("items");
        if (items == null || items.isEmpty()) return null;

        Map<String, Object> contentDetails = (Map<String, Object>) items.get(0).get("contentDetails");
        Map<String, Object> relatedPlaylists = (Map<String, Object>) contentDetails.get("relatedPlaylists");
        return (String) relatedPlaylists.get("uploads");
    }

    public Set<String> getAllVideoIdsInPlaylist(String accessToken, String playlistId) {
        Set<String> ids = new HashSet<>();
        String pageToken = null;

        while (true) {
            String url = "https://www.googleapis.com/youtube/v3/playlistItems"
                    + "?part=snippet"
                    + "&playlistId=" + playlistId
                    + "&maxResults=50"
                    + (pageToken != null ? "&pageToken=" + pageToken : "");

            Map<String, Object> body = callYouTubeApiWithBearer(url, accessToken);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
            if (items != null) {
                for (Map<String, Object> item : items) {
                    Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                    if (snippet == null) continue;

                    Map<String, Object> resourceId = (Map<String, Object>) snippet.get("resourceId");
                    if (resourceId == null) continue;

                    String videoId = (String) resourceId.get("videoId");
                    if (videoId != null) ids.add(videoId);
                }
            }

            pageToken = (String) body.get("nextPageToken");
            if (pageToken == null) break;
        }

        return ids;
    }

    public List<Map<String, Object>> getVideosByIdsWithDetails(String accessToken, List<String> videoIds) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < videoIds.size(); i += 50) {
            List<String> chunk = videoIds.subList(i, Math.min(i + 50, videoIds.size()));
            String joined = String.join(",", chunk);

            String url = "https://www.googleapis.com/youtube/v3/videos"
                    + "?part=snippet,contentDetails"
                    + "&id=" + joined;

            Map<String, Object> body = callYouTubeApiWithBearer(url, accessToken);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
            if (items != null) result.addAll(items);
        }

        return result;
    }


    public List<Map<String, Object>> getVideosByIds(String accessToken, Set<String> videoIds) {
        List<String> all = new ArrayList<>(videoIds);
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < all.size(); i += 50) {
            List<String> chunk = all.subList(i, Math.min(i + 50, all.size()));
            String joined = String.join(",", chunk);

            String url = "https://www.googleapis.com/youtube/v3/videos"
                    + "?part=snippet"
                    + "&id=" + joined;

            Map<String, Object> body = callYouTubeApiWithBearer(url, accessToken);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
            if (items != null) result.addAll(items);
        }

        return result;
    }

    private Map<String, Object> callYouTubeApiWithBearer(String url, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        RestTemplate rt = new RestTemplate();
        ResponseEntity<Map> res = rt.exchange(url, HttpMethod.GET, entity, Map.class);
        return res.getBody();
    }

    private int parseIsoDurationToSeconds(String iso) {
        // 예: PT3M15S, PT45S, PT1H2M
        if (iso == null || !iso.startsWith("PT")) return -1;

        int hours = 0, minutes = 0, seconds = 0;
        String s = iso.substring(2);

        int hIdx = s.indexOf('H');
        if (hIdx >= 0) {
            hours = Integer.parseInt(s.substring(0, hIdx));
            s = s.substring(hIdx + 1);
        }

        int mIdx = s.indexOf('M');
        if (mIdx >= 0) {
            minutes = Integer.parseInt(s.substring(0, mIdx));
            s = s.substring(mIdx + 1);
        }

        int secIdx = s.indexOf('S');
        if (secIdx >= 0) {
            seconds = Integer.parseInt(s.substring(0, secIdx));
        }

        return hours * 3600 + minutes * 60 + seconds;
    }


    private static final List<String> EXCLUDE_KEYWORDS = List.of(
            "vlog", "브이로그", "behind", "비하인드", "making", "메이킹",
            "teaser", "티저", "reaction", "리액션",
            "interview", "인터뷰", "live", "라이브", "stage", "무대",
            "karaoke", "cover", "커버",
            "shorts", "#shorts", "highlight", "하이라이트", "clip", "클립"
    );

    private boolean looksLikeNonSongByTitle(String titleLower) {
        for (String k : EXCLUDE_KEYWORDS) {
            if (titleLower.contains(k)) return true;
        }
        return false;
    }

    private boolean looksLikeSongByTitle(String titleLower) {
        // 가산점 느낌 (있으면 통과 확률 높임)
        return titleLower.contains("official audio")
                || titleLower.contains("official")
                || titleLower.contains("audio")
                || titleLower.contains("mv")
                || titleLower.contains("m/v")
                || titleLower.contains("music video")
                || titleLower.contains("lyric")
                || titleLower.contains("lyrics")
                || titleLower.contains("가사");
    }

    /**
     * videos.list 결과(items)를 받아서 "곡 후보"만 남긴다.
     * - shorts(<=60s) 제거
     * - title exclude 키워드 제거
     * - (선택) song 키워드 없더라도 완전 배제하지는 않음(너무 보수적으로 하면 곡이 많이 누락됨)
     */
    public List<Map<String, Object>> filterLikelySongs(List<Map<String, Object>> videos) {
        List<Map<String, Object>> filtered = new ArrayList<>();

        for (Map<String, Object> v : videos) {
            Map<String, Object> snippet = (Map<String, Object>) v.get("snippet");
            Map<String, Object> contentDetails = (Map<String, Object>) v.get("contentDetails");
            if (snippet == null || contentDetails == null) continue;

            String title = (String) snippet.get("title");
            String durationIso = (String) contentDetails.get("duration");

            String titleLower = title == null ? "" : title.toLowerCase();
            int seconds = parseIsoDurationToSeconds(durationIso);

            // A) Shorts 제거(<=60s)
            if (seconds > 0 && seconds <= 60) continue;
            if (titleLower.contains("#shorts") || titleLower.contains("shorts")) {
                // duration이 못 잡힌 경우 대비
                if (seconds <= 0 || seconds <= 90) continue;
            }

            // B) 잡영상 키워드 제거
            if (looksLikeNonSongByTitle(titleLower)) continue;

            // C) 보수적으로 너무 자르지 않기 위해:
            //    - song키워드가 있으면 무조건 통과
            //    - song키워드가 없어도, 길이가 2~10분이면 일단 통과 (음원일 가능성 높음)
            boolean songHint = looksLikeSongByTitle(titleLower);
            if (songHint) {
                filtered.add(v);
                continue;
            }

            if (seconds >= 120 && seconds <= 600) { // 2~10분: 대다수 곡 길이
                filtered.add(v);
            }
        }

        return filtered;
    }




}
