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

@Component
public class AddOkGroup extends Command {
    private static final String ADD_OK_GROUP_MSG = """
            Чтобы добавить новую группу, введите в чат ссылку на нее.
            Примеры ссылок:
            https://ok.ru/ok
            https://ok.ru/group57212027273260""";
    static final String SAME_SOCIAL_MEDIA_MSG = """
            Социальная сеть %s уже была синхронизирована с текущим Телеграм-каналом.
            Пожалуйста, выберите другую социальную сеть и попробуйте снова.""";

    @Autowired
    private CurrentChannelRepository currentChannelRepository;

    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;

    public AddOkGroup() {
        super(State.AddOkGroup.getIdentifier(), State.AddOkGroup.getDescription());
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chat.getId());
        if (currentChannel != null) {
            for (ChannelGroup smg : channelGroupsRepository.getGroupsForChannel(currentChannel.getChannelId())) {
                if (smg.getSocialMedia() == SocialMedia.OK) {
                    sendAnswerWithOnlyBackButton(absSender,
                            chat.getId(),
                            this.getCommandIdentifier(),
                            user.getUserName(),
                            String.format(SAME_SOCIAL_MEDIA_MSG, SocialMedia.OK.getName()));
                    return;
                }
            }
        }
        sendAnswerWithOnlyBackButton(absSender,
                chat.getId(),
                this.getCommandIdentifier(),
                user.getUserName(),
                ADD_OK_GROUP_MSG);
    }
}
