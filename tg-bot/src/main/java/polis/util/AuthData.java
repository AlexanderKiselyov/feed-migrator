package polis.util;

public class AuthData {
    private final SocialMedia socialMedia;
    // TODO изменить тип данных на Long при подключении БД
    private final Integer tokenId;
    private final String accessToken;
    private final String refreshToken;

    public AuthData(SocialMedia socialMedia, Integer tokenId, String accessToken, String refreshToken) {
        this.socialMedia = socialMedia;
        this.tokenId = tokenId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public SocialMedia getSocialMedia() {
        return socialMedia;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Integer getTokenId() {
        return tokenId;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
