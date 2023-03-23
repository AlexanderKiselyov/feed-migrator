package polis.util;

import java.util.ArrayList;
import java.util.List;

public class AuthData {
    private final SocialMedia socialMedia;
    private final String accessToken;
    private final String username;
    private final List<String> groupLinks;

    public AuthData(SocialMedia socialMedia, String accessToken, String username) {
        this.socialMedia = socialMedia;
        this.accessToken = accessToken;
        this.username = username;
        groupLinks = new ArrayList<>();
    }

    public SocialMedia getSocialMedia() {
        return socialMedia;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getGroupLinks() {
        return groupLinks;
    }

    public void addGroupLink(String newGroupLink) {
        groupLinks.add(newGroupLink);
    }
}
