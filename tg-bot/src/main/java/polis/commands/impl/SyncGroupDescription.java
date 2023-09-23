package polis.commands.impl;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.commands.Command;
import polis.commands.context.Context;
import polis.data.domain.Account;
import polis.data.domain.ChannelGroup;
import polis.data.domain.CurrentChannel;
import polis.util.IState;
import polis.util.State;

import java.util.List;

@Component
public class SyncGroupDescription extends Command {
    private static final String SYNC_OK_TG_DESCRIPTION = """
            Телеграмм-канал <b>%s</b> и группа <b>%s (%s)</b> были успешно синхронизированы.
            Настроить функцию автопостинга можно по команде /%s.""";
    private static final String NOT_VALID_CURRENT_TG_CHANNEL_OR_GROUP_DESCRIPTION = """
            Невозможно показать информацию по связанным Телеграмм-каналу и группе.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";
    private static final int ROWS_COUNT = 1;
    private static final List<String> KEYBOARD_COMMANDS = List.of(State.Autoposting.getDescription());
    private static final List<String> KEYBOARD_COMMANDS_IN_ERROR_CASE = List.of(State.MainMenu.getDescription());

    @Override
    public IState state() {
        return State.SyncGroupDescription;
    }

    @Override
    public void doExecute(AbsSender absSender, User user, Chat chat, Context context) {
        Account currentAccount = context.currentAccount();
        ChannelGroup currentGroup = context.currentGroup();
        CurrentChannel currentChannel = context.currentChannel();

        boolean noErrorCondition = currentChannel != null && currentGroup != null && currentAccount != null;
        String text = noErrorCondition
                ? String.format(SYNC_OK_TG_DESCRIPTION, currentChannel.getChannelUsername(),
                    currentGroup.getGroupName(), currentGroup.getSocialMedia().getName(),
                    State.Autoposting.getIdentifier())
                : String.format(NOT_VALID_CURRENT_TG_CHANNEL_OR_GROUP_DESCRIPTION, State.MainMenu.getIdentifier());

        sendAnswerWithReplyKeyboardAndBackButton(
                absSender,
                chat.getId(),
                text,
                ROWS_COUNT,
                noErrorCondition ? KEYBOARD_COMMANDS : KEYBOARD_COMMANDS_IN_ERROR_CASE,
                loggingInfo(user.getUserName()));
    }
}
