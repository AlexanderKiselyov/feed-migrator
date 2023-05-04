package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.Account;
import polis.data.repositories.AccountsRepository;
import polis.datacheck.OkDataCheck;
import polis.datacheck.VkDataCheck;
import polis.util.SocialMedia;
import polis.util.State;
import polis.vk.api.VkAuthorizator;

import java.util.List;
import java.util.Objects;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

@Component
public class AccountsList extends Command {
    private static final String ACCOUNTS_LIST = """
            Список аккаунтов:""";
    private static final String ACCOUNTS_LIST_INLINE = "Чтобы выбрать аккаунт, нажмите на соответствующую кнопку.";
    private static final String NOT_VALID_SOCIAL_MEDIA_ACCOUNTS_LIST = """
            Список аккаунтов пустой.
            Пожалуйста, вернитесь в меню добавления группы (/%s) и следуйте дальнейшим инструкциям.""";
    private static final String trashEmoji = "\uD83D\uDDD1";
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountsList.class);

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private OkDataCheck okDataCheck;

    @Autowired
    private VkDataCheck vkDataCheck;

    public AccountsList() {
        super(State.AccountsList.getIdentifier(), State.AccountsList.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        List<Account> accounts = accountsRepository.getAccountsForUser(chat.getId());

        if (accounts != null && !accounts.isEmpty()) {
            for (Account account : accounts) {
                if (Objects.equals(okDataCheck.getOKUsername(account.getAccessToken()), null)
                        && Objects.equals(vkDataCheck.getVkUsername(
                        new VkAuthorizator.TokenWithId(account.getAccessToken(),
                                (int) account.getAccountId())), null)) {
                    sendAnswer(
                            absSender,
                            chat.getId(),
                            this.getCommandIdentifier(),
                            user.getUserName(),
                            USERNAME_NOT_FOUND,
                            1,
                            List.of(State.AddGroup.getDescription()),
                            null,
                            GO_BACK_BUTTON_TEXT);
                    return;
                }
            }

            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    ACCOUNTS_LIST,
                    ROWS_COUNT,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    ACCOUNTS_LIST_INLINE,
                    accounts.size(),
                    commandsForKeyboard,
                    getButtonsForAccounts(accounts));
        } else {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(NOT_VALID_SOCIAL_MEDIA_ACCOUNTS_LIST, State.AddGroup.getIdentifier()),
                    1,
                    List.of(State.AddGroup.getDescription()),
                    null,
                    GO_BACK_BUTTON_TEXT);
        }
    }

    private String[] getButtonsForAccounts(List<Account> socialMediaAccounts) {
        String[] buttons = new String[socialMediaAccounts.size() * 4];
        for (int i = 0; i < socialMediaAccounts.size(); i++) {
            int tmpIndex = i * 4;
            Account tmpAccount = socialMediaAccounts.get(i);
            SocialMedia tmpAccountSocialMedia = tmpAccount.getSocialMedia();
            String accountUsername = null;
            String socialMediaName = tmpAccountSocialMedia.getName();
            switch (tmpAccountSocialMedia) {
                case OK -> accountUsername = okDataCheck.getOKUsername(tmpAccount.getAccessToken());
                case VK -> accountUsername = vkDataCheck.getVkUsername(
                        new VkAuthorizator.TokenWithId(
                                tmpAccount.getAccessToken(),
                                (int) tmpAccount.getAccountId()
                        )
                );
                default -> LOGGER.error(String.format("Unknown state: %s", socialMediaName));
            }

            if (accountUsername == null) {
                LOGGER.error(String.format("Error detecting account username of account: %s",
                        tmpAccount.getAccountId()));
                // TODO здесь бы сделать continue и уменьшить размер массива кнопок и accounts.size() (который
                //  отправляем для inline-клавиатуры)
            }

            long tmpAccountId = tmpAccount.getAccountId();

            buttons[tmpIndex] = String.format("%s (%s)", accountUsername, socialMediaName);
            buttons[tmpIndex + 1] = String.format("account %d 0 %s", tmpAccountId, socialMediaName);
            buttons[tmpIndex + 2] = trashEmoji + " Удалить";
            buttons[tmpIndex + 3] = String.format("account %d 1 %s", tmpAccountId, socialMediaName);
        }

        return buttons;
    }
}
