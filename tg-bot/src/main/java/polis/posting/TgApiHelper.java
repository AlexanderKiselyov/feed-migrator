package polis.posting;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
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
import java.util.function.Function;

public class TgApiHelper {
    private static final String TELEGRAM_API_PATH = "https://api.telegram.org";

    private final String botToken;
    private final HttpClient client = HttpClient.newHttpClient();
    private final FileDownloader fileDownloader;
    private final ObjectMapper mapper = new ObjectMapper();

    public TgApiHelper(String botToken, FileDownloader fileDownloader) {
        this.botToken = botToken;
        this.fileDownloader = fileDownloader;
    }

    File download(Video video) throws URISyntaxException, IOException, TelegramApiException {
        String fileId = video.getFileId();
        GetFilePathResponse videoPathResponse = retrieveFilePath(fileId);
        String filePath = videoPathResponse.getFilePath();
        String origExtension = filePath.substring(filePath.lastIndexOf('.'));

        File file = fileDownloader.downloadFile(filePath);
        return changeExtension(origExtension, file);
    }

    File download(PhotoSize tgPhoto) throws URISyntaxException, IOException, TelegramApiException {
        GetFilePathResponse photoPathResponse = retrieveFilePath(tgPhoto.getFileId());
        String filePath = photoPathResponse.getFilePath();
        String origExtension = filePath.substring(filePath.lastIndexOf('.'));

        File file = fileDownloader.downloadFile(photoPathResponse.getFilePath());
        return changeExtension(origExtension, file);
    }

    private GetFilePathResponse retrieveFilePath(String fileId) throws URISyntaxException, IOException {
        //https://api.telegram.org/bot<bot_token>/getFile?file_id=the_file_id
        URI uri = new URIBuilder(apiGetFilePath(botToken))
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

    private static File changeExtension(String newExtension, File file) {
        String absPath = file.getAbsolutePath();
        int dotIndex = absPath.lastIndexOf('.');
        Path path = Path.of(absPath.substring(0, dotIndex) + newExtension);
        try {
            Files.move(file.toPath(), path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return path.toFile();
    }



    private static String apiGetFilePath(String botToken) {
        return TELEGRAM_API_PATH + "/bot" + botToken + "/getFile";
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
