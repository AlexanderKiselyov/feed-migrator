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
        TelegramChannel telegramChannel = currentTgChannel.get(chat.getId());
        if (telegramChannel != null && telegramChannel.getSynchronizedGroups() != null
                && telegramChannel.getSynchronizedGroups().size() != 0) {
            List<SocialMediaGroup> synchronizedGroups = telegramChannel.getSynchronizedGroups();
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    TG_SYNC_GROUPS,
                    synchronizedGroups.size(),
                    commandsForKeyboard, null,
                    getTgChannelGroupsMarkup(synchronizedGroups));
            sendAnswer(absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    TG_SYNC_GROUPS,
                    rowsCount,
                    commandsForKeyboard,
                    null,null,
                    GO_BACK_BUTTON_TEXT);
        } else {
            sendAnswer(
                    absSender,
                    chat.getId(),
                    this.getCommandIdentifier(),
                    user.getUserName(),
                    String.format(NO_SYNC_GROUPS, State.TgChannelDescription.getIdentifier()),
                    rowsCount,
                    commandsForKeyboard,
                    null,null,
                    GO_BACK_BUTTON_TEXT);
        }
    }

    // TODO: рефакторинг и перенос функционала inline-клавиатуры в класс InlineKeyboard в процессе
    private String[] getTgChannelGroupsArray(List<SocialMediaGroup> groups) {
        String[] buttons = new String[groups.size() * 4];
        for (int i = 0; i < groups.size(); i++) {
            int tmpIndex = i * 4;
            buttons[tmpIndex] = String.format("%s (%s)", groups.get(i).getName(),
                    groups.get(i).getSocialMedia().getName());
            buttons[tmpIndex + 1] = String.format("group %s %d", groups.get(i).getId(), 0);
            buttons[tmpIndex + 2] = "\uD83D\uDDD1 Удалить";
            buttons[tmpIndex + 3] = String.format("group %s %d", groups.get(i).getId(), 1);
        }

        return buttons;
    }

    private InlineKeyboardMarkup getTgChannelGroupsMarkup(List<SocialMediaGroup> groups) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> channelsList = new ArrayList<>();
        for (SocialMediaGroup socialMediaGroup : groups) {
            InlineKeyboardButton channel = new InlineKeyboardButton();
            channel.setText(String.format("%s (%s)", socialMediaGroup.getName(),
                    socialMediaGroup.getSocialMedia().getName()));
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
