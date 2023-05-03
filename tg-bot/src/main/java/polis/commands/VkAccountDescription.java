package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.CurrentAccount;
import polis.data.repositories.CurrentAccountRepository;
import polis.datacheck.VkDataCheck;
import polis.util.State;
import polis.vk.api.VkAuthorizator;

import java.util.List;
import java.util.Objects;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

@Component
public class VkAccountDescription extends Command {
    private static final String ACCOUNT_DESCRIPTION = """
            Выбран аккаунт в социальной сети ВКонтакте с названием <b>%s</b>.""";
    private static final String NOT_VALID_ACCOUNT = """
            Невозможно получить информацию по текущему аккаунту.
            Пожалуйста, вернитесь в меню добавления группы (/%s) и следуйте дальнейшим инструкциям.""";
    private static final Logger LOGGER = LoggerFactory.getLogger(VkAccountDescription.class);

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    @Autowired
    private VkDataCheck vkDataCheck;

    private static final int ROWS_COUNT = 1;
    private static final List<String> commandsForKeyboard = List.of(
            State.AddVkGroup.getDescription()
    );

    public VkAccountDescription() {
        super(State.VkAccountDescription.getIdentifier(), State.VkAccountDescription.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentAccount currentAccount = currentAccountRepository.getCurrentAccount(chat.getId());

        if (currentAccount != null) {
            String username = vkDataCheck.getVkUsername(new VkAuthorizator.TokenWithId(currentAccount.getAccessToken(),
                    (int) currentAccount.getAccountId()));

            if (Objects.equals(username, null)) {
                sendAnswer(absSender,
                        chat.getId(),
                        this.getCommandIdentifier(),
                        user.getUserName(),
                        USERNAME_NOT_FOUND,
                        ROWS_COUNT,
                        commandsForKeyboard,
                        null,
                        GO_BACK_BUTTON_TEXT);
                LOGGER.error(String.format("Error detecting username of account: %d", currentAccount.getAccountId()));
                return;
            }

            sendAnswer(absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(ACCOUNT_DESCRIPTION, username),
                    ROWS_COUNT,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        } else {
            sendAnswer(absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(NOT_VALID_ACCOUNT, State.AddGroup.getIdentifier()),
                    ROWS_COUNT,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        }
    }
}
