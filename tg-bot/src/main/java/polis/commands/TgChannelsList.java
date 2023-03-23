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
    private static final String TG_CHANNELS_LIST = """
            Список добавленных Телеграм-каналов.
            Для выбора определенного Телеграм-канала - нажмите на нужный канал.
            Для удаления Телеграм-канала - нажмите 'Удалить' справа от канала.""";
    private static final String NO_TG_CHANNELS = """
            Список добавленных Телеграм-каналов пуст.
            Пожалуйста, вернитесь в главное меню (/%s) и добавьте хотя бы один канал.""";
    private final Map<Long, List<TelegramChannel>> tgChannels;
    private final TelegramDataCheck telegramDataCheck;
    private static final int rowsCount = 1;
    private static final List<String> commandsForKeyboard = List.of(
            State.MainMenu.getDescription()
    );

    public TgChannelsList(String commandIdentifier, String description, Map<Long, List<TelegramChannel>> tgChannels) {
        super(commandIdentifier, description);
        this.tgChannels = tgChannels;
        telegramDataCheck = new TelegramDataCheck();
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (tgChannels.containsKey(chat.getId()) && tgChannels.get(chat.getId()).size() != 0) {
            sendAnswer(absSender, chat.getId(), this.getCommandIdentifier(), user.getUserName(), TG_CHANNELS_LIST,
                    rowsCount, commandsForKeyboard, null);
            sendAnswer(absSender, chat.getId(), this.getCommandIdentifier(), user.getUserName(), TG_CHANNELS_LIST,
                    0, commandsForKeyboard, getUserChannelsArray(tgChannels.get(chat.getId())));
        } else {
            sendAnswer(absSender, chat.getId(), this.getCommandIdentifier(), user.getUserName(),
                    String.format(NO_TG_CHANNELS, State.MainMenu.getIdentifier()), rowsCount, commandsForKeyboard,
                    null);
        }
    }

    private String[] getUserChannelsArray(List<TelegramChannel> channelIds) {
        String[] buttons = new String[channelIds.size() * 4];
        for (int i = 0; i < channelIds.size(); i++) {
            int tmpIndex = i * 4;
            buttons[tmpIndex] = telegramDataCheck.getChatTitle(channelIds.get(i).getTelegramChannelId());
            buttons[tmpIndex + 1] = String.format("tg_channel %s %d", channelIds.get(i), 0);
            buttons[tmpIndex + 2] = "\uD83D\uDDD1 Удалить";
            buttons[tmpIndex + 3] = String.format("tg_channel %s %d", channelIds.get(i), 1);
        }

        return buttons;
    }
}
