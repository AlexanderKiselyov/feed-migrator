package polis.posting;

import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.api.objects.polls.PollOption;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.ok.api.OKClient;
import polis.ok.api.domain.Attachment;
import polis.ok.api.domain.Photo;
import polis.ok.api.domain.PhotoMedia;
import polis.ok.api.domain.PollMedia;
import polis.ok.api.domain.TextMedia;
import polis.ok.api.domain.VideoMedia;
import polis.ok.api.exceptions.OkApiException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class OkPostingHelper extends PostingHelper {
    private final OKClient okClient;

    public OkPostingHelper(TgApiHelper tgApiHelper, OKClient okClient) {
        super(tgApiHelper);
        this.okClient = okClient;
    }

    @Override
    public OkPost newPost(long groupId, String accessToken) {
        return new OkPost(groupId, accessToken);
    }

    public class OkPost extends PostingHelper.Post {
        private final Attachment attachment = new Attachment();

        private OkPost(long groupId, String accessToken) {
            super(groupId, accessToken);
        }

        @Override
        public Post addPhotos(List<PhotoSize> tgPhotos)
                throws URISyntaxException, IOException, TelegramApiException, ApiException {
            if (tgPhotos == null || tgPhotos.isEmpty()) {
                return this;
            }
            List<File> photos = new ArrayList<>(tgPhotos.size());
            for (PhotoSize tgPhoto : tgPhotos) {
                File file = tgApiHelper.download(tgPhoto);
                photos.add(file);
            }
            PhotoMedia photoMedia = new PhotoMedia(photos.size());
            try {
                okClient.uploadPhotos(accessToken, groupId, photos).stream()
                        .map(Photo::new)
                        .forEach(photoMedia::addPhoto);
            } catch (OkApiException e) {
                throw new ApiException(e);
            }
            attachment.addMedia(photoMedia);
            return this;
        }

        @Override
        public Post addVideos(List<Video> videos) throws URISyntaxException, IOException, TelegramApiException,
                ApiException {
            if (videos == null || videos.isEmpty()) {
                return this;
            }
            VideoMedia videoMedia = new VideoMedia(videos.size());
            for (Video video : videos) {
                File file = tgApiHelper.download(video);
                long videoId;
                try {
                    videoId = okClient.uploadVideo(accessToken, groupId, file);
                } catch (OkApiException e) {
                    throw new ApiException(e);
                }
                videoMedia.addVideo(new polis.ok.api.domain.Video(videoId));
            }
            attachment.addMedia(videoMedia);
            return this;
        }

        @Override
        public Post addText(String text) {
            if (text != null && !text.isEmpty()) {
                attachment.addMedia(new TextMedia(text));
            }
            return this;
        }

        @Override
        public Post addPoll(Poll poll) {
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
        public Post addAnimations(List<Animation> animations) throws URISyntaxException, IOException,
                TelegramApiException, ApiException {
            if (animations == null || animations.isEmpty()) {
                return this;
            }

            List<Video> videos = new ArrayList<>(1);
            for (Animation animation : animations) {
                Video video = new Video();
                video.setDuration(animation.getDuration());
                video.setFileId(animation.getFileId());
                video.setFileName(animation.getFileName());
                video.setHeight(animation.getHeight());
                video.setThumb(animation.getThumb());
                video.setFileSize(animation.getFileSize());
                video.setFileUniqueId(animation.getFileUniqueId());
                video.setMimeType(animation.getMimetype());
                video.setWidth(animation.getWidth());
                videos.add(video);
            }

            return addVideos(videos);
        }

        @Override
        public Post addDocuments(List<Document> documents) {
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
