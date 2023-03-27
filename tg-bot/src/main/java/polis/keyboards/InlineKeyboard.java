package polis.keyboards;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class InlineKeyboard extends Keyboard {

    public static final InlineKeyboard INSTANCE = new InlineKeyboard();

    // optionalButtonsValues : Button1-text, Button1-callbackData, Button2-text, Button2-callbackData...
    public synchronized void getKeyboard(SendMessage sendMessage, int rowsCount, List<String> commands,
                                         String... optionalButtonsValues) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        int buttonsAtTheRow = (int) Math.ceil((double) optionalButtonsValues.length / 2 / rowsCount);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (int i = 0; i < rowsCount; i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int j = 0, tmp = (i * buttonsAtTheRow + j) * 2; j < buttonsAtTheRow
                    && tmp < optionalButtonsValues.length - 1; j++, tmp++) {
                row.add(InlineKeyboardButton.builder()
                        .text(optionalButtonsValues[tmp])
                        .callbackData(optionalButtonsValues[++tmp])
                        .build()
                );
            }
            keyboard.add(row);
        }

        inlineKeyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
    }
}
