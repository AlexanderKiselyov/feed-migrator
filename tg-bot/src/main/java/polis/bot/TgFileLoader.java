package polis.bot;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public interface TgFileLoader {
    File downloadFile(String filePath) throws TelegramApiException;
}
