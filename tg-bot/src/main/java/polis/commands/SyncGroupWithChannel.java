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

import static polis.commands.CommandUtils.getButtonsForSyncOptions;

public abstract class SyncGroupWithChannel extends Command {
    static final String SYNC_MSG = """
            Вы выбрали Телеграм-канал <b>%s</b> и группу <b>%s (%s)</b>.""";
    static final String SYNC_INLINE_MSG = """
            Хотите ли Вы синхронизировать их?
                        
            *При размещении контента на Вашем канале очень важно уважать права других авторов, в связи с чем мы не
            осуществляем автопостинг для пересланных сообщений 🙂""";
    static final String NOT_VALID_CURRENT_TG_CHANNEL_OR_GROUP = """
            Невозможно связать Телеграм-канал и группу.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";
    static final int ROWS_COUNT = 1;
    static final List<String> commandsForKeyboardInErrorCase = List.of(State.MainMenu.getDescription());

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
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(
                            SYNC_MSG,
                            currentChannel.getChannelUsername(),
                            groupName,
                            currentGroup.getSocialMedia().getName()
                    ),
                    SYNC_INLINE_MSG,
                    ROWS_COUNT,
                    getButtonsForSyncOptions());
            return;
        }
        sendAnswerWithReplyKeyboardAndBackButton(
                absSender,
                chat.getId(),
                this.getCommandIdentifier(),
                user.getUserName(),
                String.format(
                        NOT_VALID_CURRENT_TG_CHANNEL_OR_GROUP,
                        State.MainMenu.getIdentifier()
                ),
                ROWS_COUNT,
                commandsForKeyboardInErrorCase);
    }
}
