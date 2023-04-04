package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.ok.OKDataCheck;
import polis.util.AuthData;
import polis.util.SocialMediaGroup;
import polis.util.State;
import polis.util.TelegramChannel;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class TgSyncGroups extends Command {
    private static final String TG_SYNC_GROUPS = """
            Список синхронизированных групп.""";
    private static final String TG_SYNC_GROUPS_INLINE = """
            Для выбора определенной группы нажмите на нужную группу.
            Для удаления группы нажмите 'Удалить' справа от группы.""";
    private static final String NO_SYNC_GROUPS = """
            Список синхронизированных групп пуст.
            Пожалуйста, вернитесь в описание Телеграм-канала (/%s) и добавьте хотя бы одну группу.""";
    private final Map<Long, TelegramChannel> currentTgChannel;
    private final Map<Long, List<AuthData>> socialMediaAccounts;
    private final OKDataCheck okDataCheck;
    private final Logger logger = LoggerFactory.getLogger(TgSyncGroups.class);

    public TgSyncGroups(String commandIdentifier, String description, Map<Long, TelegramChannel> currentTgChannel,
                        Map<Long, List<AuthData>> socialMediaAccounts,
                        OKDataCheck okDataCheck) {
        super(commandIdentifier, description);
        this.currentTgChannel = currentTgChannel;
        this.socialMediaAccounts = socialMediaAccounts;
        this.okDataCheck = okDataCheck;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        TelegramChannel telegramChannel = currentTgChannel.get(chat.getId());
        if (telegramChannel != null && telegramChannel.getSynchronizedGroups() != null
                && telegramChannel.getSynchronizedGroups().size() != 0
                && socialMediaAccounts.get(chat.getId()) != null) {
            List<SocialMediaGroup> synchronizedGroups = telegramChannel.getSynchronizedGroups();
            sendAnswer(absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    TG_SYNC_GROUPS,
                    rowsCount,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    TG_SYNC_GROUPS_INLINE,
                    synchronizedGroups.size(),
                    commandsForKeyboard,
                    getTgChannelGroupsArray(synchronizedGroups, socialMediaAccounts.get(chat.getId())));
        } else {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(NO_SYNC_GROUPS, State.TgChannelDescription.getIdentifier()),
                    1,
                    List.of(State.TgChannelDescription.getDescription()),
                    null,
                    GO_BACK_BUTTON_TEXT);
        }
    }

    private String[] getTgChannelGroupsArray(List<SocialMediaGroup> groups, List<AuthData> socialMediaAccounts) {
        String[] buttons = new String[groups.size() * 6];
        for (int i = 0; i < groups.size(); i++) {
            int tmpIndex = i * 6;
            String groupName = null;
            switch (groups.get(i).getSocialMedia()) {
                case OK -> {
                    for (AuthData socialMediaAccount : socialMediaAccounts) {
                        if (Objects.equals(socialMediaAccount.getTokenId(), groups.get(i).getTokenId())) {
                            groupName = okDataCheck.getOKGroupName(groups.get(i).getId(),
                                    socialMediaAccount.getAccessToken());
                            break;
                        }
                    }
                }
                default -> logger.error(String.format("Social media not found: %s", groups.get(i).getSocialMedia()));
            }
            if (groupName != null) {
                buttons[tmpIndex] = String.format("%s (%s)", groupName,
                        groups.get(i).getSocialMedia().getName());
                buttons[tmpIndex + 1] = String.format("group %s %d", groups.get(i).getId(), 0);
                buttons[tmpIndex + 2] = "\uD83D\uDD04 Автопостинг";
                buttons[tmpIndex + 3] = String.format("group %s %d", groups.get(i).getId(), 2);
                buttons[tmpIndex + 4] = "\uD83D\uDDD1 Удалить";
                buttons[tmpIndex + 5] = String.format("group %s %d", groups.get(i).getId(), 1);
            }
        }

        return buttons;
    }
}
