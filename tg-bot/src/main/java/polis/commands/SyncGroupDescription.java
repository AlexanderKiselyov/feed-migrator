package polis.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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

@Component
public class SyncGroupDescription extends Command {
    private static final String SYNC_OK_TG_DESCRIPTION = """
            Телеграм-канал <b>%s</b> и группа <b>%s (%s)</b> были успешно синхронизированы.
            Настроить функцию автопостинга можно по команде /%s.""";
    private static final String NOT_VALID_CURRENT_TG_CHANNEL_OR_GROUP_DESCRIPTION = """
            Невозможно показать информацию по связанным Телеграм-каналу и группе.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";
    private static final int ROWS_COUNT = 1;
    private static final List<String> commandsForKeyboard = List.of(State.Autoposting.getDescription());
    private static final List<String> commandsForKeyboardInErrorCase = List.of(State.MainMenu.getDescription());

    @Autowired
    private CurrentChannelRepository currentChannelRepository;

    @Autowired
    private CurrentGroupRepository currentGroupRepository;

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    public SyncGroupDescription() {
        super(State.SyncGroupDescription.getIdentifier(), State.SyncGroupDescription.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentAccount currentAccount = currentAccountRepository.getCurrentAccount(chat.getId());
        CurrentGroup currentGroup = currentGroupRepository.getCurrentGroup(chat.getId());
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());

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
                noErrorCondition ? commandsForKeyboard : commandsForKeyboardInErrorCase,
                loggingInfo(user.getUserName()));
    }
}
