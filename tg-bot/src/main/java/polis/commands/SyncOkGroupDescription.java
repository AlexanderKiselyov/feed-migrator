package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.ok.OKDataCheck;
import polis.telegram.TelegramDataCheck;
import polis.util.AuthData;
import polis.util.SocialMediaGroup;
import polis.util.State;
import polis.util.TelegramChannel;

import java.util.List;
import java.util.Map;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class SyncOkGroupDescription extends Command {
    private static final String SYNC_OK_TG_DESCRIPTION = """
            Телеграм-канал <b>%s</b> и группа <b>%s (%s)</b> были успешно синхронизированы.
            Настроить функцию автопостинга можно по команде /%s.""";
    private static final String NOT_VALID_CURRENT_TG_CHANNEL_OR_GROUP_DESCRIPTION = """
            Невозможно показать информацию по связанным Телеграм-каналу и группе.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";
    private final Map<Long, TelegramChannel> currentTgChannel;
    private final Map<Long, SocialMediaGroup> currentSocialMediaGroup;
    private final TelegramDataCheck telegramDataCheck;
    private final Map<Long, AuthData> currentSocialMediaAccount;
    private final OKDataCheck okDataCheck;
    private final int rowsCount = 1;
    private final List<String> commandsForKeyboard = List.of(
            State.Autoposting.getDescription()
    );

    public SyncOkGroupDescription(String commandIdentifier,
                                  String description,
                                  Map<Long, TelegramChannel> currentTgChannel,
                                  Map<Long, SocialMediaGroup> currentSocialMediaGroup,
                                  Map<Long, AuthData> currentSocialMediaAccount,
                                  OKDataCheck okDataCheck) {
        super(commandIdentifier, description);
        this.currentTgChannel = currentTgChannel;
        this.currentSocialMediaGroup = currentSocialMediaGroup;
        this.currentSocialMediaAccount = currentSocialMediaAccount;
        this.okDataCheck = okDataCheck;
        telegramDataCheck = new TelegramDataCheck();
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (currentTgChannel.containsKey(chat.getId()) && currentSocialMediaGroup.containsKey(chat.getId())) {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(
                            SYNC_OK_TG_DESCRIPTION,
                            telegramDataCheck.getChatParameter(
                                    currentTgChannel.get(chat.getId()).getTelegramChannelUsername(), "title"
                            ),
                            okDataCheck.getOKGroupName(currentSocialMediaGroup.get(chat.getId()).getId(),
                                    currentSocialMediaAccount.get(chat.getId()).getAccessToken()),
                            currentSocialMediaGroup.get(chat.getId()).getSocialMedia().getName(),
                            State.Autoposting.getIdentifier()
                    ),
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
                    String.format(
                            NOT_VALID_CURRENT_TG_CHANNEL_OR_GROUP_DESCRIPTION,
                            State.MainMenu.getIdentifier()
                    ),
                    super.rowsCount,
                    super.commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        }
    }
}
