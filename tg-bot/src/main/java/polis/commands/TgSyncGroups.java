package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.util.SocialMediaGroup;
import polis.util.State;
import polis.util.TelegramChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class TgSyncGroups extends Command {
    private static final String TG_SYNC_GROUPS = """
            Список синхронизованных групп.
            Для выбора определенной группы - нажмите на нужную группу.
            Для удаления группы - нажмите 'Удалить' справа от группы.""";
    private static final String NO_SYNC_GROUPS = """
            Список синхронизованных групп пуст.
            Пожалуйста, вернитесь в описание телеграмм канала (/%s) и добавьте хотя бы одну группу.""";
    private final Map<Long, TelegramChannel> currentTgChannel;

    public TgSyncGroups(String commandIdentifier, String description, Map<Long, TelegramChannel> currentTgChannel) {
        super(commandIdentifier, description);
        this.currentTgChannel = currentTgChannel;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (currentTgChannel.get(chat.getId()) != null && currentTgChannel.get(chat.getId()).getGroups() != null
                && currentTgChannel.get(chat.getId()).getGroups().size() != 0) {
            sendAnswer(absSender, chat.getId(), this.getCommandIdentifier(), user.getUserName(), TG_SYNC_GROUPS,
                    rowsCount, commandsForKeyboard,
                    getTgChannelGroupsMarkup(currentTgChannel.get(chat.getId()).getGroups()));
            sendAnswer(absSender, chat.getId(), this.getCommandIdentifier(), user.getUserName(), TG_SYNC_GROUPS,
                    rowsCount, commandsForKeyboard,null, GO_BACK_BUTTON_TEXT);
        } else {
            sendAnswer(absSender, chat.getId(), this.getCommandIdentifier(), user.getUserName(),
                    String.format(NO_SYNC_GROUPS, State.TgChannelDescription.getIdentifier()), rowsCount,
                    commandsForKeyboard, null, GO_BACK_BUTTON_TEXT);
        }
    }

    private InlineKeyboardMarkup getTgChannelGroupsMarkup(List<SocialMediaGroup> groups) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> channelsList = new ArrayList<>();
        for (SocialMediaGroup socialMediaGroup : groups) {
            InlineKeyboardButton channel = new InlineKeyboardButton();
            channel.setText(socialMediaGroup.getName());
            channel.setCallbackData(String.format("group %s %d", socialMediaGroup.getId(), 0));
            InlineKeyboardButton deleteChannel = new InlineKeyboardButton();
            deleteChannel.setText("Удалить");
            deleteChannel.setCallbackData(String.format("group %s %d", socialMediaGroup.getId(), 1));
            List<InlineKeyboardButton> channelActions = new ArrayList<>();
            channelActions.add(channel);
            channelActions.add(deleteChannel);
            channelsList.add(channelActions);
        }
        inlineKeyboardMarkup.setKeyboard(channelsList);
        return inlineKeyboardMarkup;
    }
}
