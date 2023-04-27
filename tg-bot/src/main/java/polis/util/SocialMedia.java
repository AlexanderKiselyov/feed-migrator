package polis.util;

import java.util.Objects;

public enum SocialMedia {
    OK("Одноклассники"),
    VK("ВКонтакте");

    private final String name;

    SocialMedia(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static SocialMedia findSocialMediaByName(String name) {
        for (SocialMedia socialMedia : SocialMedia.values()) {
            if (Objects.equals(socialMedia.getName(), name)) {
                return socialMedia;
            }
        }
        return null;
    }
}
