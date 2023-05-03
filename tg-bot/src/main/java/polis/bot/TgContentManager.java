package polis.bot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class TgContentManager {
    private static final String TELEGRAM_API_URL = "https://api.telegram.org";
    private static final Logger logger = LoggerFactory.getLogger(TgContentManager.class);

    private final TgFileLoader fileLoader;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public TgContentManager(@Qualifier("Bot") TgFileLoader fileLoader) {
        this.fileLoader = fileLoader;
    }

    public File download(Video video) throws URISyntaxException, IOException, TelegramApiException {
        String fileId = video.getFileId();
        return fileLoader.downloadFileById(fileId);
    }

    public File download(PhotoSize tgPhoto) throws URISyntaxException, IOException, TelegramApiException {
        String fileId = tgPhoto.getFileId();
        return fileLoader.downloadFileById(fileId);
    }

    public static List<Video> toVideos(List<Animation> animations) {
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
        return videos;
    }

    GetFilePathResponse retrieveFilePath(String botToken, String fileId) throws URISyntaxException, IOException {
        //https://api.telegram.org/bot<bot_token>/getFile?file_id=the_file_id
        URI uri = new URIBuilder(getFileUrl(botToken))
                .addParameter("file_id", fileId)
                .build();
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        HttpResponse<String> response;
        try {
            response = client.send(request, BodyHandlers.ofString());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        JSONObject object = new JSONObject(response.body());
        return mapper.readValue(object.getJSONObject("result").toString(), GetFilePathResponse.class);
    }

    static File fileWithOrigExtension(String tgApiFilePath, File file) {
        String origExtension = tgApiFilePath.substring(tgApiFilePath.lastIndexOf('.'));
        String absPath = file.getAbsolutePath();
        int dotIndex = absPath.lastIndexOf('.');
        Path path = Path.of(absPath.substring(0, dotIndex) + origExtension);
        try {
            Files.move(file.toPath(), path);
        } catch (IOException e) {
            logger.error("Error while changing extension of " + tgApiFilePath, e);
            throw new RuntimeException(e);
        }
        return path.toFile();
    }

    private static String getFileUrl(String botToken) {
        return TELEGRAM_API_URL + "/bot" + botToken + "/getFile";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GetFilePathResponse {
        @JsonProperty("file_id")
        private String fileId;

        @JsonProperty("file_size")
        private String fileSize;

        @JsonProperty("file_path")
        private String filePath;

        public String getFileId() {
            return fileId;
        }

        public String getFileSize() {
            return fileSize;
        }

        public String getFilePath() {
            return filePath;
        }
    }
}
