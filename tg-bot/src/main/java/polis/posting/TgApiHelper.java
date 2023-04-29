package polis.posting;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
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

public class TgApiHelper {
    private static final String TELEGRAM_API_URL = "https://api.telegram.org";
    private static final Logger logger = LoggerFactory.getLogger(TgApiHelper.class);

    private final String botToken;
    private final FileDownloader fileDownloader;

    @Autowired
    private HttpClient client;

    @Autowired
    private ObjectMapper mapper;

    public TgApiHelper(String botToken, FileDownloader fileDownloader) {
        this.botToken = botToken;
        this.fileDownloader = fileDownloader;
    }

    File download(Video video) throws URISyntaxException, IOException, TelegramApiException {
        String fileId = video.getFileId();
        return downloadFile(fileId);
    }

    File download(PhotoSize tgPhoto) throws URISyntaxException, IOException, TelegramApiException {
        String fileId = tgPhoto.getFileId();
        return downloadFile(fileId);
    }

    private File downloadFile(String fileId) throws URISyntaxException, IOException, TelegramApiException {
        GetFilePathResponse pathResponse = retrieveFilePath(fileId);
        String tgApiFilePath = pathResponse.getFilePath();
        File file = fileDownloader.downloadFile(tgApiFilePath);
        return fileWithOrigExtension(tgApiFilePath, file);
    }

    private static File fileWithOrigExtension(String tgApiFilePath, File file) {
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

    private GetFilePathResponse retrieveFilePath(String fileId) throws URISyntaxException, IOException {
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

    private static String getFileUrl(String botToken) {
        return TELEGRAM_API_URL + "/bot" + botToken + "/getFile";
    }

    @FunctionalInterface
    public interface FileDownloader {
        File downloadFile(String filePath) throws TelegramApiException;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GetFilePathResponse {
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
