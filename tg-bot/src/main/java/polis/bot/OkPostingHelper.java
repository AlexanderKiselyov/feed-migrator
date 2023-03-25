package polis.bot;

import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.api.objects.polls.PollOption;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.ok.api.OKClient;
import polis.ok.api.OkApiException;
import polis.ok.domain.Attachment;
import polis.ok.domain.Photo;
import polis.ok.domain.PhotoMedia;
import polis.ok.domain.PollMedia;
import polis.ok.domain.TextMedia;
import polis.ok.domain.VideoMedia;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OkPostingHelper extends PostingHelper {
    private final Bot bot;
    private final OKClient okClient;

    public OkPostingHelper(Bot bot, OKClient okClient) {
        super(bot);
        this.bot = bot;
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
        public Post addPhotos(List<PhotoSize> tgPhotos) throws URISyntaxException, IOException, TelegramApiException, OkApiException {
            if (tgPhotos == null || tgPhotos.isEmpty()) {
                return this;
            }
            List<File> photos = new ArrayList<>(tgPhotos.size());
            for (PhotoSize tgPhoto : tgPhotos) {
                TgApiHelper.GetFilePathResponse photoPathResponse;
                try {
                    photoPathResponse = bot.tgApiHelper.retrieveFilePath(bot.botToken, tgPhoto.getFileId());
                } catch (URISyntaxException e) {
                    bot.sendAnswer(chatId, "Проблемы при формировании url, проверьте введённые данные: " + e.getMessage());
                    throw e;
                } catch (IOException e) {
                    bot.sendAnswer(chatId, "Проблемы при получении от сервера Телеграмма расположения фотографий: " + e.getMessage());
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
        public Post addVideo(Video video) throws URISyntaxException, IOException, TelegramApiException {
            if (video == null) {
                return this;
            }
            String fileId = video.getFileId();
            TgApiHelper.GetFilePathResponse videoPathResponse;
            try {
                videoPathResponse = bot.tgApiHelper.retrieveFilePath(bot.botToken, fileId);
            } catch (URISyntaxException e) {
                bot.sendAnswer(chatId, "Проблемы при формировании url, проверьте введённые данные: " + e.getMessage());
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
                bot.sendAnswer(chatId, "Проблема при формировании url, проверьте введённые данные: " + e.getMessage());
                throw e;
            } catch (OkApiException e) {
                bot.sendAnswer(chatId, e.getMessage());
                throw e;
            } catch (IOException e) {
                bot.sendAnswer(chatId, "Проблемы с сетью при загрузке видео в Одноклассники: " + e.getMessage());
                throw e;
            }
            attachment.addMedia(new VideoMedia(Collections.singletonList(new polis.ok.domain.Video(videoId))));
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
        public void post(String accessToken, long groupId) throws URISyntaxException, IOException, OkApiException {
            okClient.postMediaTopic(accessToken, groupId, attachment);
        }

    }
}
