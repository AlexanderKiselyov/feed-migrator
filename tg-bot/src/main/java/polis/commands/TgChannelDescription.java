package polis.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.CurrentChannel;
import polis.data.repositories.CurrentChannelRepository;
import polis.telegram.TelegramDataCheck;
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

    @Autowired
    private CurrentChannelRepository currentChannelRepository;

    private final TelegramDataCheck telegramDataCheck;
    private static final int rowsCount = 3;
    private static final List<String> commandsForKeyboard = List.of(
            State.TgSyncGroups.getDescription(),
            State.AddGroup.getDescription(),
            State.MainMenu.getDescription()
    );

    public TgChannelDescription() {
        super(State.TgChannelDescription.getIdentifier(), State.TgChannelDescription.getDescription());
        telegramDataCheck = new TelegramDataCheck();
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());
        if (currentChannel != null) {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(TELEGRAM_CHANNEL_DESCRIPTION,
                            telegramDataCheck.getChatParameter(currentChannel.getChannelUsername(), "title"),
                            State.TgSyncGroups.getIdentifier(),
                            State.AddGroup.getIdentifier()
                    ),
                    rowsCount,
                    commandsForKeyboard,
                    null);
        } else {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    NOT_VALID_CHANNEL,
                    rowsCount,
                    commandsForKeyboard,
                    null);
        }
    }
}
