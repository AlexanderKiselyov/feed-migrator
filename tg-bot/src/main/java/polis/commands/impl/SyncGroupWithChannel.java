package polis.commands.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.commands.Command;
import polis.commands.context.Context;
import polis.data.domain.Account;
import polis.data.domain.ChannelGroup;
import polis.data.domain.CurrentChannel;
import polis.callbacks.typed.objects.YesNoCallback;
import polis.callbacks.typed.parsers.YesNoCallbackParser;
import polis.util.State;

import java.util.List;

import static polis.util.Emojis.EIGHTEEN_PLUS;
import static polis.util.Emojis.HAPPY_FACE;
import static polis.util.Emojis.STOP_PROFANITY;

@Component
public abstract class SyncGroupWithChannel extends Command {
    private static final String SYNC_MSG = """
            Вы выбрали Телеграмм-канал <b>%s</b> и группу <b>%s (%s)</b>.
            Хотите ли Вы синхронизировать их?
                        
            <b>* При размещении контента на Вашем канале очень важно уважать права других авторов, в связи с чем"""
            + """
             автопостинг для пересланных сообщений не осуществляется %s
            * Запрещается публиковать контент, нарушающий законодательство РФ (ненормативная лексика, контент 18+ и\s"""
            + """
            др.) %s %s</b>""";
    private static final String NOT_VALID_CURRENT_TG_CHANNEL_OR_GROUP = """
            Невозможно связать Телеграмм-канал и группу.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";
    private static final int ROWS_COUNT = 1;
    private static final List<String> KEYBOARD_COMMANDS_IN_ERROR_CASE = List.of(State.MainMenu.getDescription());

    @Autowired
    private YesNoCallbackParser yesNoCallbackParser;

    @Override
    public void doExecute(AbsSender absSender, User user, Chat chat, Context context) {
        Account currentAccount = context.currentAccount();
        ChannelGroup currentGroup = context.currentGroup();
        CurrentChannel currentChannel = context.currentChannel();
        if (currentChannel != null && currentGroup != null && currentAccount != null) {
            String groupName = currentGroup.getGroupName();
            sendAnswerWithInlineKeyboard(
                    absSender,
                    chat.getId(),
                    String.format(SYNC_MSG, currentChannel.getChannelUsername(), groupName,
                            currentGroup.getSocialMedia().getName(), HAPPY_FACE, STOP_PROFANITY, EIGHTEEN_PLUS),
                    ROWS_COUNT,
                    getButtonsForSyncOptions(),
                    loggingInfo(user.getUserName()));
            return;
        }
        sendAnswerWithReplyKeyboardAndBackButton(
                absSender,
                chat.getId(),
                String.format(NOT_VALID_CURRENT_TG_CHANNEL_OR_GROUP, State.MainMenu.getIdentifier()),
                ROWS_COUNT,
                KEYBOARD_COMMANDS_IN_ERROR_CASE,
                loggingInfo(user.getUserName()));
    }

    private List<String> getButtonsForSyncOptions() {
        return List.of(
                YES_ANSWER,
                yesNoCallbackParser.toText(YesNoCallback.YES_CALLBACK),
                NO_ANSWER,
                yesNoCallbackParser.toText(YesNoCallback.NO_CALLBACK)
        );
    }
}
