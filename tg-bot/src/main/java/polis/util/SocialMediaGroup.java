package polis.util;

public class SocialMediaGroup {
    private final String id;
    private final String name;
    private final SocialMedia socialMedia;

    public SocialMediaGroup(String id, String name, SocialMedia socialMedia) {
        this.id = id;
        this.name = name;
        this.socialMedia = socialMedia;
    }

    public String getId() {
        return id;
    }

    public SocialMedia getSocialMedia() {
        return socialMedia;
    }

    public String getName() {
        return name;
    }
}
