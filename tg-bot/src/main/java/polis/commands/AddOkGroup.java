package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.util.SocialMedia;
import polis.util.SocialMediaGroup;
import polis.util.TelegramChannel;

import java.util.Map;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

public class AddOkGroup extends Command {
    private static final String ADD_OK_GROUP = """
            Чтобы добавить новую группу, введите в чат ссылку на нее.
            Примеры ссылок:
            https://ok.ru/ok
            https://ok.ru/group57212027273260""";
    static final String SAME_SOCIAL_MEDIA = """
            Социальная сеть %s уже была синхронизирована с текущим Телеграм-каналом.
            Пожалуйста, выберите другую социальную сеть и попробуйте снова.""";
    private final Map<Long, TelegramChannel> currentTgChannel;

    public AddOkGroup(String commandIdentifier, String description, Map<Long, TelegramChannel> currentTgChannel) {
        super(commandIdentifier, description);
        this.currentTgChannel = currentTgChannel;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (currentTgChannel.get(chat.getId()) != null) {
            for (SocialMediaGroup smg : currentTgChannel.get(chat.getId()).getSynchronizedGroups()) {
                if (smg.getSocialMedia() == SocialMedia.OK) {
                    sendAnswer(absSender,
                            chat.getId(),
                            this.getCommandIdentifier(),
                            user.getUserName(),
                            String.format(SAME_SOCIAL_MEDIA, SocialMedia.OK.getName()),
                            rowsCount,
                            commandsForKeyboard,
                            null,
                            GO_BACK_BUTTON_TEXT);
                    return;
                }
            }
        }
        sendAnswer(absSender,
                chat.getId(),
                this.getCommandIdentifier(),
                user.getUserName(),
                ADD_OK_GROUP,
                rowsCount,
                commandsForKeyboard,
                null,
                GO_BACK_BUTTON_TEXT);
    }
}
