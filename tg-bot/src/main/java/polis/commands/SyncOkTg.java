package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.telegram.TelegramDataCheck;
import polis.util.SocialMediaGroup;
import polis.util.State;
import polis.util.TelegramChannel;

import java.util.Map;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class SyncOkTg extends Command {
    private static final String SYNC_OK_TG = """
            Вы выбрали Телеграм-канал <b>%s</b> и группу <b>%s (%s)</b>.""";
    private static final String SYNC_OK_TG_INLINE = """
            Хотите ли Вы синхронизироовать их?""";
    private static final String NOT_VALID_CURRENT_TG_CHANNEL_OR_GROUP = """
            Невозможно связать Телеграм-канал и группу.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";
    private final Map<Long, TelegramChannel> currentTgChannel;
    private final Map<Long, SocialMediaGroup> currentSocialMediaGroup;
    private final TelegramDataCheck telegramDataCheck;
    private static final int rowsCount = 1;

    public SyncOkTg(String commandIdentifier, String description, Map<Long, TelegramChannel> currentTgChannel,
                    Map<Long, SocialMediaGroup> currentSocialMediaGroup) {
        super(commandIdentifier, description);
        this.currentTgChannel = currentTgChannel;
        this.currentSocialMediaGroup = currentSocialMediaGroup;
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
                            SYNC_OK_TG,
                            telegramDataCheck.getChatTitle(currentTgChannel.get(chat.getId()).getTelegramChannelId()),
                            currentSocialMediaGroup.get(chat.getId()).getName(),
                            currentSocialMediaGroup.get(chat.getId()).getSocialMedia().getName()
                    ),
                    super.rowsCount,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(
                            SYNC_OK_TG_INLINE,
                            telegramDataCheck.getChatTitle(currentTgChannel.get(chat.getId()).getTelegramChannelId()),
                            currentSocialMediaGroup.get(chat.getId()).getName(),
                            currentSocialMediaGroup.get(chat.getId()).getSocialMedia().getName()
                    ),
                    rowsCount,
                    commandsForKeyboard,
                    yesNoList());
        } else {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(
                            NOT_VALID_CURRENT_TG_CHANNEL_OR_GROUP,
                            State.MainMenu.getIdentifier()
                    ),
                    super.rowsCount,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        }
    }

    private String[] yesNoList() {
        return new String[] {
                "Да",
                "yesNo 0",
                "Нет",
                "yesNo 1"
        };
    }
}