package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.telegram.TelegramDataCheck;
import polis.util.State;
import polis.util.TelegramChannel;

import java.util.List;
import java.util.Map;

public class TgChannelsList extends Command {
    private static final String TG_CHANNELS_LIST = "Список добавленных Телеграм-каналов.";
    private static final String TG_CHANNELS_LIST_INLINE = """
            Нажмите на Телеграм-канал, чтобы выбрать определенный.
            Для удаления Телеграм-канала нажмите 'Удалить' справа от канала.""";
    private static final String NO_TG_CHANNELS = """
            Список добавленных Телеграм-каналов пуст.
            Пожалуйста, добавьте хотя бы один канал.""";
    private final Map<Long, List<TelegramChannel>> tgChannels;
    private final TelegramDataCheck telegramDataCheck;
    private static final int rowsCount = 2;
    private static final List<String> commandsForKeyboard = List.of(
            State.AddTgChannel.getDescription(),
            State.MainMenu.getDescription()
    );

    public TgChannelsList(String commandIdentifier, String description, Map<Long, List<TelegramChannel>> tgChannels) {
        super(commandIdentifier, description);
        this.tgChannels = tgChannels;
        telegramDataCheck = new TelegramDataCheck();
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        List<TelegramChannel> channels = tgChannels.get(chat.getId());
        if (tgChannels.containsKey(chat.getId()) && channels.size() != 0) {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    TG_CHANNELS_LIST,
                    rowsCount,
                    commandsForKeyboard,
                    null);
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    TG_CHANNELS_LIST_INLINE,
                    channels.size(),
                    commandsForKeyboard,
                    getUserTgChannelsArray(channels));
        } else {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    NO_TG_CHANNELS,
                    rowsCount,
                    commandsForKeyboard,
                    null);
        }
    }

    private String[] getUserTgChannelsArray(List<TelegramChannel> channels) {
        String[] buttons = new String[channels.size() * 4];
        for (int i = 0; i < channels.size(); i++) {
            int tmpIndex = i * 4;
            String telegramChannelUsername = channels.get(i).getTelegramChannelUsername();
            Long telegramChannelId = channels.get(i).getTelegramChannelId();
            buttons[tmpIndex] = String.valueOf(telegramDataCheck.getChatParameter(telegramChannelUsername, "title"));
            buttons[tmpIndex + 1] = String.format("tg_channel %s %d", telegramChannelId, 0);
            buttons[tmpIndex + 2] = "\uD83D\uDDD1 Удалить";
            buttons[tmpIndex + 3] = String.format("tg_channel %s %d", telegramChannelId, 1);
        }

        return buttons;
    }
}
