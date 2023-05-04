package polis.posting.ok;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
import polis.posting.Poster;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class OkPoster implements Poster {

    private final OKClient okClient;

    @Autowired
    public OkPoster(OKClient okClient) {
        this.okClient = okClient;
    }

    @Override
    public List<String> uploadPhotos(List<File> photos, String accessToken, long groupId) throws URISyntaxException, IOException, ApiException {
        if(photos == null || photos.isEmpty()){
            return Collections.emptyList();
        }
        try {
            return okClient.uploadPhotos(accessToken, groupId, photos);
        } catch (OkApiException e) {
            throw new ApiException(e);
        }
    }

    @Override
    public List<String> uploadVideos(List<File> videos, String accessToken, long groupId) throws URISyntaxException, IOException, ApiException {
        if(videos == null || videos.isEmpty()){
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
    public OkPost newPost() {
        return new OkPost();
    }

    public class OkPost implements Poster.Post {
        private final Attachment attachment = new Attachment();

        @Override
        public Post addPhotos(List<String> photoIds) {
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
        public OkPost addText(String text) {
            if (text != null && !text.isEmpty()) {
                attachment.addMedia(new TextMedia(text));
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
        public OkPost addDocuments(List<File> documents) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void post(String accessToken, long groupId) throws URISyntaxException, IOException, ApiException {
            try {
                okClient.postMediaTopic(accessToken, groupId, attachment);
            } catch (OkApiException e) {
                throw new ApiException(e);
            }
        }
    }
}
