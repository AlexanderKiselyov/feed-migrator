package polis.keyboards;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import polis.util.State;

import java.util.ArrayList;
import java.util.List;

public class Keyboard {
    public static final String GO_BACK_BUTTON_TEXT = "Назад";

    public static SendMessage createSendMessage(Long chatId, String messageText, String... optionalCommands) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(messageText);
        Keyboard.setAllCommands(sendMessage, optionalCommands);
        return sendMessage;
    }

    private static synchronized void setAllCommands(SendMessage sendMessage, String... optionalCommands) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        for (State state: State.values()) {
            keyboardFirstRow.add(new KeyboardButton("/" + state.getIdentifier()));
        }
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        for (String command : optionalCommands) {
            keyboardSecondRow.add(new KeyboardButton(command));
        }
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);
    }
}
