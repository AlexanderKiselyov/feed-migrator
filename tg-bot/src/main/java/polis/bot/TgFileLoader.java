package polis.bot;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;

public interface TgFileLoader {
    File downloadFile(String filePath) throws TelegramApiException;
}
