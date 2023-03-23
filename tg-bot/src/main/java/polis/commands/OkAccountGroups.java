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

public class OkAccountGroups extends Command {
    private static final String OK_ACCOUNT_GROUPS = """
            Список групп OK аккаунта <b>/%s</b>:""";
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
        if (currentSocialMediaAccount.containsKey(chat.getId())
                && currentSocialMediaAccount.get(chat.getId()).getGroupLinks() != null
                && currentSocialMediaAccount.get(chat.getId()).getGroupLinks().size() != 0) {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(OK_ACCOUNT_GROUPS, currentSocialMediaAccount.get(chat.getId()).getUsername()),
                    0,
                    commandsForKeyboard,
                    getGroupsMarkup(currentSocialMediaAccount.get(chat.getId()).getGroupLinks()));
        } else {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(NOT_VALID_SOCIAL_MEDIA_GROUPS_LIST, State.MainMenu.getIdentifier()),
                    rowsCount,
                    commandsForKeyboard,
                    null);
        }
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
