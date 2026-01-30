package hyun9.song_finder.service;

import java.util.Set;

public interface DumpService {

    Set<String> getDumpedTitles(String userId, String channelId);

    void dumpSong(String userId, String channelId, String normalizedTitle);
}
