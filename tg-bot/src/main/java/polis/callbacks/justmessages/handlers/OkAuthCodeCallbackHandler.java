package polis.callbacks.justmessages.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.callbacks.justmessages.MessageCallbackHandler;
import polis.callbacks.UtilCallbackHandler;
import polis.callbacks.justmessages.SomeMessage;
import polis.commands.context.Context;
import polis.data.domain.Account;
import polis.data.repositories.AccountsRepository;
import polis.datacheck.OkDataCheck;
import polis.ok.api.OkAuthorizator;
import polis.ok.api.exceptions.CodeExpiredException;
import polis.ok.api.exceptions.OkApiException;
import polis.util.IState;
import polis.util.SocialMedia;
import polis.util.State;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@Component
public class OkAuthCodeCallbackHandler extends UtilCallbackHandler<SomeMessage> implements MessageCallbackHandler {

    public static final String OK_AUTH_STATE_ANSWER = String.format("""
                    Вы были успешно авторизованы в социальной сети Одноклассники.
                    Вы можете посмотреть информацию по аккаунту, если введете команду /%s.""",
            State.OkAccountDescription.getIdentifier());

    private static final List<String> KEYBOARD_BUTTONS = List.of(State.OkAccountDescription.getDescription());

    @Autowired
    private OkAuthorizator okAuthorizator;
    @Autowired
    private OkDataCheck okDataCheck;
    @Autowired
    private AccountsRepository accountsRepository;

    @Override
    public void handleCallback(long chatId, Message message, SomeMessage callback, Context context) throws TelegramApiException {
        String okAuthCode = callback.text;
        OkAuthorizator.TokenPair pair;
        try {
            pair = okAuthorizator.getToken(okAuthCode);
        } catch (IOException | URISyntaxException e) {
            return;
            //TODO LOG
        } catch (CodeExpiredException e) {
            //TODO LOG
            return;
        } catch (OkApiException e) {
            //TODO LOG
            return;
        }
        Long userId = okDataCheck.getOKUserId(pair.accessToken());
        if (userId == null) {
            //TODO LOG
            return;
        }
        if (accountsRepository.getUserAccount(chatId, userId, SocialMedia.OK.getName()) != null) {
            //TODO LOG
            return;
        }
        String username = okDataCheck.getOKUsername(pair.accessToken());
        if (username == null || username.isEmpty()) {
            //TODO LOG
            return;
        }
        Account newAccount = new Account(
                chatId,
                SocialMedia.OK.getName(),
                userId,
                username,
                pair.accessToken(),
                pair.refreshToken()
        );
        context.setCurrentAccount(newAccount);
        accountsRepository.insertNewAccount(newAccount);

        sendAnswer(chatId, username, OK_AUTH_STATE_ANSWER, KEYBOARD_BUTTONS);
    }

    @Override
    public IState state() {
        return State.AddOkAccount;
    }
}
