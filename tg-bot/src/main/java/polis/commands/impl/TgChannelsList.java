package polis.commands.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.commands.Command;
import polis.commands.DescribableCommand;
import polis.data.domain.UserChannels;
import polis.data.repositories.UserChannelsRepository;
import polis.keyboards.InlineKeyboard;
import polis.keyboards.ReplyKeyboard;
import polis.keyboards.callbacks.CallbackType;
import polis.keyboards.callbacks.objects.TgChannelCallback;
import polis.keyboards.callbacks.parsers.TgChannelCallbackParser;
import polis.util.Emojis;
import polis.util.IState;
import polis.util.State;

import java.util.ArrayList;
import java.util.List;

@Component
public class TgChannelsList extends Command implements DescribableCommand {
    private static final String TG_CHANNELS_LIST_MSG = """
            Список добавленных Телеграмм-каналов.
            Нажмите на Телеграмм-канал, чтобы выбрать определенный.
            Для удаления Телеграмм-канала нажмите 'Удалить' справа от канала.""";
    private static final String NO_TG_CHANNELS = """
            Список добавленных Телеграмм-каналов пуст.
            Пожалуйста, добавьте хотя бы один канал.""";
    private static final int ROWS_COUNT = 2;
    private static final List<String> KEYBOARD_COMMANDS = List.of(
            State.AddTgChannel.getDescription(),
            State.MainMenu.getDescription()
    );

    @Autowired
    private UserChannelsRepository userChannelsRepository;

    @Autowired
    private TgChannelCallbackParser tgChannelCallbackParser;

    public TgChannelsList(InlineKeyboard inlineKeyboard, ReplyKeyboard replyKeyboard) {
        super(State.TgChannelsList.getIdentifier(), State.TgChannelsList.getDescription(), inlineKeyboard, replyKeyboard);
    }

    @Override
    public String helloMessage() {
        return "";
    }

    @Override
    public List<IState> nextPossibleCommands() {
        return TRANSITION_WITH_CALLBACK;
    }

    @Override
    public CallbackType callbackType() {
        return CallbackType.TG_CHANNEL_CHOSEN;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        List<UserChannels> channels = userChannelsRepository.getUserChannels(chat.getId());
        if (channels != null && !channels.isEmpty()) {
            sendAnswerWithInlineKeyboard(
                    absSender,
                    chat.getId(),
                    TG_CHANNELS_LIST_MSG,
                    channels.size(),
                    getUserTgChannelsArray(channels),
                    loggingInfo(user.getUserName()));
        } else {
            sendAnswerWithReplyKeyboard(
                    absSender,
                    chat.getId(),
                    NO_TG_CHANNELS,
                    ROWS_COUNT,
                    KEYBOARD_COMMANDS,
                    loggingInfo(user.getUserName()));
        }
    }

    private List<String> getUserTgChannelsArray(List<UserChannels> channels) {
        List<String> buttons = new ArrayList<>(channels.size() * 4);
        for (UserChannels channel : channels) {
            String telegramChannelUsername = channel.getChannelUsername();
            long telegramChannelId = channel.getChannelId();
            buttons.add(telegramChannelUsername);
            buttons.add(tgChannelCallbackParser.toText(new TgChannelCallback(telegramChannelId, false)));
            buttons.add(Emojis.TRASH + DELETE_MESSAGE);
            buttons.add(tgChannelCallbackParser.toText(new TgChannelCallback(telegramChannelId, true)));
        }
        return buttons;
    }
}
