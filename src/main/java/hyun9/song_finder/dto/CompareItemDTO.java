package hyun9.song_finder.dto;

public class CompareItemDTO {

    private final String normalizedTitle;
    private final String thumbnailUrl; // null 허용
    private final CompareStatus status;

    public CompareItemDTO(String normalizedTitle, String thumbnailUrl, CompareStatus status) {
        this.normalizedTitle = normalizedTitle;
        this.thumbnailUrl = thumbnailUrl;
        this.status = status;
    }

    public String getNormalizedTitle() {
        return normalizedTitle;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public CompareStatus getStatus() {
        return status;
    }
}
