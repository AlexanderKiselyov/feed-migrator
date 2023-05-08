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
public class TgChannelDescription extends Command {
    private static final String TELEGRAM_CHANNEL_DESCRIPTION = """
            Текущий выбранный Телеграм-канал <b>%s</b>.
            Вы можете посмотреть синхронизированные с каналом группы по команде /%s.
            Добавить новую группу можно по команде /%s.""";
    private static final String NOT_VALID_CHANNEL = String.format("""
                    Телеграм-канал не был выбран.
                    Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""",
            State.MainMenu.getIdentifier());
    private static final int ROWS_COUNT = 3;
    private static final int ROWS_COUNT_IN_ERROR_CASE = 1;
    private static final List<String> commandsForKeyboard = List.of(
            State.TgSyncGroups.getDescription(),
            State.AddGroup.getDescription(),
            State.MainMenu.getDescription()
    );
    private static final List<String> commandsForKeyboardInErrorCase = List.of(State.MainMenu.getDescription());

    @Autowired
    private CurrentChannelRepository currentChannelRepository;

    public TgChannelDescription() {
        super(State.TgChannelDescription.getIdentifier(), State.TgChannelDescription.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());
        boolean noErrorCondition = currentChannel != null;
        String text = noErrorCondition ? String.format(TELEGRAM_CHANNEL_DESCRIPTION,
                currentChannel.getChannelUsername(), State.TgSyncGroups.getIdentifier(), State.AddGroup.getIdentifier())
                : NOT_VALID_CHANNEL;

        sendAnswerWithReplyKeyboard(
                absSender,
                chat.getId(),
                this.getCommandIdentifier(),
                user.getUserName(),
                text,
                noErrorCondition ? ROWS_COUNT : ROWS_COUNT_IN_ERROR_CASE,
                noErrorCondition ? commandsForKeyboard : commandsForKeyboardInErrorCase);
    }
}
