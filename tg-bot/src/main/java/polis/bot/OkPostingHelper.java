package polis.bot;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class OkPostingHelper extends PostingHelper {
    private final OKClient okClient;

    public OkPostingHelper(Bot bot, String botToken, TgApiHelper tgApiHelper, OKClient okClient) {
        super(bot, botToken, tgApiHelper);
        this.okClient = okClient;
    }

    private static File withExtension(String origExtension, File file) {
        String absPath = file.getAbsolutePath();
        int dotIndex = absPath.lastIndexOf('.');
        Path path = Path.of(absPath.substring(0, dotIndex) + origExtension);
        try {
            Files.move(file.toPath(), path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return path.toFile();
    }

    @Override
    public OkPost newPost(long chatId, long groupId, String accessToken) {
        return new OkPost(chatId, groupId, accessToken);
    }

    public class OkPost extends PostingHelper.Post {
        private final long chatId;
        private final Attachment attachment = new Attachment();

        private OkPost(long chatId, long groupId, String accessToken) {
            super(chatId, groupId, accessToken);
            this.chatId = chatId;
        }

        @Override
        public Post addPhotos(List<PhotoSize> tgPhotos)
                throws URISyntaxException, IOException, TelegramApiException, OkApiException {
            if (tgPhotos == null || tgPhotos.isEmpty()) {
                return this;
            }
            List<File> photos = new ArrayList<>(tgPhotos.size());
            for (PhotoSize tgPhoto : tgPhotos) {
                TgApiHelper.GetFilePathResponse photoPathResponse;
                try {
                    photoPathResponse = tgApiHelper.retrieveFilePath(botToken, tgPhoto.getFileId());
                } catch (URISyntaxException e) {
                    bot.sendAnswer(chatId, "Проблемы при формировании url, проверьте введённые данные: "
                            + e.getMessage());
                    throw e;
                } catch (IOException e) {
                    bot.sendAnswer(chatId, "Проблемы при получении от сервера Телеграмма расположения фотографий: "
                            + e.getMessage());
                    throw e;
                }

                String filePath = photoPathResponse.getFilePath();
                String origExtension = filePath.substring(filePath.lastIndexOf('.'));
                try {
                    File photo = bot.downloadFile(photoPathResponse.getFilePath());
                    photos.add(withExtension(origExtension, photo));
                } catch (TelegramApiException e) {
                    throw e;
                }
            }

            PhotoMedia photoMedia = new PhotoMedia(photos.size());
            try {
                okClient.uploadPhotos(accessToken, groupId, photos)
                        .stream().map(Photo::new).forEach(photoMedia::addPhoto);
            } catch (URISyntaxException e) {
                bot.sendAnswer(chatId, "Проблема при формировании url, проверьте введённые данные: " + e.getMessage());
                throw e;
            } catch (OkApiException e) {
                bot.sendAnswer(chatId, "Проблема при загрузке фото из Телеграмма " + e.getMessage());
                throw e;
            } catch (IOException e) {
                bot.sendAnswer(chatId, "Проблемы с сетью при загрузке фото в Одноклассники: " + e.getMessage());
                throw e;
            }
            attachment.addMedia(photoMedia);
            return this;
        }

        @Override
        public Post addVideos(List<Video> videos) throws URISyntaxException, IOException, TelegramApiException {
            if (videos == null || videos.isEmpty()) {
                return this;
            }

            VideoMedia videoMedia = new VideoMedia(videos.size());
            for (Video video : videos) {
                String fileId = video.getFileId();
                TgApiHelper.GetFilePathResponse videoPathResponse;
                try {
                    videoPathResponse = tgApiHelper.retrieveFilePath(botToken, fileId);
                } catch (URISyntaxException e) {
                    bot.sendAnswer(chatId, "Проблемы при формировании url, проверьте введённые данные: "
                            + e.getMessage());
                    throw e;
                } catch (IOException e) {
                    bot.sendAnswer(chatId, "Проблемы при получении расположения видео: " + e.getMessage());
                    throw e;
                }
                File file;
                try {
                    String filePath = videoPathResponse.getFilePath();
                    String origExtension = filePath.substring(filePath.lastIndexOf('.'));
                    file = withExtension(origExtension, bot.downloadFile(filePath));
                } catch (TelegramApiException e) {
                    bot.sendAnswer(chatId, "Проблема при загрузке видео из Телеграмма " + e.getMessage());
                    throw e;
                }
                long videoId;
                try {
                    videoId = okClient.uploadVideo(accessToken, groupId, file);
                } catch (URISyntaxException e) {
                    bot.sendAnswer(chatId, "Проблема при формировании url, проверьте введённые данные: "
                            + e.getMessage());
                    throw e;
                } catch (OkApiException e) {
                    bot.sendAnswer(chatId, e.getMessage());
                    throw e;
                } catch (IOException e) {
                    bot.sendAnswer(chatId, "Проблемы с сетью при загрузке видео в Одноклассники: " + e.getMessage());
                    throw e;
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
                TelegramApiException {
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
        public void post(String accessToken, long groupId) throws URISyntaxException, IOException, OkApiException {
            okClient.postMediaTopic(accessToken, groupId, attachment);
        }

    }
}
