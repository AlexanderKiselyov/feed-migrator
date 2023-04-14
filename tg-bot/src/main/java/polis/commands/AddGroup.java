package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.CurrentChannel;
import polis.data.repositories.CurrentChannelRepository;
import polis.telegram.TelegramDataCheck;
import polis.util.State;

import java.util.List;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class AddGroup extends Command {
    private static final String ADD_GROUP = """
            Меню добавления групп для Телеграм-канала <b>%s</b>.""";
    private static final String NOT_VALID_TG_CHANNEL = """
            Невозможно получить информацию по текущему телеграм-каналу.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";
    private final TelegramDataCheck telegramDataCheck;
    private final CurrentChannelRepository currentChannelRepository;
    private static final int rowsCount = 2;
    private static final List<String> commandsForKeyboard = List.of(
            State.AccountsList.getDescription(),
            State.AddOkAccount.getDescription()
    );

    public AddGroup(String commandIdentifier, String description, CurrentChannelRepository currentChannelRepository) {
        super(commandIdentifier, description);
        this.currentChannelRepository = currentChannelRepository;
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
                    String.format(ADD_GROUP,
                            telegramDataCheck.getChatParameter(currentChannel.getChannelUsername(), "title")),
                    rowsCount,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        } else {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(NOT_VALID_TG_CHANNEL, State.MainMenu.getIdentifier()),
                    rowsCount,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        }
    }
}
