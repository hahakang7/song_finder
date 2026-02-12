package hyun9.song_finder.service;

import java.util.Set;

public interface DumpService {

    Set<String> getDumpedTitles(String userId, String channelId);

    void dump(String userId, String channelId, String normalizedTitle);

    void undo(
            String userId,
            String channelId,
            String normalizedTitle
    );

}
