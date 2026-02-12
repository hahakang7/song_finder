package hyun9.song_finder.service;

public interface SubscriptionSyncService {

    void subscribeAndSyncPlaylist(String userId, String accessToken, String playlistId, String playlistTitle);

    void subscribeAndSyncArtist(String userId, String accessToken, String channelId, String artistName);

    void unsubscribePlaylist(String userId, String playlistId);

    void unsubscribeArtist(String userId, String channelId);

}
