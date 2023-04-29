package polis.bot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class TgApiHelper {
    private static final String TELEGRAM_API_PATH = "https://api.telegram.org";


    private final HttpClient client;
    private final ObjectMapper mapper;

    public TgApiHelper(HttpClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    GetFilePathResponse retrieveFilePath(String botToken, String fileId) throws URISyntaxException, IOException {
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

    private static String apiGetFilePath(String botToken) {
        return TELEGRAM_API_PATH + "/bot" + botToken + "/getFile";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GetFilePathResponse {
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
