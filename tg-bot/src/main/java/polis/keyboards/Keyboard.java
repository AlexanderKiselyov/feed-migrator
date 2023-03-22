package polis.keyboards;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

public abstract class Keyboard {
    public static String GO_BACK_BUTTON_TEXT = "Назад";

    public SendMessage createSendMessage(Long chatId, String messageText, int rowsCount, List<String> commands,
                                         String... optionalButtonsValues) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(messageText);
        getKeyboard(sendMessage, rowsCount, commands, optionalButtonsValues);
        return sendMessage;
    }

    void getKeyboard(SendMessage sendMessage, int rowsCount, List<String> commands,
                     String... optionalButtonsValues) {
    }
}
