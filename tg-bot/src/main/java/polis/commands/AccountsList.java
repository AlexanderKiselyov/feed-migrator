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

public class AccountsList extends Command {
    private static final String ACCOUNTS_LIST = """
            Список аккаунтов:""";
    private static final String NOT_VALID_SOCIAL_MEDIA_ACCOUNTS_LIST = """
            Список аккаунтов пустой.
            Пожалуйста, вернитесь в меню добавления группы (/%s) и следуйте дальнейшим инструкциям.""";
    private final Map<Long, List<AuthData>> socialMediaAccounts;

    public AccountsList(String commandIdentifier, String description, Map<Long, List<AuthData>> socialMediaAccounts) {
        super(commandIdentifier, description);
        this.socialMediaAccounts = socialMediaAccounts;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (socialMediaAccounts.containsKey(chat.getId()) && socialMediaAccounts.get(chat.getId()).size() != 0) {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    ACCOUNTS_LIST,
                    0,
                    commandsForKeyboard,
                    getAccountsMarkup(socialMediaAccounts.get(chat.getId())));
        } else {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(NOT_VALID_SOCIAL_MEDIA_ACCOUNTS_LIST, State.AddGroup.getIdentifier()),
                    rowsCount,
                    commandsForKeyboard,
                    null);
        }
    }

    private InlineKeyboardMarkup getAccountsMarkup(List<AuthData> socialMediaAccounts) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> channelsList = new ArrayList<>();
        for (AuthData socialMediaAccount : socialMediaAccounts) {
            InlineKeyboardButton channel = new InlineKeyboardButton();
            channel.setText(String.format("%s (%s)", socialMediaAccount.getUsername(),
                    socialMediaAccount.getSocialMedia().getName()));
            channel.setCallbackData(String.format("account %s %s %s",
                    socialMediaAccount.getSocialMedia().getName(),
                    socialMediaAccount.getAccessToken(),
                    socialMediaAccount.getUsername()));
            List<InlineKeyboardButton> accountActions = new ArrayList<>();
            accountActions.add(channel);
            channelsList.add(accountActions);
        }
        inlineKeyboardMarkup.setKeyboard(channelsList);
        return inlineKeyboardMarkup;
    }
}
