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
public class TgChannelDescription extends Command {
    private static final String TELEGRAM_CHANNEL_DESCRIPTION = """
            Текущий выбранный Телеграмм-канал <b>%s</b>.
            Вы можете посмотреть синхронизированные с каналом группы по команде /%s.
            Добавить новую группу можно по команде /%s.""";
    private static final String NOT_VALID_CHANNEL = String.format("""
                    Телеграмм-канал не был выбран.
                    Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""",
            State.MainMenu.getIdentifier());
    private static final int ROWS_COUNT = 3;
    private static final int ROWS_COUNT_IN_ERROR_CASE = 1;
    private static final List<String> KEYBOARD_COMMANDS = List.of(
            State.TgSyncGroups.getDescription(),
            State.AddGroup.getDescription(),
            State.MainMenu.getDescription()
    );
    private static final List<String> KEYBOARD_COMMANDS_IN_ERROR_CASE = List.of(State.MainMenu.getDescription());

    @Override
    public IState state() {
        return State.TgChannelDescription;
    }

    @Override
    public void doExecute(AbsSender absSender, User user, Chat chat, Context context) {
        CurrentChannel currentChannel = context.currentChannel();
        boolean noErrorCondition = currentChannel != null;
        String text = noErrorCondition ? String.format(TELEGRAM_CHANNEL_DESCRIPTION,
                currentChannel.getChannelUsername(), State.TgSyncGroups.getIdentifier(), State.AddGroup.getIdentifier())
                : NOT_VALID_CHANNEL;

        sendAnswerWithReplyKeyboard(
                absSender,
                chat.getId(),
                text,
                noErrorCondition ? ROWS_COUNT : ROWS_COUNT_IN_ERROR_CASE,
                noErrorCondition ? KEYBOARD_COMMANDS : KEYBOARD_COMMANDS_IN_ERROR_CASE,
                loggingInfo(user.getUserName()));
    }
}
