package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import polis.data_check.DataCheck;
import polis.util.State;

import java.util.Objects;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

@Component
public class Autoposting extends Command {
    private static final String AUTOPOSTING = """
            Функция автопостинга позволяет автоматически публиковать новый пост из Телеграм-канала в группу.
            """;
    private static final String AUTOPOSTING_INLINE = """
            Включить данную функцию для Телеграм-канала <b>%s</b> и группы <b>%s (%s)</b>?""";
    private static final String NO_CURRENT_TG_CHANNEL = """
            Телеграм-канал не был выбран.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";

    @Autowired
    private CurrentChannelRepository currentChannelRepository;

    @Autowired
    private CurrentGroupRepository currentGroupRepository;

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    @Autowired
    private DataCheck dataCheck;

    private static final int rowsCount = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(Autoposting.class);

    public Autoposting() {
        super(State.Autoposting.getIdentifier(), State.Autoposting.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentAccount currentAccount = currentAccountRepository.getCurrentAccount(chat.getId());
        CurrentGroup currentGroup = currentGroupRepository.getCurrentGroup(chat.getId());
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());

        if (currentChannel != null && currentAccount != null && currentGroup != null) {
            String groupName = dataCheck.getOKGroupName(currentGroup.getGroupId(), currentAccount.getAccessToken());

            if (Objects.equals(groupName, "")) {
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
                    AUTOPOSTING,
                    super.rowsCount,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
            String autopostingEnable = "";
            switch (currentGroup.getSocialMedia()) {
                case OK -> autopostingEnable = String.format(AUTOPOSTING_INLINE,
                        currentChannel.getChannelUsername(),
                        groupName,
                        currentGroup.getSocialMedia().getName());
                default -> LOGGER.error(String.format("Social media incorrect: %s", currentGroup.getSocialMedia()));
            }
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    autopostingEnable,
                    rowsCount,
                    commandsForKeyboard,
                    getIfAddAutoposting(chat.getId(), currentChannel.getChannelId()));
        } else {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(NO_CURRENT_TG_CHANNEL, State.MainMenu.getIdentifier()),
                    super.rowsCount,
                    commandsForKeyboard,
                    null,
                    GO_BACK_BUTTON_TEXT);
        }
    }

    private String[] getIfAddAutoposting(long chatId, long channelId) {
        return new String[]{
                "Да",
                String.format("autoposting %d %d 1", chatId, channelId),
                "Нет",
                String.format("autoposting %d %d 0", chatId, channelId)
        };
    }
}
