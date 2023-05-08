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
    private static final int ROWS_COUNT = 2;
    private static final List<String> commandsForKeyboard = List.of(
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
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    TG_CHANNELS_LIST_MSG,
                    TG_CHANNELS_LIST_INLINE_MSG,
                    channels.size(),
                    getUserTgChannelsArray(channels));
        } else {
            sendAnswerWithReplyKeyboard(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    NO_TG_CHANNELS,
                    ROWS_COUNT,
                    commandsForKeyboard);
        }
    }

    private List<String> getUserTgChannelsArray(List<UserChannels> channels) {
        List<String> buttons = new ArrayList<>(channels.size() * 4);
        for (UserChannels channel : channels) {
            String telegramChannelUsername = channel.getChannelUsername();
            Long telegramChannelId = channel.getChannelId();
            buttons.add(telegramChannelUsername);
            buttons.add(String.format("tg_channel %s %d", telegramChannelId, 0));
            buttons.add(Emojis.TRASH + " Удалить");
            buttons.add(String.format("tg_channel %s %d", telegramChannelId, 1));
        }
        return buttons;
    }
}
