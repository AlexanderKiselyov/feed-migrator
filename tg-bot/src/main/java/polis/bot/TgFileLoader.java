package polis.bot;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public interface TgFileLoader {
    File downloadFileById(String fileId) throws URISyntaxException, IOException, TelegramApiException;
    File downloadFileById(String fileId, String nameToSet) throws URISyntaxException, IOException, TelegramApiException;
}
