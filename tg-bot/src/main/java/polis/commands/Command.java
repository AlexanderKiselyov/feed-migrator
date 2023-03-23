package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import polis.keyboards.InlineKeyboard;
import polis.keyboards.ReplyKeyboard;
import java.util.List;

abstract class Command extends BotCommand {
    private final Logger logger = LoggerFactory.getLogger(Command.class);
    final int rowsCount = 0;
    final List<String> commandsForKeyboard = List.of(
    );

    public Command(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    void sendAnswer(AbsSender absSender, Long chatId, String commandName, String userName, String text, int rowsCount,
                    List<String> commandsList, String[] inlineKeyboardCommands,
                    String... optionalButtonsValues) {
        SendMessage message;
        boolean hasInlineKeyboard = inlineKeyboardCommands != null && inlineKeyboardCommands.length != 0;
        if (rowsCount == 0 && optionalButtonsValues.length == 0 && !hasInlineKeyboard) {
            message = new SendMessage();
        } else if (!hasInlineKeyboard) {
            message = ReplyKeyboard.INSTANCE.createSendMessage(chatId, text, rowsCount, commandsList,
                    optionalButtonsValues);
        } else {
            message = InlineKeyboard.INSTANCE.createSendMessage(chatId, text, inlineKeyboardCommands.length / 4,
                    commandsList);
        }
        message.setChatId(chatId.toString());
        message.setParseMode(ParseMode.HTML);
        message.setText(text);
        message.disableWebPagePreview();

        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            logger.error(String.format("Cannot execute command %s of user %s: %s", commandName, userName,
                    e.getMessage()));
        }
    }
}
