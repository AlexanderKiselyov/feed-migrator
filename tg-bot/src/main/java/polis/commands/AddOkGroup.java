package polis.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.data.domain.ChannelGroup;
import polis.data.domain.CurrentChannel;
import polis.data.repositories.ChannelGroupsRepositoryImpl;
import polis.data.repositories.CurrentChannelRepository;
import polis.util.SocialMedia;

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
    private final CurrentChannelRepository currentChannelRepository;
    private final ChannelGroupsRepositoryImpl channelGroupsRepository;

    public AddOkGroup(String commandIdentifier, String description, CurrentChannelRepository currentChannelRepository,
                      ChannelGroupsRepositoryImpl channelGroupsRepository) {
        super(commandIdentifier, description);
        this.currentChannelRepository = currentChannelRepository;
        this.channelGroupsRepository = channelGroupsRepository;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());
        if (currentChannel != null) {
            for (ChannelGroup smg : channelGroupsRepository.getGroupsForChannel(currentChannel.getChannelId())) {
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
