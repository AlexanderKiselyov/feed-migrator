package polis.commands.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polis.commands.Command;
import polis.commands.context.Context;
import polis.util.IState;
import polis.util.State;
import polis.vk.api.VkAuthorizator;

import java.net.URISyntaxException;

@Component
public class AddVkAccount extends Command {
    private static final String VK_AUTH_ANSWER_MSG = """
            Для авторизации в социальной сети ВКонтакте перейдите по ссылке:
            %s
            После авторизации скопируйте всю ссылку из адресной строки целиком и отправьте ее в этот диалог.""";
    private static final Logger LOGGER = LoggerFactory.getLogger(AddVkAccount.class);

    @Override
    public IState state() {
        return State.AddVkAccount;
    }

    @Override
    public void doExecute(AbsSender absSender, User user, Chat chat, Context context) {
        try {
            String messageText = String.format(VK_AUTH_ANSWER_MSG, VkAuthorizator.formAuthorizationUrl());
            sendAnswerWithOnlyBackButton(
                    absSender,
                    chat.getId(),
                    messageText,
                    loggingInfo(user.getUserName()));
        } catch (URISyntaxException e) {
            LOGGER.error(String.format("Cannot form link: %s", e));
        }
    }
}
