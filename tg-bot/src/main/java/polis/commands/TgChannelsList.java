package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.telegram.TelegramDataCheck;
import polis.util.State;
import polis.util.TelegramChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TgChannelsList extends Command {
    private static final String TG_CHANNELS_LIST = "Список добавленных Телеграм-каналов.";
    private static final String TG_CHANNELS_LIST_INLINE = """
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
                    null, null);
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    TG_CHANNELS_LIST_INLINE,
                    channels.size(),
                    commandsForKeyboard, null,
                    getUserChannelsMarkup(channels)); //
        } else {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(NO_TG_CHANNELS, State.MainMenu.getIdentifier()),
                    rowsCount,
                    commandsForKeyboard, null,
                    null);
        }
    }

    // TODO: рефакторинг и перенос функционала inline-клавиатуры в класс InlineKeyboard в процессе
    private String[] getUserChannelsArray(List<TelegramChannel> channels) {
        String[] buttons = new String[channels.size() * 4];
        for (int i = 0; i < channels.size(); i++) {
            int tmpIndex = i * 4;
            buttons[tmpIndex] = telegramDataCheck.getChatTitle(channels.get(i).getTelegramChannelId());
            buttons[tmpIndex + 1] = String.format("tg_channel %s %d", channels.get(i), 0);
            buttons[tmpIndex + 2] = "\uD83D\uDDD1 Удалить";
            buttons[tmpIndex + 3] = String.format("tg_channel %s %d", channels.get(i), 1);
        }

        return buttons;
    }

    // TODO: переделаю это под метод из InlineKeyboard.java, а вызов будет в Command.java
    private InlineKeyboardMarkup getUserChannelsMarkup(List<TelegramChannel> channels) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> channelsList = new ArrayList<>();
        for (TelegramChannel channel : channels) {
            InlineKeyboardButton channelButton = new InlineKeyboardButton();
            channelButton.setText(telegramDataCheck.getChatTitle(channel.getTelegramChannelId()));
            channelButton.setCallbackData(String.format("tg_channel %s %d", channel.getTelegramChannelId(), 0));
            InlineKeyboardButton deleteChannel = new InlineKeyboardButton();
            deleteChannel.setText("Удалить");
            deleteChannel.setCallbackData(String.format("tg_channel %s %d", channel.getTelegramChannelId(), 1));
            List<InlineKeyboardButton> channelActions = new ArrayList<>();
            channelActions.add(channelButton);
            channelActions.add(deleteChannel);
            channelsList.add(channelActions);
        }
        inlineKeyboardMarkup.setKeyboard(channelsList);
        return inlineKeyboardMarkup;
    }
}
