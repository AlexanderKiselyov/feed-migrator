package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.CurrentAccount;
import polis.data.domain.CurrentChannel;
import polis.data.domain.CurrentGroup;
import polis.data.repositories.CurrentAccountRepository;
import polis.data.repositories.CurrentChannelRepository;
import polis.data.repositories.CurrentGroupRepository;
import polis.ok.OKDataCheck;
import polis.util.State;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class Autoposting extends Command {
    private static final String AUTOPOSTING = """
            Функция автопостинга позволяет автоматически публиковать новый пост из Телеграм-канала в группу.
            """;
    private static final String AUTOPOSTING_INLINE = """
            Включить данную функцию для Телеграм-канала <b>%s</b> и группы <b>%s (%s)</b>?""";
    private static final String NO_CURRENT_TG_CHANNEL = """
            Телеграм-канал не был выбран.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";
    private final CurrentChannelRepository currentChannelRepository;
    private final CurrentGroupRepository currentGroupRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final OKDataCheck okDataCheck;
    private static final int rowsCount = 1;
    private final Logger logger = LoggerFactory.getLogger(Autoposting.class);

    public Autoposting(String commandIdentifier, String description, CurrentChannelRepository currentChannelRepository,
                       CurrentGroupRepository currentGroupRepository,
                       CurrentAccountRepository currentAccountRepository,
                       OKDataCheck okDataCheck) {
        super(commandIdentifier, description);
        this.currentChannelRepository = currentChannelRepository;
        this.currentGroupRepository = currentGroupRepository;
        this.currentAccountRepository = currentAccountRepository;
        this.okDataCheck = okDataCheck;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentAccount currentAccount = currentAccountRepository.getCurrentAccount(chat.getId());
        CurrentGroup currentGroup = currentGroupRepository.getCurrentGroup(chat.getId());
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());
        if (currentChannel != null && currentAccount != null) {
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
                        okDataCheck.getOKGroupName(currentGroup.getGroupId(), currentAccount.getAccessToken()),
                        currentGroup.getSocialMedia().getName());
                default -> logger.error(String.format("Social media incorrect: %s", currentGroup.getSocialMedia()));
            }
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    autopostingEnable,
                    rowsCount,
                    commandsForKeyboard,
                    getIfAddAutoposting(currentChannel.getChannelId()));
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

    private String[] getIfAddAutoposting(Long id) {
        return new String[]{
                "Да",
                String.format("autoposting %s 0", id),
                "Нет",
                String.format("autoposting %s 1", id)
        };
    }
}
