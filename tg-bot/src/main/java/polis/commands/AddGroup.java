package polis.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.CurrentChannel;
import polis.data.repositories.CurrentChannelRepository;
import polis.util.State;

import java.util.List;

@Component
public class AddGroup extends Command {
    private static final String ADD_GROUP_MSG = """
            Меню добавления групп для Телеграм-канала <b>%s</b>.""";
    private static final String NOT_VALID_TG_CHANNEL_MSG = """
            Невозможно получить информацию по текущему телеграм-каналу.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";
    private static final int ROWS_COUNT = 2;
    private static final List<String> commandsForKeyboard = List.of(
            State.AccountsList.getDescription(),
            State.AddOkAccount.getDescription(),
            State.AddVkAccount.getDescription()
    );

    @Autowired
    private CurrentChannelRepository currentChannelRepository;

    public AddGroup() {
        super(State.AddGroup.getIdentifier(), State.AddGroup.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());
        if (currentChannel != null) {
            sendAnswerWithReplyKeyboardAndBackButton(
                    absSender,
                    chat.getId(),
                    String.format(ADD_GROUP_MSG, currentChannel.getChannelUsername()),
                    ROWS_COUNT,
                    commandsForKeyboard,
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
