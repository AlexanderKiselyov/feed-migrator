package polis.bot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.text.Transliterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.Nullable;
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

@Component
public class TgContentManager {
    public static final int FILE_SIZE_LIMIT_MB = 20; //todo increase
    private static final String CYRILLIC_TO_LATIN = "Russian-Latin/BGN";
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot%s/getFile?file_id=%s";
    private static final String FILE_IS_TOO_BIG_API_DESC = "file is too big";
    private static final Logger logger = LoggerFactory.getLogger(TgContentManager.class);

    private final TgFileLoader fileLoader;
    private final String tgApiToken;
    private final HttpClient client;
    private final ObjectMapper mapper;

    @Autowired
    public TgContentManager(
            @Qualifier("Bot") TgFileLoader fileLoader,
            @Value("${bot.token}") String tgApiToken,
            HttpClient client,
            ObjectMapper mapper
    ) {
        this.fileLoader = fileLoader;
        this.tgApiToken = tgApiToken;
        this.client = client;
        this.mapper = mapper;
    }

    public File download(Video video) throws IOException, TelegramApiException {
        String fileId = video.getFileId();
        return downloadFileById(fileId, video.getFileName());
    }

    public File download(PhotoSize tgPhoto) throws IOException, TelegramApiException {
        String fileId = tgPhoto.getFileId();
        return downloadFileById(fileId);
    }

    public File download(Document document) throws URISyntaxException, IOException, TelegramApiException {
        String fileId = document.getFileId();
        return downloadFileById(fileId, document.getFileName());
    }

    private File downloadFileById(String fileId) throws IOException, TelegramApiException {
        FileInfo pathResponse = retrieveFilePath(tgApiToken, fileId);
        String tgApiFilePath = pathResponse.getFilePath();
        File file = fileLoader.downloadFile(tgApiFilePath);
        return TgContentManager.fileWithOrigExtension(tgApiFilePath, file);
    }

    private File downloadFileById(String fileId, String nameToSet) throws IOException, TelegramApiException {
        FileInfo pathResponse = retrieveFilePath(tgApiToken, fileId);
        String tgApiFilePath = pathResponse.getFilePath();
        File file = fileLoader.downloadFile(tgApiFilePath);
        return TgContentManager.fileWithOrigName(tgApiFilePath, file, nameToSet);
    }

    private FileInfo retrieveFilePath(String botToken, String fileId) throws IOException, TelegramApiException {
        URI uri = URI.create(TELEGRAM_API_URL.formatted(botToken, fileId));
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        HttpResponse<String> response;
        try {
            response = client.send(request, BodyHandlers.ofString());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        GetFileResponse getFileResponse = mapper.readValue(response.body(), GetFileResponse.class);
        if (getFileResponse.isOk()) {
            return getFileResponse.getResult();
        }
        logger.error("Not ok response when loading file:\n\t" + getFileResponse);
        if (getFileResponse.description.contains(FILE_IS_TOO_BIG_API_DESC)) {
            throw new FileIsTooBigException();
        }
        throw new TelegramApiException("Error when loading file %s due %s:%s"
                .formatted(fileId, getFileResponse.getErrorCode(), getFileResponse.getDescription()));
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

    private static File fileWithOrigName(String tgApiFilePath, File file, String fileName) {
        String tmpFileName = containsCyrillic(fileName) ? transliterateFromRusToEng(fileName) : fileName;
        Path res;
        try {
            Path tempDir = Files.createTempDirectory("bot-tmp");
            res = Files.move(file.toPath(), tempDir.resolve(tmpFileName));
        } catch (IOException e) {
            logger.error("Error while changing name of " + tgApiFilePath, e);
            throw new RuntimeException(e);
        }
        logger.info("Successfully changed name of file \"{}\" to file with path: {}", tgApiFilePath, res);
        return res.toFile();
    }

    public static Video toVideo(Animation animation) {
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
        return video;
    }

    private static boolean containsCyrillic(String fileName) {
        return fileName.chars()
                .mapToObj(Character.UnicodeBlock::of)
                .anyMatch(b -> b.equals(Character.UnicodeBlock.CYRILLIC));
    }

    private static String transliterateFromRusToEng(String filename) {
        Transliterator toLatinTrans = Transliterator.getInstance(CYRILLIC_TO_LATIN);
        return toLatinTrans.transliterate(filename);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GetFileResponse {
        @JsonProperty("ok")
        private boolean ok;

        @JsonProperty("error_code")
        private int errorCode;

        @JsonProperty("description")
        private String description;

        @Nullable //in case not ok response
        @JsonProperty("result")
        private FileInfo result;

        public boolean isOk() {
            return ok;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public String getDescription() {
            return description;
        }

        public FileInfo getResult() {
            return result;
        }

        @Override
        public String toString() {
            return "GetFileResponse{"
                    + "ok=" + ok
                    + ", errorCode=" + errorCode
                    + ", description='" + description + '\''
                    + ", result=" + result
                    + "}";
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FileInfo {
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
