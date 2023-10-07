package polis.commands.impl;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.commands.Command;
import polis.commands.context.Context;
import polis.data.domain.CurrentChannel;
import polis.util.IState;
import polis.util.State;

import java.util.List;

@Component
public class AddGroup extends Command {
    private static final String ADD_GROUP_MSG = """
            Меню добавления групп для Телеграмм-канала <b>%s</b>.""";
    private static final String NOT_VALID_TG_CHANNEL_MSG = """
            Невозможно получить информацию по текущему Телеграмм-каналу.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";
    private static final int ROWS_COUNT = 2;
    private static final List<String> KEYBOARD_COMMANDS = List.of(
            State.AccountsList.getDescription(),
            State.AddOkAccount.getDescription(),
            State.AddVkAccount.getDescription()
    );

    @Override
    public IState state() {
        return State.AddGroup;
    }

    @Override
    public void doExecute(AbsSender absSender, User user, Chat chat, Context context) {
        CurrentChannel currentChannel = context.currentChannel();
        if (currentChannel != null) {
            sendAnswerWithReplyKeyboardAndBackButton(
                    absSender,
                    chat.getId(),
                    String.format(ADD_GROUP_MSG, currentChannel.getChannelUsername()),
                    ROWS_COUNT,
                    KEYBOARD_COMMANDS,
                    loggingInfo(user.getUserName()));
            return;
        }
        sendAnswerWithOnlyBackButton(
                absSender,
                chat.getId(),
                String.format(NOT_VALID_TG_CHANNEL_MSG, State.MainMenu.getIdentifier()),
                loggingInfo(user.getUserName()));
    }
}
