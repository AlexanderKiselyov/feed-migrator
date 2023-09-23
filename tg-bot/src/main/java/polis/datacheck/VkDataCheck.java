package polis.datacheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import polis.commands.NonCommand;
import polis.commands.context.Context;
import polis.commands.context.ContextStorage;
import polis.data.domain.Account;
import polis.data.repositories.AccountsRepository;
import polis.util.SocialMedia;
import polis.util.State;
import polis.vk.api.VkApiMethods;
import polis.vk.api.VkAuthorizator;
import polis.vk.api.exceptions.VkApiException;

import static polis.commands.Command.USERNAME_NOT_FOUND;

@Component
public class VkDataCheck {
    public static final String VK_AUTH_STATE_SERVER_EXCEPTION_ANSWER = """
            Невозможно выполнить авторизацию в социальной сети ВКонтакте.
            Пожалуйста, проверьте данные авторизации и попробуйте еще раз.""";

    public static final String VK_AUTH_STATE_ANSWER = """
            Вы были успешно авторизованы в социальной сети ВКонтакте.
            Вы можете посмотреть информацию по аккаунту, если введете команду /%s.""";
    public static final String SAME_VK_ACCOUNT = "Данный аккаунт в социальной сети ВКонтакте уже был добавлен.";
    private static final String INVALID_LINK = """
            Введена неверная ссылка.
            Пожалуйста, скопируйте ссылку из адресной строки и введите ее еще раз.""";
    private static final String ACCESS_CODE = "access_token=";
    private static final String USER_ID = "user_id=";
    private static final String VK_SOCIAL_NAME = SocialMedia.VK.getName();
    private static final Logger LOGGER = LoggerFactory.getLogger(VkDataCheck.class);
    private final VkApiMethods vkApiMethods = new VkApiMethods();

    @Autowired
    private AccountsRepository accountsRepository;
    @Autowired
    private ContextStorage contextStorage;

    public NonCommand.AnswerPair getVkAccessToken(String text, Long chatId) {
        String accessToken;
        int userId;

        if (text.contains(ACCESS_CODE) && text.contains(USER_ID)) {
            int accessTokenStartIndex = text.indexOf(ACCESS_CODE) + ACCESS_CODE.length();
            int accessTokenEndIndex = text.indexOf("&", accessTokenStartIndex);

            int userIdStartIndex = text.indexOf(USER_ID) + USER_ID.length();
            int userIdEndIndex = text.indexOf("&", userIdStartIndex);

            accessToken = text.substring(accessTokenStartIndex,
                    accessTokenEndIndex == -1 ? text.length() : accessTokenEndIndex);
            userId = Integer.parseInt(text.substring(userIdStartIndex,
                    userIdEndIndex == -1 ? text.length() : userIdEndIndex));
        } else {
            return new NonCommand.AnswerPair(INVALID_LINK, true);
        }

        VkAuthorizator.TokenWithId tokenWithId = new VkAuthorizator.TokenWithId(accessToken, userId);

        if (accountsRepository.getUserAccount(chatId, userId, VK_SOCIAL_NAME) != null) {
            return new NonCommand.AnswerPair(SAME_VK_ACCOUNT, true);
        }

        String username = getVkUsername(tokenWithId);

        if (username == null) {
            return new NonCommand.AnswerPair(USERNAME_NOT_FOUND, true);
        }

        Account newAccount = new Account(
                chatId,
                VK_SOCIAL_NAME,
                userId,
                username,
                accessToken,
                ""
        );

        Context context = contextStorage.getContext(chatId);
        context.resetCurrentAccount(new Account(
                chatId,
                newAccount.getSocialMedia().getName(),
                newAccount.getAccountId(),
                newAccount.getUserFullName(),
                newAccount.getAccessToken(),
                newAccount.getRefreshToken()
        ));

        accountsRepository.insertNewAccount(newAccount);

        return new NonCommand.AnswerPair(
                String.format(VK_AUTH_STATE_ANSWER, State.VkAccountDescription.getIdentifier()),
                false);
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

    public String getVkGroupName(VkAuthorizator.TokenWithId tokenWithId, Integer groupId) {
        try {
            return vkApiMethods.getVkGroupName(tokenWithId, groupId);
        } catch (VkApiException e) {
            LOGGER.error(String.format("Unknown error: %s", e.getMessage()));
            return null;
        }
    }
}
