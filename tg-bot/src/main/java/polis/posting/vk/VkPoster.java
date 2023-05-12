package polis.posting.vk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import polis.posting.ApiException;
import polis.vk.api.VkClient;
import polis.vk.api.exceptions.VkApiException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Component
public class VkPoster implements IVkPoster {
    private final VkClient vkClient;

    @Autowired
    public VkPoster(VkClient vkClient) {
        this.vkClient = vkClient;
    }

    @Override
    public List<String> uploadPhotos(List<File> photos, Integer userId, String accessToken, long groupId)
            throws URISyntaxException, IOException, ApiException {
        try {
            return vkClient.uploadPhotos(userId, accessToken, groupId, photos);
        } catch (VkApiException e) {
            throw new ApiException(e.getCode(), e);
        }
    }

    @Override
    public List<String> uploadVideos(List<File> videos, Integer userId, String accessToken, long groupId)
            throws URISyntaxException, IOException, ApiException {
        List<String> videoIds = new ArrayList<>();

        try {
            for (File video : videos) {
                videoIds.add(String.valueOf(vkClient.uploadVideo(userId, accessToken, groupId, video)));
            }
        } catch (VkApiException e) {
            throw new ApiException(e.getCode(), e);
        }

        return videoIds;
    }

    @Override
    public String uploadPoll(Integer userId, String accessToken, String question, Boolean isAnonymous,
                             Boolean isMultiple, Boolean isClosed, List<String> answers)
            throws ApiException, URISyntaxException, IOException {
        try {
            return vkClient.createPoll(userId, accessToken, question, isAnonymous, isMultiple, isClosed, answers);
        } catch (VkApiException e) {
            throw new ApiException(e.getCode(), e);
        }

    }

    @Override
    public List<String> uploadDocuments(List<File> documents, Integer userId, String accessToken, long groupId)
            throws ApiException {
        try {
            return vkClient.saveDocuments(documents, userId, accessToken, groupId);
        } catch (VkApiException e) {
            throw new ApiException(e.getCode(), e);
        }
    }

    @Override
    public String getTextLinks(String text, List<MessageEntity> textLinks, String accessToken, Integer ownerId)
            throws URISyntaxException, IOException, ApiException {
        if (text != null && !text.isEmpty()) {
            int textGlobalOffset = 0;
            StringBuilder formattedText = new StringBuilder(text);
            try {
                for (MessageEntity textLink : textLinks) {
                    String shortTextLink = String.format(" (%s)",
                            vkClient.getShortLink(ownerId, accessToken, textLink.getUrl()));
                    formattedText.insert(textGlobalOffset + textLink.getOffset() + textLink.getLength(),
                            shortTextLink.toCharArray(), 0, shortTextLink.length());
                    textGlobalOffset += shortTextLink.length();
                }
                return formattedText.toString();
            } catch (VkApiException e) {
                throw new ApiException(e);
            }
        }
        return null;
    }

    @Override
    public VkPost newPost(Long ownerId, String accessToken) {
        return new VkPost(ownerId, accessToken);
    }

    public class VkPost implements IVkPost {
        private final List<String> attachments = new ArrayList<>();
        private String message = null;
        private final long ownerId;
        private final String accessToken;

        public VkPost(Long ownerId, String accessToken) {
            this.ownerId = ownerId;
            this.accessToken = accessToken;
        }

        @Override
        public VkPost addPhotos(List<String> photoIds) {
            if (photoIds == null || photoIds.isEmpty()) {
                return this;
            }

            for (String photoId : photoIds) {
                attachments.add(String.format("photo%d_%s", ownerId, photoId));
            }
            return this;
        }

        @Override
        public VkPost addVideos(List<String> videoIds, long groupId) {
            if (videoIds == null || videoIds.isEmpty()) {
                return this;
            }

            for (String videoId : videoIds) {
                attachments.add(String.format("video-%d_%s", groupId, videoId));
            }
            return this;
        }

        @Override
        public VkPost addTextWithLinks(String formattedText) {
            if (formattedText != null && !formattedText.isEmpty()) {
                message = formattedText;
            }
            return this;
        }

        @Override
        public VkPost addPoll(Poll poll, String pollId) {
            if (poll == null) {
                return this;
            }

            attachments.add(String.format("poll%d_%s", ownerId, pollId));
            return this;
        }

        @Override
        public VkPost addDocuments(List<String> documentIds, long groupId) {
            if (documentIds == null || documentIds.isEmpty()) {
                return this;
            }

            for (String documentId : documentIds) {
                attachments.add(String.format("doc-%d_%s", groupId, documentId));
            }
            return this;
        }

        @Override
        public long post(Integer userId, long groupId) throws ApiException {
            try {
                return vkClient.postMediaTopic(userId, accessToken, groupId, message,
                        String.join(",", attachments));
            } catch (VkApiException e) {
                throw new ApiException(e.getCode(), e);
            }
        }
    }
}
