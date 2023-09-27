package polis.commands.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.commands.Command;
import polis.commands.context.Context;
import polis.data.domain.ChannelGroup;
import polis.data.domain.CurrentChannel;
import polis.callbacks.inlinekeyboard.objects.AutopostingCallback;
import polis.callbacks.inlinekeyboard.parsers.AutopostingCallbackParser;
import polis.util.IState;
import polis.util.State;

import java.util.List;

@Component
public class Autoposting extends Command {
    private static final String AUTOPOSTING_MSG = """
            Функция автопостинга позволяет автоматически публиковать новый пост из Телеграмм-канала в группу.
            Включить данную функцию для Телеграмм-канала <b>%s</b> и группы <b>%s (%s)</b>?""";
    private static final String NO_CURRENT_TG_CHANNEL_MSG = """
            По какой-то причине не хватает информации о выбранной группе или телеграмм каналу
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";

    @Autowired
    private AutopostingCallbackParser autopostingCallbackParser;

    private static final int ROWS_COUNT = 1;
    private static final List<String> KEYBOARD_COMMANDS_IN_ERROR_CASE = List.of(State.MainMenu.getDescription());

    @Override
    public IState state() {
        return State.Autoposting;
    }

    @Override
    public void doExecute(AbsSender absSender, User user, Chat chat, Context context) {
        ChannelGroup currentGroup = context.currentGroup();
        CurrentChannel currentChannel = context.currentChannel();

        if (currentChannel == null || currentGroup == null) {
            sendAnswerWithReplyKeyboardAndBackButton(
                    absSender,
                    chat.getId(),
                    String.format(NO_CURRENT_TG_CHANNEL_MSG, State.MainMenu.getIdentifier()),
                    ROWS_COUNT,
                    KEYBOARD_COMMANDS_IN_ERROR_CASE,
                    loggingInfo(user.getUserName())
            );
            return;
        }
        String groupName = currentGroup.getGroupName();
        sendAnswerWithInlineKeyboard(
                absSender,
                chat.getId(),
                String.format(AUTOPOSTING_MSG, currentChannel.getChannelUsername(), groupName,
                        currentGroup.getSocialMedia().getName()),
                ROWS_COUNT,
                getButtonsForAutopostingOptions(chat.getId(), currentChannel.getChannelId()),
                loggingInfo(user.getUserName())
        );
    }

    private List<String> getButtonsForAutopostingOptions(long chatId, long channelId) {
        return List.of(
                YES_ANSWER,
                autopostingCallbackParser.toText(new AutopostingCallback(chatId, channelId, true)),
                NO_ANSWER,
                autopostingCallbackParser.toText(new AutopostingCallback(chatId, channelId, false))
        );
    }
}
