package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import polis.commands.context.Context;
import polis.commands.context.ContextStorage;
import polis.data.domain.Account;
import polis.data.domain.ChannelGroup;
import polis.data.domain.CurrentChannel;
import polis.data.domain.UserChannels;
import polis.data.repositories.ChannelGroupsRepository;
import polis.data.repositories.UserChannelsRepository;
import polis.datacheck.OkDataCheck;
import polis.datacheck.VkDataCheck;
import polis.telegram.TelegramDataCheck;
import polis.util.IState;
import polis.util.SocialMedia;
import polis.util.State;
import polis.util.Substate;
import polis.vk.api.VkAuthorizator;

import java.util.Objects;

import static polis.commands.Command.GROUP_NAME_NOT_FOUND;
import static polis.commands.impl.AddOkGroup.SAME_SOCIAL_MEDIA_MSG;

@Component
public class NonCommand {
    private static final String START_STATE_ANSWER = "Не могу распознать команду. Попробуйте еще раз.";
    private static final String BOT_WRONG_STATE_ANSWER = "Неверная команда бота. Попробуйте еще раз.";
    private static final String GROUP_NOT_FOUND = "Не удалось получить id группы. Попробуйте еще раз.";
    private static final String USER_NOT_GROUP_ADMIN = """
            Пользователь не является администратором, модератором или редактором канала.
            Попробуйте еще раз.""";
    public static final String VK_GROUP_ADDED = """
            Группа была успешно добавлена.
            Синхронизируйте группу с Телеграмм-каналом по команде /%s.""";
    private static final String WRONG_CHAT_PARAMETERS = """
            Ошибка получения параметров чата.
            Пожалуйста, проверьте, что ссылка на канал является верной и введите ссылку еще раз.""";
    private static final String SAME_CHANNEL = """
            Телеграмм-канал <b>%s</b> уже был ранее добавлен.
            Пожалуйста, выберите другой Телеграмм-канал и попробуйте снова.""";
    private static final String WRONG_LINK_TELEGRAM = """
            Ссылка неверная.
            Пожалуйста, проверьте, что ссылка на канал является верной и введите ссылку еще раз.""";
    private static final String WRONG_ACCOUNT = """
            Неверный аккаунт %s.
            Пожалуйста, вернитесь в главное меню (%s) и следуйте дальнейшим инструкциям.""";

    @Autowired
    private UserChannelsRepository userChannelsRepository;

    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;

    @Autowired
    private OkDataCheck okDataCheck;

    @Autowired
    private VkDataCheck vkDataCheck;

    @Autowired
    private TelegramDataCheck telegramDataCheck;

    @Autowired
    private ContextStorage contextStorage;

    private static final Logger LOGGER = LoggerFactory.getLogger(NonCommand.class);

    public AnswerPair nonCommandExecute(String text, Long chatId) {
        Context context = contextStorage.getContext(chatId);
        IState state = context.currentState();
        if (state == null) {
            LOGGER.error("Null state");
            return new AnswerPair(BOT_WRONG_STATE_ANSWER, true);
        }


        if (state.equals(State.Start)) {
            return new AnswerPair(START_STATE_ANSWER, true);
        } else if (state.equals(State.AddTgChannel)) {
            String[] split = text.split("/");
            if (split.length < 2) {
                return new AnswerPair(WRONG_LINK_TELEGRAM, true);
            }
            String checkChannelLink = text.split("/")[split.length - 1];

            AnswerPair answer = telegramDataCheck.checkTelegramChannelLink(checkChannelLink);
            if (!answer.getError()) {
                TelegramDataCheck.TelegramChannel channel = telegramDataCheck.getChannel(checkChannelLink);

                if (channel == null) {
                    return new AnswerPair(WRONG_CHAT_PARAMETERS, true);
                }

                UserChannels addedChannel = userChannelsRepository.getUserChannel(channel.id(), chatId);

                if (addedChannel != null) {
                    return new AnswerPair(String.format(SAME_CHANNEL, addedChannel.getChannelUsername()), true);
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
        } else if (state.equals(Substate.AddOkAccount_AuthCode)) {
            return okDataCheck.getOKAuthCode(text, chatId);
        } else if (state.equals(Substate.AddOkGroup_AddGroup)) {
            Account currentAccount = context.currentAccount();
            if (currentAccount == null) {
                return new AnswerPair(String.format(WRONG_ACCOUNT, SocialMedia.OK.getName(),
                        State.MainMenu.getIdentifier()),
                        true);
            }

            for (ChannelGroup smg : channelGroupsRepository
                    .getGroupsForChannel(context.currentChannel().getChannelId())) {
                if (smg.getSocialMedia() == SocialMedia.OK) {
                    return new AnswerPair(String.format(SAME_SOCIAL_MEDIA_MSG, SocialMedia.OK.getName()), true);
                }
            }

            String accessToken = currentAccount.getAccessToken();

            Long groupId = okDataCheck.getOKGroupId(text, accessToken);

            if (groupId == null) {
                return new AnswerPair(GROUP_NOT_FOUND, true);
            }

            AnswerPair answer = okDataCheck.checkOKGroupAdminRights(accessToken, groupId);

            String groupName = okDataCheck.getOKGroupName(groupId, currentAccount.getAccessToken());

            if (Objects.equals(groupName, null)) {
                return new AnswerPair(GROUP_NAME_NOT_FOUND, true);
            }

            if (!answer.getError()) {
                context.resetCurrentGroup(new ChannelGroup(
                        currentAccount.getAccessToken(),
                        groupName,
                        currentAccount.getAccountId(),
                        chatId,
                        groupId,
                        SocialMedia.OK.getName()
                ));
            }

            return answer;
        } else if (state.equals(Substate.AddVkAccount_AccessToken)) {
            return vkDataCheck.getVkAccessToken(text, chatId);
        } else if (state.equals(Substate.AddVkGroup_AddGroup)) {
            Account currentAccount = context.currentAccount();
            if (currentAccount == null) {
                return new AnswerPair(String.format(WRONG_ACCOUNT, SocialMedia.VK.getName(),
                        State.MainMenu.getIdentifier()),
                        true);
            }

            for (ChannelGroup smg : channelGroupsRepository
                    .getGroupsForChannel(context.currentChannel().getChannelId())) {
                if (smg.getSocialMedia() == SocialMedia.VK) {
                    return new AnswerPair(String.format(SAME_SOCIAL_MEDIA_MSG, SocialMedia.VK.getName()), true);
                }
            }

            String accessToken = currentAccount.getAccessToken();

            Integer groupId = vkDataCheck.getVkGroupId(new VkAuthorizator.TokenWithId(accessToken,
                    (int) currentAccount.getAccountId()), text);

            if (groupId == null) {
                return new AnswerPair(GROUP_NOT_FOUND, true);
            }

            Boolean isAdmin = vkDataCheck.getIsVkGroupAdmin(new VkAuthorizator.TokenWithId(accessToken,
                    (int) currentAccount.getAccountId()), text);

            if (!isAdmin) {
                return new AnswerPair(USER_NOT_GROUP_ADMIN, true);
            }

            String groupName = vkDataCheck.getVkGroupName(
                    new VkAuthorizator.TokenWithId(currentAccount.getAccessToken(),
                            (int) currentAccount.getAccountId()),
                    groupId
            );

            if (Objects.equals(groupName, null)) {
                return new AnswerPair(GROUP_NAME_NOT_FOUND, true);
            }

            context.resetCurrentGroup(new ChannelGroup(
                    currentAccount.getAccessToken(),
                    groupName,
                    currentAccount.getAccountId(),
                    chatId,
                    groupId,
                    SocialMedia.VK.getName()
            ));
            return new AnswerPair(String.format(VK_GROUP_ADDED, State.SyncVkTg.getIdentifier()), false);
        }
        return new AnswerPair(BOT_WRONG_STATE_ANSWER, true);
    }

    /**
     * Хранит ответ бота с индикатором ранее присланного ошибочного сообщения от пользователя.
     */
    public static class AnswerPair {
        private final String answer;
        private final Boolean isError;

        public AnswerPair(String answer, boolean isError) {
            this.answer = answer;
            this.isError = isError;
        }

        public String getAnswer() {
            return answer;
        }

        public Boolean getError() {
            return isError;
        }
    }
}
