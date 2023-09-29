package polis.callbacks.justmessages.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import polis.commands.NonCommand;
import polis.commands.context.Context;
import polis.data.domain.CurrentChannel;
import polis.data.domain.UserChannels;
import polis.data.repositories.UserChannelsRepository;
import polis.telegram.TelegramDataCheck;
import polis.util.IState;
import polis.util.State;

@Component
public class AddTgChannelCallbackHandler extends NonCommandHandler {
    private static final String WRONG_CHAT_PARAMETERS = """
            Ошибка получения параметров чата.
            Пожалуйста, проверьте, что ссылка на канал является верной и введите ссылку еще раз.""";
    private static final String SAME_CHANNEL = """
            Телеграмм-канал <b>%s</b> уже был ранее добавлен.
            Пожалуйста, выберите другой Телеграмм-канал и попробуйте снова.""";
    private static final String WRONG_LINK_TELEGRAM = """
            Ссылка неверная.
            Пожалуйста, проверьте, что ссылка на канал является верной и введите ссылку еще раз.""";

    @Autowired
    private TelegramDataCheck telegramDataCheck;
    @Autowired
    private UserChannelsRepository userChannelsRepository;

    @Override
    public IState state() {
        return State.AddTgChannel;
    }

    @Override
    protected NonCommand.AnswerPair nonCommandExecute(long chatId, String text, Context context) {
        String[] split = text.split("/");
        if (split.length < 2) {

            return new NonCommand.AnswerPair(WRONG_LINK_TELEGRAM, true);
        }
        String checkChannelLink = text.split("/")[split.length - 1];

        NonCommand.AnswerPair answer = telegramDataCheck.checkTelegramChannelLink(checkChannelLink);
        if (!answer.getError()) {
            TelegramDataCheck.TelegramChannel channel = telegramDataCheck.getChannel(checkChannelLink);

            if (channel == null) {
                return new NonCommand.AnswerPair(WRONG_CHAT_PARAMETERS, true);
            }

            UserChannels addedChannel = userChannelsRepository.getUserChannel(channel.id(), chatId);

            if (addedChannel != null) {
                return new NonCommand.AnswerPair(String.format(SAME_CHANNEL, addedChannel.getChannelUsername()), true);
            }

            UserChannels newTgChannel = new UserChannels(
                    chatId,
                    channel.id(),
                    channel.title()
            );

            userChannelsRepository.insertUserChannel(newTgChannel);
            context.resetCurrentChannel(new CurrentChannel(
                    chatId,
                    newTgChannel.getChannelId(),
                    newTgChannel.getChannelUsername()
            ));
        }
        return answer;
    }
}
