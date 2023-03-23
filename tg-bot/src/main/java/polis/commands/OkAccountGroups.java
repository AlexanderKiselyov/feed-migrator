package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.util.AuthData;
import polis.util.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class OkAccountGroups extends Command {
    // TODO: Добавить текст, чтобы отправлять два сообщения (для двух разных клавиатур)
    private static final String OK_ACCOUNT_GROUPS = """
            Список групп OK аккаунта <b>/%s</b>:""";
    private static final String OK_ACCOUNT_GROUPS_INLINE = "TODO сообщение";
    private static final String NOT_VALID_SOCIAL_MEDIA_GROUPS_LIST = """
            Список групп ОК аккаунта пустой.
            Пожалуйста, вернитесь в меню добавления группы (/%s) и следуйте дальнейшим инструкциям.""";
    private final Map<Long, AuthData> currentSocialMediaAccount;

    public OkAccountGroups(String commandIdentifier, String description,
                           Map<Long, AuthData> currentSocialMediaAccount) {
        super(commandIdentifier, description);
        this.currentSocialMediaAccount = currentSocialMediaAccount;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        List<String> groupLinks = currentSocialMediaAccount.get(chat.getId()).getGroupLinks();
        if (currentSocialMediaAccount.containsKey(chat.getId()) && groupLinks != null && groupLinks.size() != 0) {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(OK_ACCOUNT_GROUPS, currentSocialMediaAccount.get(chat.getId()).getUsername()),
                    rowsCount,
                    commandsForKeyboard,
                    null,null,
                    GO_BACK_BUTTON_TEXT);
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    OK_ACCOUNT_GROUPS_INLINE,
                    groupLinks.size(),
                    commandsForKeyboard, null,
                    getGroupsMarkup(groupLinks));
        } else {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(NOT_VALID_SOCIAL_MEDIA_GROUPS_LIST, State.MainMenu.getIdentifier()),
                    rowsCount,
                    commandsForKeyboard,
                    null,null,
                    GO_BACK_BUTTON_TEXT);
        }
    }

    // TODO: рефакторинг и перенос функционала inline-клавиатуры в класс InlineKeyboard в процессе
    private String[] getGroupsArray(List<String> groupLinks) {
        String[] buttons = new String[groupLinks.size()];
        for (int i = 0; i < groupLinks.size(); i++) {
            buttons[i] = groupLinks.get(i);
        }

        return buttons;
    }

    private InlineKeyboardMarkup getGroupsMarkup(List<String> groupLinks) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> channelsList = new ArrayList<>();
        for (String groupLink : groupLinks) {
            InlineKeyboardButton channel = new InlineKeyboardButton();
            channel.setText(groupLink);
            List<InlineKeyboardButton> groupActions = new ArrayList<>();
            groupActions.add(channel);
            channelsList.add(groupActions);
        }
        inlineKeyboardMarkup.setKeyboard(channelsList);
        return inlineKeyboardMarkup;
    }
}
