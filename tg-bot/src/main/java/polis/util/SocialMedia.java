package polis.util;

public enum SocialMedia {
    OK("Одноклассники");

    private final String name;

    SocialMedia(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
