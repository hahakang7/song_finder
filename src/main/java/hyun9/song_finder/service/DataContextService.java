package hyun9.song_finder.service;

import hyun9.song_finder.dto.DummyArtist;
import hyun9.song_finder.dto.DummyPlaylist;
import hyun9.song_finder.dto.DummyTrack;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DataContextService {

    private static final String ARTISTS_KEY = "dummyArtists";
    private static final String PLAYLISTS_KEY = "dummyPlaylists";

    public List<DummyArtist> getArtists(HttpSession session) {
        ensureData(session);
        return castArtists(session.getAttribute(ARTISTS_KEY));
    }

    public List<DummyPlaylist> getPlaylists(HttpSession session) {
        ensureData(session);
        return castPlaylists(session.getAttribute(PLAYLISTS_KEY));
    }

    public List<DummyArtist> getSubscribedArtists(HttpSession session) {
        return getArtists(session);
    }

    public List<DummyPlaylist> getSubscribedPlaylists(HttpSession session) {
        return getPlaylists(session).stream()
                .filter(DummyPlaylist::isSubscribed)
                .collect(Collectors.toList());
    }

    public Optional<DummyArtist> findArtist(HttpSession session, String artistId) {
        return getArtists(session).stream().filter(a -> a.getId().equals(artistId)).findFirst();
    }

    public Optional<DummyPlaylist> findPlaylist(HttpSession session, String playlistId) {
        return getPlaylists(session).stream().filter(p -> p.getId().equals(playlistId)).findFirst();
    }

    public void resyncArtist(HttpSession session, String artistId) {
        findArtist(session, artistId).ifPresent(a -> a.setLastSyncedAt(LocalDateTime.now()));
    }

    public void unsubscribeArtist(HttpSession session, String artistId) {
        getArtists(session).removeIf(a -> a.getId().equals(artistId));
    }

    public void registerArtist(HttpSession session, String channelUrl) {
        getArtists(session).add(0, new DummyArtist(
                "artist-" + UUID.randomUUID().toString().substring(0, 8),
                "New Artist",
                channelUrl,
                "https://i.pravatar.cc/80?img=" + (int) (Math.random() * 70 + 1),
                LocalDateTime.now(),
                LocalDateTime.now()
        ));
    }

    public boolean passesDummyArtistValidation(String channelUrl) {
        if (channelUrl == null || channelUrl.isBlank()) return false;
        String lower = channelUrl.toLowerCase();
        return (lower.contains("youtube.com") || lower.contains("youtu.be")) && !lower.contains("notfound");
    }

    public void togglePlaylistSubscription(HttpSession session, String playlistId) {
        findPlaylist(session, playlistId).ifPresent(p -> p.setSubscribed(!p.isSubscribed()));
    }

    public void resyncPlaylist(HttpSession session, String playlistId) {
        findPlaylist(session, playlistId).ifPresent(p -> p.setLastSyncedAt(LocalDateTime.now()));
    }

    public List<DummyTrack> getMissingTracks(String artistId, String playlistId) {
        return List.of(
                new DummyTrack("m1", "Shiny Night", "Dummy Artist", 212),
                new DummyTrack("m2", "Blue Light", "Dummy Artist", 184),
                new DummyTrack("m3", "New Dawn", "Dummy Artist", 238)
        );
    }

    private void ensureData(HttpSession session) {
        if (session.getAttribute(ARTISTS_KEY) != null && session.getAttribute(PLAYLISTS_KEY) != null) {
            return;
        }

        List<DummyArtist> artists = new ArrayList<>(List.of(
                new DummyArtist("artist-1", "IU", "https://youtube.com/@iu", "https://i.pravatar.cc/80?img=1", LocalDateTime.now().minusHours(3), LocalDateTime.now().minusDays(40)),
                new DummyArtist("artist-2", "NewJeans", "https://youtube.com/@newjeans", "https://i.pravatar.cc/80?img=2", LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(35)),
                new DummyArtist("artist-3", "AKMU", "https://youtube.com/@akmu", "https://i.pravatar.cc/80?img=3", LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(20)),
                new DummyArtist("artist-4", "Crush", "https://youtube.com/@crush", "https://i.pravatar.cc/80?img=4", LocalDateTime.now().minusDays(4), LocalDateTime.now().minusDays(15)),
                new DummyArtist("artist-5", "Taeyeon", "https://youtube.com/@taeyeon", "https://i.pravatar.cc/80?img=5", LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(10)),
                new DummyArtist("artist-6", "DEAN", "https://youtube.com/@dean", "https://i.pravatar.cc/80?img=6", LocalDateTime.now().minusDays(6), LocalDateTime.now().minusDays(9))
        ));

        List<DummyTrack> baseTracks = List.of(
                new DummyTrack("t1", "Ditto", "NewJeans", 192),
                new DummyTrack("t2", "밤편지", "IU", 260),
                new DummyTrack("t3", "200%", "AKMU", 221)
        );

        List<DummyPlaylist> playlists = new ArrayList<>(List.of(
                new DummyPlaylist("pl-1", "My Favorites", "https://placehold.co/80x80", 20, LocalDateTime.now().minusHours(5), true, new ArrayList<>(baseTracks)),
                new DummyPlaylist("pl-2", "Drive Songs", "https://placehold.co/80x80", 35, LocalDateTime.now().minusDays(1), true, new ArrayList<>(baseTracks)),
                new DummyPlaylist("pl-3", "Morning Chill", "https://placehold.co/80x80", 18, LocalDateTime.now().minusDays(3), true, new ArrayList<>(baseTracks)),
                new DummyPlaylist("pl-4", "Workout", "https://placehold.co/80x80", 42, LocalDateTime.now().minusDays(6), false, new ArrayList<>(baseTracks)),
                new DummyPlaylist("pl-5", "Coding BGM", "https://placehold.co/80x80", 54, LocalDateTime.now().minusDays(8), false, new ArrayList<>(baseTracks)),
                new DummyPlaylist("pl-6", "Late Night", "https://placehold.co/80x80", 16, LocalDateTime.now().minusDays(10), true, new ArrayList<>(baseTracks))
        ));

        artists.sort(Comparator.comparing(DummyArtist::getLastSyncedAt).reversed());
        session.setAttribute(ARTISTS_KEY, artists);
        session.setAttribute(PLAYLISTS_KEY, playlists);
    }

    @SuppressWarnings("unchecked")
    private List<DummyArtist> castArtists(Object value) {
        return (List<DummyArtist>) value;
    }

    @SuppressWarnings("unchecked")
    private List<DummyPlaylist> castPlaylists(Object value) {
        return (List<DummyPlaylist>) value;
    }
}
