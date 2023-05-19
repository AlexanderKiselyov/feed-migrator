package polis.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.CurrentAccount;
import polis.data.domain.CurrentChannel;
import polis.data.domain.CurrentGroup;
import polis.data.repositories.CurrentAccountRepository;
import polis.data.repositories.CurrentChannelRepository;
import polis.data.repositories.CurrentGroupRepository;
import polis.util.State;

import java.util.List;

import static polis.util.Emojis.EIGHTEEN_PLUS;
import static polis.util.Emojis.HAPPY_FACE;
import static polis.util.Emojis.STOP_PROFANITY;

public abstract class SyncGroupWithChannel extends Command {
    private static final String SYNC_MSG = """
            Вы выбрали Телеграмм-канал <b>%s</b> и группу <b>%s (%s)</b>.""";
    private static final String SYNC_INLINE_MSG = String.format("""
            Хотите ли Вы синхронизировать их?
                        
            <b>* При размещении контента на Вашем канале очень важно уважать права других авторов, в связи с чем автопостинг для пересланных сообщений не осуществляется %s
            * Запрещается публиковать контент, нарущающий законодательство РФ (ненормативная лексика, контент 18+ и др.) %s %s</b>""",
            HAPPY_FACE, STOP_PROFANITY, EIGHTEEN_PLUS);
    private static final String NOT_VALID_CURRENT_TG_CHANNEL_OR_GROUP = """
            Невозможно связать Телеграмм-канал и группу.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";
    private static final String ENABLE_SYNC = "yesNo 0";
    private static final String DISABLE_SYNC = "yesNo 1";
    private static final int ROWS_COUNT = 1;
    private static final List<String> KEYBOARD_COMMANDS_IN_ERROR_CASE = List.of(State.MainMenu.getDescription());

    @Autowired
    private CurrentChannelRepository currentChannelRepository;

    @Autowired
    private CurrentGroupRepository currentGroupRepository;

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    public SyncGroupWithChannel(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentAccount currentAccount = currentAccountRepository.getCurrentAccount(chat.getId());
        CurrentGroup currentGroup = currentGroupRepository.getCurrentGroup(chat.getId());
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());
        if (currentChannel != null && currentGroup != null && currentAccount != null) {
            String groupName = currentGroup.getGroupName();
            sendAnswerWithInlineKeyboardAndBackButton(
                    absSender,
                    chat.getId(),
                    String.format(
                            SYNC_MSG,
                            currentChannel.getChannelUsername(),
                            groupName,
                            currentGroup.getSocialMedia().getName()
                    ),
                    SYNC_INLINE_MSG,
                    ROWS_COUNT,
                    getButtonsForSyncOptions(),
                    loggingInfo(user.getUserName()));
            return;
        }
        sendAnswerWithReplyKeyboardAndBackButton(
                absSender,
                chat.getId(),
                String.format(
                        NOT_VALID_CURRENT_TG_CHANNEL_OR_GROUP,
                        State.MainMenu.getIdentifier()
                ),
                ROWS_COUNT,
                KEYBOARD_COMMANDS_IN_ERROR_CASE,
                loggingInfo(user.getUserName()));
    }

    private static List<String> getButtonsForSyncOptions() {
        return List.of(
                YES_ANSWER,
                ENABLE_SYNC,
                NO_ANSWER,
                DISABLE_SYNC
        );
    }
}
