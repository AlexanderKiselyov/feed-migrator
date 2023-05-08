package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.util.State;
import polis.vk.api.VkAuthorizator;

import java.net.URISyntaxException;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class AddVkAccount extends Command {
    private static final String VK_AUTH_ANSWER = """
                    Для авторизации в социальной сети ВКонтакте перейдите по ссылке:
                    %s
                    После авторизации скопируйте код авторизации (все символы после "code=") из адресной строки
                    или всю ссылку целиком и отправьте в этот диалог.""";
    private static final Logger LOGGER = LoggerFactory.getLogger(AddVkAccount.class);

    public AddVkAccount() {
        super(State.AddVkAccount.getIdentifier(), State.AddVkAccount.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        try {
            String messageText = String.format(VK_AUTH_ANSWER, VkAuthorizator.formAuthorizationUrl());
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    messageText,
                    ROWS_COUNT,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        } catch (URISyntaxException e) {
            LOGGER.error(String.format("Cannot form link: %s", e));
        }
    }
}
