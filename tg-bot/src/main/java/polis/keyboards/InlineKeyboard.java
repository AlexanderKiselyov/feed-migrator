package polis.keyboards;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import polis.keyboards.callbacks.objects.GoBackCallback;
import polis.keyboards.callbacks.parsers.GoBackCallbackParser;

import java.util.ArrayList;
import java.util.List;

@Component
public class InlineKeyboard extends Keyboard {

    @Autowired
    private GoBackCallbackParser goBackCallbackParser;

    // optionalButtonsValues : Button1-text, Button1-callbackData, Button2-text, Button2-callbackData...
    public synchronized void getKeyboard(SendMessage sendMessage, int rowsCount, List<String> commands,
                                         String... optionalButtonsValues) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        int buttonsAtTheRow = (int) Math.ceil((double) commands.size() / 2 / rowsCount);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (int i = 0; i < rowsCount; i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int j = 0, tmp = (i * buttonsAtTheRow + j) * 2; j < buttonsAtTheRow
                    && tmp < commands.size() - 1; j++, tmp++) {
                row.add(InlineKeyboardButton.builder()
                        .text(commands.get(tmp))
                        .callbackData(commands.get(++tmp))
                        .build()
                );
            }
            keyboard.add(row);
        }

        keyboard.add(List.of(InlineKeyboardButton.builder()
                .text(GO_BACK_BUTTON_TEXT)
                .callbackData(goBackCallbackParser.toText(GoBackCallback.INSTANCE))
                .build()
        ));

        inlineKeyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
    }
}
