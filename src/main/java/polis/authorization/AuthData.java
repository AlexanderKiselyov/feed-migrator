package polis.authorization;

import polis.util.SocialMedia;

public class AuthData {
    private final SocialMedia socialMedia;
    private final String accessToken;

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
}
