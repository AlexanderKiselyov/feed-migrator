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
public class Autoposting extends Command {
    private static final String AUTOPOSTING_MSG = """
            Функция автопостинга позволяет автоматически публиковать новый пост из Телеграм-канала в группу.""";
    private static final String AUTOPOSTING_INLINE_MSG = """
            Включить данную функцию для Телеграм-канала <b>%s</b> и группы <b>%s (%s)</b>?""";
    private static final String NO_CURRENT_TG_CHANNEL_MSG = """
            Телеграм-канал не был выбран.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";

    @Autowired
    private CurrentChannelRepository currentChannelRepository;

    @Autowired
    private CurrentGroupRepository currentGroupRepository;

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    private static final int ROWS_COUNT = 1;
    private static final List<String> commandsForKeyboardInErrorCase = List.of(State.MainMenu.getDescription());

    public Autoposting() {
        super(State.Autoposting.getIdentifier(), State.Autoposting.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentAccount currentAccount = currentAccountRepository.getCurrentAccount(chat.getId());
        CurrentGroup currentGroup = currentGroupRepository.getCurrentGroup(chat.getId());
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());

        if (currentChannel != null && currentAccount != null && currentGroup != null) {
            String groupName = currentGroup.getGroupName();
            sendAnswerWithInlineKeyboardAndBackButton(
                    absSender,
                    chat.getId(),
                    AUTOPOSTING_MSG,
                    String.format(AUTOPOSTING_INLINE_MSG, currentChannel.getChannelUsername(), groupName,
                            currentGroup.getSocialMedia().getName()),
                    ROWS_COUNT,
                    getButtonsForAutopostingOptions(chat.getId(), currentChannel.getChannelId()),
                    loggingInfo(user.getUserName()));
            return;
        }
        sendAnswerWithReplyKeyboardAndBackButton(
                absSender,
                chat.getId(),
                String.format(NO_CURRENT_TG_CHANNEL_MSG, State.MainMenu.getIdentifier()),
                ROWS_COUNT,
                commandsForKeyboardInErrorCase,
                loggingInfo(user.getUserName()));
    }

    private List<String> getButtonsForAutopostingOptions(long chatId, long channelId) {
        return List.of(
                "Да",
                String.format("autoposting %d %d 0", chatId, channelId),
                "Нет",
                String.format("autoposting %d %d 1", chatId, channelId)
        );
    }
}
