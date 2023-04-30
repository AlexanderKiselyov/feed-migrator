package polis.bot;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public interface TgFileLoader {
    File downloadFileById(String fileId) throws URISyntaxException, IOException, TelegramApiException;
}
