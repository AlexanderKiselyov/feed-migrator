package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import polis.data.domain.ChannelGroup;
import polis.data.domain.CurrentAccount;
import polis.data.domain.CurrentChannel;
import polis.data.domain.CurrentGroup;
import polis.data.domain.UserChannels;
import polis.data.repositories.AccountsRepository;
import polis.data.repositories.ChannelGroupsRepository;
import polis.data.repositories.CurrentAccountRepository;
import polis.data.repositories.CurrentChannelRepository;
import polis.data.repositories.CurrentGroupRepository;
import polis.data.repositories.CurrentStateRepository;
import polis.data.repositories.UserChannelsRepository;
import polis.data_check.DataCheck;
import polis.telegram.TelegramDataCheck;
import polis.util.IState;
import polis.util.SocialMedia;
import polis.util.State;
import polis.util.Substate;

import java.util.Objects;

import static polis.commands.AddOkGroup.SAME_SOCIAL_MEDIA;
import static polis.commands.Command.GROUP_NAME_NOT_FOUND;

@Component
public class NonCommand {
    private static final String START_STATE_ANSWER = "Не могу распознать команду. Попробуйте еще раз.";
    private static final String BOT_WRONG_STATE_ANSWER = "Неверная команда бота. Попробуйте еще раз.";
    private static final String GROUP_NOT_FOUND = "Не удалось получить id группы. Попробуйте еще раз.";
    private static final String WRONG_LINK_TELEGRAM = """
             Ссылка неверная.
             Пожалуйста, проверьте, что ссылка на канал является верной и введите ссылку еще раз.""";
    private static final String WRONG_OK_ACCOUNT = """
            Неверный аккаунт Одноклассников.
            Пожалуйста, вернитесь в главное меню (%s) и следуйте дальнейшим инструкциям.""";

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    @Autowired
    private UserChannelsRepository userChannelsRepository;

    @Autowired
    private CurrentChannelRepository currentChannelRepository;

    @Autowired
    private CurrentGroupRepository currentGroupRepository;

    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;

    @Autowired
    private CurrentStateRepository currentStateRepository;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private DataCheck dataCheck;

    private final TelegramDataCheck telegramDataCheck;
    private static final Logger LOGGER = LoggerFactory.getLogger(NonCommand.class);

    public NonCommand() {
        telegramDataCheck = new TelegramDataCheck();
    }

    public AnswerPair nonCommandExecute(String text, Long chatId, IState state) {
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
                UserChannels newTgChannel = new UserChannels(
                        chatId,
                        (Long) telegramDataCheck.getChatParameter(checkChannelLink, "id"),
                        checkChannelLink
                );
                userChannelsRepository.insertUserChannel(newTgChannel);
                currentChannelRepository.insertCurrentChannel(new CurrentChannel(chatId, newTgChannel.getChannelId(),
                        newTgChannel.getChannelUsername()));
            }
            return answer;
        } else if (state.equals(Substate.AddOkAccount_AuthCode)) {
            return dataCheck.getOKAuthCode(text, chatId);
        } else if (state.equals(Substate.AddOkGroup_AddGroup)) {
            CurrentAccount currentAccount = currentAccountRepository.getCurrentAccount(chatId);
            if (currentAccount == null) {
                return new AnswerPair(String.format(WRONG_OK_ACCOUNT, State.MainMenu.getIdentifier()),
                        true);
            }

            for (ChannelGroup smg : channelGroupsRepository
                    .getGroupsForChannel(currentChannelRepository.getCurrentChannel(chatId).getChannelId())) {
                if (smg.getSocialMedia() == SocialMedia.OK) {
                    return new AnswerPair(String.format(SAME_SOCIAL_MEDIA, SocialMedia.OK.getName()), true);
                }
            }

            String accessToken = currentAccount.getAccessToken();

            Long groupId = dataCheck.getOKGroupId(text, accessToken);

            if (groupId == -1) {
                return new AnswerPair(GROUP_NOT_FOUND, true);
            }

            AnswerPair answer = dataCheck.checkOKGroupAdminRights(accessToken, groupId);

            String groupName = dataCheck.getOKGroupName(groupId, currentAccount.getAccessToken());

            if (Objects.equals(groupName, "")) {
                return new AnswerPair(GROUP_NAME_NOT_FOUND, true);
            }

            if (!answer.getError()) {
                CurrentGroup newGroup = new CurrentGroup(chatId, SocialMedia.OK.getName(), groupId,
                        groupName,
                        currentAccount.getAccountId(), currentAccount.getAccessToken());
                currentGroupRepository.insertCurrentGroup(newGroup);
            }

            return answer;
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
