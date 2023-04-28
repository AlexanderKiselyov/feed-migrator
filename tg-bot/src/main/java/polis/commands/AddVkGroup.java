package polis.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.ChannelGroup;
import polis.data.domain.CurrentChannel;
import polis.data.repositories.ChannelGroupsRepository;
import polis.data.repositories.CurrentChannelRepository;
import polis.util.SocialMedia;
import polis.util.State;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

@Component
public class AddVkGroup extends Command {
    private static final String ADD_VK_GROUP = """
            Чтобы добавить новую группу, введите в чат ссылку на нее.
            Примеры ссылок:
            https://vk.com/lentach
            https://vk.com/club1234567890""";
    static final String SAME_SOCIAL_MEDIA = """
            Социальная сеть %s уже была синхронизирована с текущим Телеграм-каналом.
            Пожалуйста, выберите другую социальную сеть и попробуйте снова.""";

    @Autowired
    private CurrentChannelRepository currentChannelRepository;

    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;

    public AddVkGroup() {
        super(State.AddVkGroup.getIdentifier(), State.AddVkGroup.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());
        if (currentChannel != null) {
            for (ChannelGroup smg : channelGroupsRepository.getGroupsForChannel(currentChannel.getChannelId())) {
                if (smg.getSocialMedia() == SocialMedia.VK) {
                    sendAnswer(absSender,
                            chat.getId(),
                            this.getCommandIdentifier(),
                            user.getUserName(),
                            String.format(SAME_SOCIAL_MEDIA, SocialMedia.VK.getName()),
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
                ADD_VK_GROUP,
                rowsCount,
                commandsForKeyboard,
                null,
                GO_BACK_BUTTON_TEXT);
    }
}
