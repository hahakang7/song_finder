package hyun9.song_finder.dto;

public record FollowedArtistDto(
        String name,
        boolean hasNewSong,
        String latestSong
) {}
