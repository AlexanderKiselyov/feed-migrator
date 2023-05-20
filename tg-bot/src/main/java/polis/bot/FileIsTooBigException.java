package polis.bot;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class FileIsTooBigException extends TelegramApiException {
    public FileIsTooBigException() {
    }

    public FileIsTooBigException(String message) {
        super(message);
    }

    public FileIsTooBigException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileIsTooBigException(Throwable cause) {
        super(cause);
    }

    public FileIsTooBigException(String message, Throwable cause, boolean enableSuppression,
                                 boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
