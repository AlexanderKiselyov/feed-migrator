package polis.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.UserChannels;
import polis.data.repositories.UserChannelsRepository;
import polis.util.Emojis;
import polis.util.State;

import java.util.ArrayList;
import java.util.List;

@Component
public class TgChannelsList extends Command {
    private static final String TG_CHANNELS_LIST_MSG = """
            Список добавленных Телеграм-каналов.""";
    private static final String TG_CHANNELS_LIST_INLINE_MSG = """
            Нажмите на Телеграм-канал, чтобы выбрать определенный.
            Для удаления Телеграм-канала нажмите 'Удалить' справа от канала.""";
    private static final String NO_TG_CHANNELS = """
            Список добавленных Телеграм-каналов пуст.
            Пожалуйста, добавьте хотя бы один канал.""";
    private static final String GET_TELEGRAM_CHANNEL = "tg_channel %s %d";
    private static final String DELETE_TELEGRAM_CHANNEL = "tg_channel %s %d";
    private static final int ROWS_COUNT = 2;
    private static final List<String> KEYBOARD_COMMANDS = List.of(
            State.AddTgChannel.getDescription(),
            State.MainMenu.getDescription()
    );

    @Autowired
    private UserChannelsRepository userChannelsRepository;

    public TgChannelsList() {
        super(State.TgChannelsList.getIdentifier(), State.TgChannelsList.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        List<UserChannels> channels = userChannelsRepository.getUserChannels(chat.getId());
        if (channels != null && !channels.isEmpty()) {
            sendAnswerWithInlineKeyboardAndBackButton(
                    absSender,
                    chat.getId(),
                    TG_CHANNELS_LIST_MSG,
                    TG_CHANNELS_LIST_INLINE_MSG,
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

    private static List<String> getUserTgChannelsArray(List<UserChannels> channels) {
        List<String> buttons = new ArrayList<>(channels.size() * 4);
        for (UserChannels channel : channels) {
            String telegramChannelUsername = channel.getChannelUsername();
            Long telegramChannelId = channel.getChannelId();
            buttons.add(telegramChannelUsername);
            buttons.add(String.format(GET_TELEGRAM_CHANNEL, telegramChannelId, 0));
            buttons.add(Emojis.TRASH + DELETE_MESSAGE);
            buttons.add(String.format(DELETE_TELEGRAM_CHANNEL, telegramChannelId, 1));
        }
        return buttons;
    }
}
