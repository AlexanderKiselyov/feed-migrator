package polis.posting.ok;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.api.objects.polls.PollOption;
import polis.ok.api.OKClient;
import polis.ok.api.domain.Attachment;
import polis.ok.api.domain.Photo;
import polis.ok.api.domain.PhotoMedia;
import polis.ok.api.domain.PollMedia;
import polis.ok.api.domain.TextMedia;
import polis.ok.api.domain.Video;
import polis.ok.api.domain.VideoMedia;
import polis.ok.api.exceptions.OkApiException;
import polis.posting.ApiException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class OkPoster implements IOkPoster {

    private final OKClient okClient;

    @Autowired
    public OkPoster(OKClient okClient) {
        this.okClient = okClient;
    }

    @Override
    public List<String> uploadPhotos(List<File> photos, Integer userId, String accessToken, long groupId)
            throws URISyntaxException, IOException, ApiException {
        if (photos == null || photos.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return okClient.uploadPhotos(accessToken, groupId, photos);
        } catch (OkApiException e) {
            throw new ApiException(e);
        }
    }

    @Override
    public List<String> uploadVideos(List<File> videos, Integer userId, String accessToken, long groupId)
            throws URISyntaxException, IOException, ApiException {
        if (videos == null || videos.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> videoIds = new ArrayList<>(videos.size());
        for (File video : videos) {
            long videoId;
            try {
                videoId = okClient.uploadVideo(accessToken, groupId, video);
            } catch (OkApiException e) {
                throw new ApiException(e);
            }
            videoIds.add(String.valueOf(videoId));
        }
        return videoIds;
    }

    @Override
    public String getTextLinks(String text, List<MessageEntity> textLinks, String accessToken)
            throws URISyntaxException, IOException, ApiException {
        if (text != null && !text.isEmpty()) {
            int textGlobalOffset = 0;
            StringBuilder formattedText = new StringBuilder(text);
            try {
                for (MessageEntity textLink : textLinks) {
                    String shortTextLink = String.format(" (%s)",
                            okClient.getShortLink(accessToken, textLink.getUrl()));
                    formattedText.insert(textGlobalOffset + textLink.getOffset() + textLink.getLength(),
                            shortTextLink.toCharArray(), 0, shortTextLink.length());
                    textGlobalOffset += shortTextLink.length();
                }
                return formattedText.toString();
            } catch (OkApiException e) {
                throw new ApiException(e);
            }
        }
        return null;
    }

    @Override
    public OkPost newPost(String accessToken) {
        return new OkPost(accessToken);
    }

    public class OkPost implements IOkPost {
        private final Attachment attachment = new Attachment();
        private final String accessToken;

        public OkPost(String accessToken) {
            this.accessToken = accessToken;
        }

        @Override
        public OkPost addPhotos(List<String> photoIds) {
            if (photoIds == null || photoIds.isEmpty()) {
                return this;
            }
            PhotoMedia photoMedia = new PhotoMedia(
                    photoIds.stream().map(Photo::new).toList()
            );
            attachment.addMedia(photoMedia);
            return this;
        }

        @Override
        public OkPost addVideos(List<String> videoIds) {
            if (videoIds == null || videoIds.isEmpty()) {
                return this;
            }
            VideoMedia videoMedia = new VideoMedia(
                    videoIds.stream().map(Long::parseLong).map(Video::new).toList()
            );
            attachment.addMedia(videoMedia);
            return this;
        }

        @Override
        public OkPost addTextWithLinks(String formattedText) {
            if (formattedText != null && !formattedText.isEmpty()) {
                attachment.addMedia(new TextMedia(formattedText));
            }
            return this;
        }

        @Override
        public OkPost addPoll(Poll poll) {
            if (poll == null) {
                return this;
            }
            List<PollMedia.Option> options = new ArrayList<>();
            if (poll.getIsAnonymous()) {
                options.add(PollMedia.Option.ANONYMOUS_VOTING);
            }
            if (!poll.getAllowMultipleAnswers()) {
                options.add(PollMedia.Option.SINGLE_CHOICE);
            }
            if (poll.getIsClosed()) {
                options.add(PollMedia.Option.VOTING_CLOSED);
            }
            PollMedia pollMedia = new PollMedia(
                    poll.getQuestion(),
                    poll.getOptions().stream().map(PollOption::getText).toList(),
                    options
            );
            attachment.addMedia(pollMedia);
            return this;
        }

        @Override
        public OkPost addDocuments(List<String> documentIds) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long post(long groupId)
                throws URISyntaxException, IOException, ApiException {
            try {
                return okClient.postMediaTopic(accessToken, groupId, attachment);
            } catch (OkApiException e) {
                throw new ApiException(e);
            }
        }
    }
}
