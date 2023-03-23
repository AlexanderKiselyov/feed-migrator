package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.telegram.TelegramDataCheck;
import polis.util.SocialMediaGroup;
import polis.util.State;
import polis.util.TelegramChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SyncOkTg extends Command {
    private static final String SYNC_OK_TG = """
            Вы выбрали Телеграм-канал <b>%s</b> и группу <b>%s (%s)</b>.
            Хотите ли Вы синхронизироовать их?""";
    private static final String NOT_VALID_CURRENT_TG_CHANNEL_OR_GROUP = """
            Невозможно свзяать Телеграм-канал и группу.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";
    private final Map<Long, TelegramChannel> currentTgChannel;
    private final Map<Long, SocialMediaGroup> currentSocialMediaGroup;
    private final TelegramDataCheck telegramDataCheck;

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
                    0,
                    commandsForKeyboard,
                    yesNoMarkup());
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
                    0,
                    commandsForKeyboard,
                    null);
        }
    }

    private InlineKeyboardMarkup yesNoMarkup() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        InlineKeyboardButton yes = new InlineKeyboardButton();
        yes.setText("Да");
        yes.setCallbackData("yesNo 0");
        InlineKeyboardButton no = new InlineKeyboardButton();
        no.setText("Нет");
        no.setCallbackData("yesNo 1");
        List<InlineKeyboardButton> buttonsActions = new ArrayList<>();
        buttonsActions.add(yes);
        buttonsActions.add(no);
        buttons.add(buttonsActions);
        inlineKeyboardMarkup.setKeyboard(buttons);
        return inlineKeyboardMarkup;
    }
}
