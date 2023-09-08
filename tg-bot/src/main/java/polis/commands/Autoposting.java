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
import polis.keyboards.InlineKeyboard;
import polis.keyboards.ReplyKeyboard;
import polis.keyboards.callbacks.objects.AutopostingCallback;
import polis.keyboards.callbacks.parsers.AutopostingCallbackParser;
import polis.util.State;

import java.util.List;

@Component
public class Autoposting extends Command {
    private static final String AUTOPOSTING_MSG = """
            Функция автопостинга позволяет автоматически публиковать новый пост из Телеграмм-канала в группу.
            Включить данную функцию для Телеграмм-канала <b>%s</b> и группы <b>%s (%s)</b>?""";
    private static final String NO_CURRENT_TG_CHANNEL_MSG = """
            Телеграмм-канал не был выбран.
            Пожалуйста, вернитесь в главное меню (/%s) и следуйте дальнейшим инструкциям.""";

    @Autowired
    private CurrentChannelRepository currentChannelRepository;

    @Autowired
    private CurrentGroupRepository currentGroupRepository;

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    @Autowired
    private AutopostingCallbackParser autopostingCallbackParser;

    private static final int ROWS_COUNT = 1;
    private static final List<String> KEYBOARD_COMMANDS_IN_ERROR_CASE = List.of(State.MainMenu.getDescription());

    public Autoposting(InlineKeyboard inlineKeyboard, ReplyKeyboard replyKeyboard) {
        super(State.Autoposting.getIdentifier(), State.Autoposting.getDescription(), inlineKeyboard, replyKeyboard);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentAccount currentAccount = currentAccountRepository.getCurrentAccount(chat.getId());
        CurrentGroup currentGroup = currentGroupRepository.getCurrentGroup(chat.getId());
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());

        if (currentChannel != null && currentAccount != null && currentGroup != null) {
            String groupName = currentGroup.getGroupName();
            sendAnswerWithInlineKeyboard(
                    absSender,
                    chat.getId(),
                    String.format(AUTOPOSTING_MSG, currentChannel.getChannelUsername(), groupName,
                            currentGroup.getSocialMedia().getName()),
                    ROWS_COUNT,
                    getButtonsForAutopostingOptions(chat.getId(), currentChannel.getChannelId()),
                    loggingInfo(user.getUserName()));
            return;
        }
        sendAnswerWithReplyKeyboardAndBackButton(
                absSender,
                chat.getId(),
                String.format(NO_CURRENT_TG_CHANNEL_MSG, State.MainMenu.getIdentifier()),
                ROWS_COUNT,
                KEYBOARD_COMMANDS_IN_ERROR_CASE,
                loggingInfo(user.getUserName()));
    }

    private List<String> getButtonsForAutopostingOptions(long chatId, long channelId) {
        return List.of(
                YES_ANSWER,
                autopostingCallbackParser.toText(new AutopostingCallback(chatId, channelId, true)),
                NO_ANSWER,
                autopostingCallbackParser.toText(new AutopostingCallback(chatId, channelId, false))
        );
    }
}
