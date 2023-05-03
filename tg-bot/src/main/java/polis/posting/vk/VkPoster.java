package polis.posting.vk;

import polis.posting.ApiException;
import polis.posting.Poster;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class VkPoster implements Poster {
    @Override
    public List<String> uploadPhotos(List<File> photos, String accessToken, long groupId) throws URISyntaxException, IOException, ApiException {
        return null;
    }

    @Override
    public List<String> uploadVideos(List<File> videos, String accessToken, long groupId) throws URISyntaxException, IOException, ApiException {
        return null;
    }

    @Override
    public Post newPost() {
        return null;
    }
}
