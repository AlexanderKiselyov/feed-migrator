package polis.keyboards.callbacks.handlers.replykeyboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.commands.context.Context;
import polis.data.domain.Account;
import polis.data.repositories.AccountsRepository;
import polis.datacheck.OkDataCheck;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.handlers.ReplyKeyboardCallbackHandler;
import polis.keyboards.callbacks.handlers.UtilHandler;
import polis.keyboards.callbacks.objects.ReplyKeyboardCallback;
import polis.ok.api.OkAuthorizator;
import polis.ok.api.exceptions.CodeExpiredException;
import polis.ok.api.exceptions.OkApiException;
import polis.util.IState;
import polis.util.SocialMedia;
import polis.util.State;

import java.io.IOException;
import java.net.URISyntaxException;

@Component
public class OkAuthCodeCallbackHandler extends UtilHandler<ReplyKeyboardCallback> implements ReplyKeyboardCallbackHandler {
    @Autowired
    private OkAuthorizator okAuthorizator;
    @Autowired
    private OkDataCheck okDataCheck;
    @Autowired
    private AccountsRepository accountsRepository;

    @Override
    public void handleCallback(long chatId, Message message, ReplyKeyboardCallback callback, Context context) throws TelegramApiException {
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
        context.resetCurrentAccount(newAccount);
        accountsRepository.insertNewAccount(newAccount);
        processNextCommand(State.AccountsList, null, message, null);
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.REPLY_KEYBOARD_MESSAGE;
    }

    @Override
    public IState state() {
        return State.AddOkAccount;
    }
}
