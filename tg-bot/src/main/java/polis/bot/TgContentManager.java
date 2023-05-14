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
    private static final Logger logger = LoggerFactory.getLogger(TgContentManager.class);
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot%s/getFile?file_id=%s";

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

    private File downloadFileById(String fileId, String nameToSet) throws IOException,
            TelegramApiException {
        FileInfo pathResponse = retrieveFilePath(tgApiToken, fileId);
        String tgApiFilePath = pathResponse.getFilePath();
        File file = fileLoader.downloadFile(tgApiFilePath);
        return TgContentManager.fileWithOrigName(tgApiFilePath, file, nameToSet);
    }

    private FileInfo retrieveFilePath(String botToken, String fileId) throws IOException {
        URI uri = URI.create(TELEGRAM_API_URL.formatted(botToken, fileId));
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
        String tmpFileName = containsCyrillic(fileName) ? transliterationFromRusToEng(fileName) : fileName;
        String absPath = file.getAbsolutePath();
        int nameIndex = absPath.lastIndexOf(File.separatorChar) + 1;
        String basePath = absPath.substring(0, nameIndex);
        Path path = Path.of(basePath + tmpFileName);
        int i = 0;
        while (Files.exists(path) && Files.exists(Path.of(basePath + i + File.separator + tmpFileName))) {
            i++;
        }
        try {
            if (Files.exists(path)) {
                Path folder = Path.of(basePath + i);
                if (!Files.exists(folder)) {
                    Files.createDirectory(folder);
                }
                path = Path.of(folder + File.separator + tmpFileName);
            }
            Files.move(file.toPath(), path);
        } catch (IOException e) {
            logger.error("Error while changing name of " + tgApiFilePath, e);
            throw new RuntimeException(e);
        }
        logger.info("Successfully changed name of file \"{}\" to file with path: {}", tgApiFilePath, path);
        return path.toFile();
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

    private static boolean containsCyrillic(String fileName) {
        return fileName.chars()
                .mapToObj(Character.UnicodeBlock::of)
                .anyMatch(b -> b.equals(Character.UnicodeBlock.CYRILLIC));
    }

    private static String transliterationFromRusToEng(String filename) {
        String CYRILLIC_TO_LATIN = "Russian-Latin/BGN";
        Transliterator toLatinTrans = Transliterator.getInstance(CYRILLIC_TO_LATIN);
        return toLatinTrans.transliterate(filename);
    }

    private static String getFileUrl(String botToken) {
        return TELEGRAM_API_URL + "/bot" + botToken + "/getFile";
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
