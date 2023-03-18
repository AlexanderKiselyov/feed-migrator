package polis.util;

public class AuthData {
    private final SocialMedia socialMedia;
    private final String accessToken;
    private String groupLink;

    public AuthData(SocialMedia socialMedia, String accessToken) {
        this.socialMedia = socialMedia;
        this.accessToken = accessToken;
    }

    public SocialMedia getSocialMedia() {
        return socialMedia;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getGroupLink() {
        return groupLink;
    }

    public void setGroupLink(String newGroupLink) {
        groupLink = newGroupLink;
    }
}
