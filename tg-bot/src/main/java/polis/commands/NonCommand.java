package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polis.data.domain.*;
import polis.data.repositories.*;
import polis.ok.OKDataCheck;
import polis.telegram.TelegramDataCheck;
import polis.util.IState;
import polis.util.SocialMedia;
import polis.util.State;
import polis.util.Substate;

import static polis.commands.AddOkGroup.SAME_SOCIAL_MEDIA;

public class NonCommand {
    private static final String START_STATE_ANSWER = "Не могу распознать команду. Попробуйте еще раз.";
    private static final String BOT_WRONG_STATE_ANSWER = "Неверная команда бота. Попробуйте еще раз.";
    private static final String WRONG_LINK_TELEGRAM = """
             Ссылка неверная.
             Пожалуйста, проверьте, что ссылка на канал является верной и введите ссылку еще раз.""";
    private static final String WRONG_OK_ACCOUNT = """
            Неверный аккаунт Одноклассников.
            Пожалуйста, вернитесь в главное меню (%s) и следуйте дальнейшим инструкциям.""";
    private final CurrentAccountRepository currentAccountRepository;
    private final UserChannelsRepository userChannelsRepository;
    private final CurrentChannelRepository currentChannelRepository;
    private final CurrentGroupRepository currentGroupRepository;
    private final ChannelGroupsRepositoryImpl channelGroupsRepository;
    private final OKDataCheck okDataCheck;
    private final TelegramDataCheck telegramDataCheck;
    private final Logger logger = LoggerFactory.getLogger(NonCommand.class);

    public NonCommand(OKDataCheck okDataCheck,
                      CurrentAccountRepository currentAccountRepository,
                      UserChannelsRepository userChannelsRepository,
                      CurrentChannelRepository currentChannelRepository,
                      CurrentGroupRepository currentGroupRepository,
                      ChannelGroupsRepositoryImpl channelGroupsRepository) {
        this.currentAccountRepository = currentAccountRepository;
        this.userChannelsRepository = userChannelsRepository;
        this.currentChannelRepository = currentChannelRepository;
        this.okDataCheck = okDataCheck;
        this.currentGroupRepository = currentGroupRepository;
        this.channelGroupsRepository = channelGroupsRepository;
        telegramDataCheck = new TelegramDataCheck();
    }

    public AnswerPair nonCommandExecute(String text, Long chatId, IState state) {
        if (state == null) {
            logger.error("Null state");
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
                tgChannelOwner.put(newTgChannel.getChannelId(), chatId);
            }
            return answer;
        } else if (state.equals(Substate.AddOkAccount_AuthCode)) {
            return okDataCheck.getOKAuthCode(text, chatId);
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

            Long groupId = okDataCheck.getOKGroupId(text, accessToken);

            AnswerPair answer = okDataCheck.checkOKGroupAdminRights(accessToken, groupId);

            if (!answer.getError()) {
                CurrentGroup newGroup = new CurrentGroup(chatId, SocialMedia.OK, groupId,
                        okDataCheck.getOKGroupName(groupId, currentAccount.getAccessToken()),
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
