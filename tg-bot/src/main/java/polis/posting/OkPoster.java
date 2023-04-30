package polis.posting;

import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.api.objects.polls.PollOption;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.ok.api.OKClient;
import polis.ok.api.domain.*;
import polis.ok.api.exceptions.OkApiException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class OkPoster implements Poster {
    private final OKClient okClient;

    public OkPoster(OKClient okClient) {
        this.okClient = okClient;
    }

    @Override
    public OkPost newPost(long groupId, String accessToken) {
        return new OkPost(groupId, accessToken);
    }

    public class OkPost extends Poster.Post {
        private final Attachment attachment = new Attachment();

        private OkPost(long groupId, String accessToken) {
            super(groupId, accessToken);
        }

        @Override
        public Post addPhotos(List<File> photos)
                throws URISyntaxException, IOException, TelegramApiException, ApiException {
            if (photos == null || photos.isEmpty()) {
                return this;
            }
            PhotoMedia photoMedia = new PhotoMedia(photos.size());
            try {
                okClient.uploadPhotos(accessToken, groupId, photos).stream().map(Photo::new).forEach(photoMedia::addPhoto);
            } catch (OkApiException e) {
                throw new ApiException(e);
            }
            attachment.addMedia(photoMedia);
            return this;
        }

        @Override
        public OkPost addVideos(List<File> videos) throws URISyntaxException, IOException, TelegramApiException, ApiException {
            if (videos == null || videos.isEmpty()) {
                return this;
            }
            VideoMedia videoMedia = new VideoMedia(videos.size());
            for (File video : videos) {
                long videoId;
                try {
                    videoId = okClient.uploadVideo(accessToken, groupId, video);
                } catch (OkApiException e) {
                    throw new ApiException(e);
                }
                videoMedia.addVideo(new polis.ok.api.domain.Video(videoId));
            }
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
        public OkPost addAnimations(List<File> animations) throws URISyntaxException, IOException,
                TelegramApiException, ApiException {
            if (animations == null || animations.isEmpty()) {
                return this;
            }
            return addVideos(animations);
        }

        @Override
        public OkPost addDocuments(List<File> documents) {
            return this;
        }

        @Override
        public void post(String accessToken) throws URISyntaxException, IOException, ApiException {
            try {
                okClient.postMediaTopic(accessToken, groupId, attachment);
            } catch (OkApiException e) {
                throw new ApiException(e);
            }
        }

    }
}
