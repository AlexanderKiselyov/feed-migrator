package polis.keyboards;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import polis.util.State;

import java.util.ArrayList;
import java.util.List;

public class ReplyKeyboard extends Keyboard {

    public static final ReplyKeyboard INSTANCE = new ReplyKeyboard();

    public void getKeyboard(SendMessage sendMessage, int rowsCount, List<String> commands,
                            String... optionalButtonsValues) {
        // TODO: Проверка, не пришел ли пустой список команд без доп. кнопок
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        int buttonsAtTheRow = (int) Math.ceil((double) commands.size() / rowsCount);

        List<KeyboardRow> keyboard = new ArrayList<>();
        for (int i = 0; i < rowsCount; i++) {
            KeyboardRow row = new KeyboardRow();
            for (int j = 0, tmp = i * buttonsAtTheRow + j; j < buttonsAtTheRow && tmp < commands.size(); j++, tmp++) {
                row.add(new KeyboardButton(commands.get(tmp)));
            }
            keyboard.add(row);
        }

        if (optionalButtonsValues.length != 0) {
            KeyboardRow keyboardSecondRow = new KeyboardRow();
            for (String command : optionalButtonsValues) {
                keyboardSecondRow.add(new KeyboardButton(command));
            }
            keyboard.add(keyboardSecondRow);
        }

        replyKeyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
    }
}
