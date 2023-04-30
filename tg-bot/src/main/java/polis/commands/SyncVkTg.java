package polis.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.CurrentAccount;
import polis.data.domain.CurrentChannel;
import polis.data.domain.CurrentGroup;
import polis.data.repositories.CurrentAccountRepository;
import polis.data.repositories.CurrentChannelRepository;
import polis.data.repositories.CurrentGroupRepository;
import polis.datacheck.VkDataCheck;
import polis.telegram.TelegramDataCheck;
import polis.util.State;
import polis.vk.api.VkAuthorizator;

import java.util.Objects;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

@Component
public class SyncVkTg extends Command {
    private static final String SYNC_VK_TG = """
            Вы выбрали Телеграм-канал <b>%s</b> и группу <b>%s (%s)</b>.""";
    private static final String SYNC_VK_TG_INLINE = """
            Хотите ли Вы синхронизировать их?""";
    private static final String NOT_VALID_CURRENT_TG_CHANNEL_OR_GROUP = """
            Невозможно связать Телеграм-канал и группу.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";

    @Autowired
    private CurrentChannelRepository currentChannelRepository;

    @Autowired
    private CurrentGroupRepository currentGroupRepository;

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    @Autowired
    private VkDataCheck vkDataCheck;

    private final TelegramDataCheck telegramDataCheck;
    private static final int rowsCount = 1;

    public SyncVkTg() {
        super(State.SyncVkTg.getIdentifier(), State.SyncVkTg.getDescription());
        telegramDataCheck = new TelegramDataCheck();
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentAccount currentAccount = currentAccountRepository.getCurrentAccount(chat.getId());
        CurrentGroup currentGroup = currentGroupRepository.getCurrentGroup(chat.getId());
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());
        if (currentChannel != null && currentGroup != null && currentAccount != null) {
            String groupName = vkDataCheck.getVkGroupName(new VkAuthorizator.TokenWithId(currentGroup.getAccessToken(),
                    (int) currentGroup.getAccountId()), String.valueOf(currentGroup.getGroupId()));

            if (Objects.equals(groupName, null)) {
                sendAnswer(
                        absSender,
                        chat.getId(),
                        this.getCommandIdentifier(),
                        user.getUserName(),
                        GROUP_NAME_NOT_FOUND,
                        super.rowsCount,
                        commandsForKeyboard,
                        null,
                        GO_BACK_BUTTON_TEXT);
                return;
            }

            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(
                            SYNC_VK_TG,
                            telegramDataCheck.getChatParameter(currentChannel.getChannelUsername(), "title"),
                            groupName,
                            currentGroup.getSocialMedia().getName()
                    ),
                    super.rowsCount,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    SYNC_VK_TG_INLINE,
                    rowsCount,
                    commandsForKeyboard,
                    yesNoList());
        } else {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(
                            NOT_VALID_CURRENT_TG_CHANNEL_OR_GROUP,
                            State.MainMenu.getIdentifier()
                    ),
                    super.rowsCount,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        }
    }

    private String[] yesNoList() {
        return new String[] {
                "Да",
                "yesNo 0",
                "Нет",
                "yesNo 1"
        };
    }
}
