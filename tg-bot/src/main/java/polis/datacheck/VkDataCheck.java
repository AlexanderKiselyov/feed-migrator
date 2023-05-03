package polis.datacheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import polis.commands.NonCommand;
import polis.data.domain.Account;
import polis.data.domain.CurrentAccount;
import polis.data.domain.CurrentState;
import polis.data.repositories.AccountsRepository;
import polis.data.repositories.CurrentAccountRepository;
import polis.data.repositories.CurrentStateRepository;
import polis.util.SocialMedia;
import polis.util.State;
import polis.util.Substate;
import polis.vk.api.VkApiMethods;
import polis.vk.api.VkAuthorizator;
import polis.vk.api.exceptions.VkApiException;

import static polis.commands.Command.USERNAME_NOT_FOUND;

@Component
public class VkDataCheck {
    public static final String VK_AUTH_STATE_SERVER_EXCEPTION_ANSWER = "Ошибка на сервере. Попробуйте еще раз.";
    public static final String VK_AUTH_STATE_ANSWER = """
            Вы были успешно авторизованы в социальной сети ВКонтакте.
            Вы можете посмотреть информацию по аккаунту, если введете команду /%s.""";
    private static final Logger LOGGER = LoggerFactory.getLogger(VkDataCheck.class);
    private final VkAuthorizator vkAuthorizator = new VkAuthorizator();
    private final VkApiMethods vkApiMethods = new VkApiMethods();

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private CurrentStateRepository currentStateRepository;

    public NonCommand.AnswerPair getVkAuthCode(String text, Long chatId) {
        try {
            VkAuthorizator.TokenWithId tokenWithId = vkAuthorizator.getToken(text);

            String username = getVkUsername(tokenWithId);

            if (username == null) {
                return new NonCommand.AnswerPair(USERNAME_NOT_FOUND, true);
            }

            Account newAccount = new Account(
                    chatId,
                    SocialMedia.VK.getName(),
                    tokenWithId.userId(),
                    username,
                    tokenWithId.accessToken(),
                    "" // FIXME подумать над отсутствующим refresh_token в ВК
            );

            currentAccountRepository.insertCurrentAccount(
                    new CurrentAccount(
                            chatId,
                            newAccount.getSocialMedia().getName(),
                            newAccount.getAccountId(),
                            newAccount.getUserFullName(),
                            newAccount.getAccessToken(),
                            newAccount.getRefreshToken()
                    )
            );

            accountsRepository.insertNewAccount(newAccount);

            currentStateRepository.insertCurrentState(new CurrentState(chatId,
                    Substate.nextSubstate(State.VkAccountDescription).getIdentifier()));

            return new NonCommand.AnswerPair(
                    String.format(VK_AUTH_STATE_ANSWER, State.VkAccountDescription.getIdentifier()),
                    false);
        } catch (VkApiException e) {
            LOGGER.error(String.format("Unknown error: %s", e.getMessage()));
            return new NonCommand.AnswerPair(VK_AUTH_STATE_SERVER_EXCEPTION_ANSWER, true);
        }
    }

    public Boolean getIsVkGroupAdmin(VkAuthorizator.TokenWithId tokenWithId, String groupId) {
        try {
            return vkApiMethods.getIsVkGroupAdmin(tokenWithId, groupId);
        } catch (VkApiException e) {
            LOGGER.error(String.format("Unknown error: %s", e.getMessage()));
            return null;
        }
    }

    public String getVkUsername(VkAuthorizator.TokenWithId tokenWithId) {
        try {
            return vkApiMethods.getVkUsername(tokenWithId);
        } catch (VkApiException e) {
            LOGGER.error(String.format("Unknown error: %s", e.getMessage()));
            return null;
        }
    }

    public Integer getVkGroupId(VkAuthorizator.TokenWithId tokenWithId, String groupLink) {
        try {
            return vkApiMethods.getVkGroupId(tokenWithId, groupLink);
        } catch (VkApiException e) {
            LOGGER.error(String.format("Unknown error: %s", e.getMessage()));
            return null;
        }
    }

    public String getVkGroupName(VkAuthorizator.TokenWithId tokenWithId, Long groupId) {
        try {
            return vkApiMethods.getVkGroupName(tokenWithId, groupId);
        } catch (VkApiException e) {
            LOGGER.error(String.format("Unknown error: %s", e.getMessage()));
            return null;
        }
    }
}
