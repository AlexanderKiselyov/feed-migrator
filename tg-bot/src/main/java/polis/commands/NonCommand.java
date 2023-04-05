package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polis.ok.OKDataCheck;
import polis.telegram.TelegramDataCheck;
import polis.util.AuthData;
import polis.util.IState;
import polis.util.SocialMedia;
import polis.util.SocialMediaGroup;
import polis.util.State;
import polis.util.Substate;
import polis.util.TelegramChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private final Map<Long, AuthData> currentSocialMediaAccount;
    private final Map<Long, List<TelegramChannel>> tgChannels;
    private final Map<Long, TelegramChannel> currentTgChannel;
    private final Map<Long, SocialMediaGroup> currentSocialMediaGroup;
    private final Map<Long, Long> tgChannelOwner;
    private final OKDataCheck okDataCheck;
    private final TelegramDataCheck telegramDataCheck;
    private final Logger logger = LoggerFactory.getLogger(NonCommand.class);

    public NonCommand(OKDataCheck okDataCheck,
                      Map<Long, AuthData> currentSocialMediaAccount,
                      Map<Long, List<TelegramChannel>> tgChannels,
                      Map<Long, TelegramChannel> currentChannel,
                      Map<Long, SocialMediaGroup> currentSocialMediaGroup,
                      Map<Long, Long> tgChannelOwner) {
        this.currentSocialMediaAccount = currentSocialMediaAccount;
        this.tgChannels = tgChannels;
        this.currentTgChannel = currentChannel;
        this.okDataCheck = okDataCheck;
        this.currentSocialMediaGroup = currentSocialMediaGroup;
        this.tgChannelOwner = tgChannelOwner;
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
                TelegramChannel newTgChannel;
                if (tgChannels.containsKey(chatId)) {
                    List<TelegramChannel> currentTelegramChannels = tgChannels.get(chatId);
                    newTgChannel = new TelegramChannel(
                            (Long) telegramDataCheck.getChatParameter(checkChannelLink, "id"),
                            checkChannelLink, new ArrayList<>(1));
                    currentTelegramChannels.add(newTgChannel);
                    tgChannels.put(chatId, currentTelegramChannels);
                } else {
                    List<TelegramChannel> newTelegramChannel = new ArrayList<>(1);
                    newTgChannel = new TelegramChannel(
                            (Long) telegramDataCheck.getChatParameter(checkChannelLink, "id"),
                            checkChannelLink, new ArrayList<>(1));
                    newTelegramChannel.add(newTgChannel);
                    tgChannels.put(chatId, newTelegramChannel);
                }
                currentTgChannel.put(chatId, newTgChannel);
                tgChannelOwner.put(newTgChannel.getTelegramChannelId(), chatId);
            }
            return answer;
        } else if (state.equals(Substate.AddOkAccount_AuthCode)) {
            return okDataCheck.getOKAuthCode(text, chatId);
        } else if (state.equals(Substate.AddOkGroup_AddGroup)) {
            if (currentSocialMediaAccount.get(chatId) == null) {
                return new AnswerPair(String.format(WRONG_OK_ACCOUNT, State.MainMenu.getIdentifier()),
                        true);
            }

            for (SocialMediaGroup smg : currentTgChannel.get(chatId).getSynchronizedGroups()) {
                if (smg.getSocialMedia() == SocialMedia.OK) {
                    return new AnswerPair(String.format(SAME_SOCIAL_MEDIA, SocialMedia.OK.getName()), true);
                }
            }

            String accessToken = currentSocialMediaAccount.get(chatId).getAccessToken();

            Long groupId = okDataCheck.getOKGroupId(text, accessToken);

            AnswerPair answer = okDataCheck.checkOKGroupAdminRights(accessToken, groupId);

            if (!answer.getError()) {
                SocialMediaGroup newGroup = new SocialMediaGroup(groupId,
                        currentSocialMediaAccount.get(chatId).getTokenId(), SocialMedia.OK);
                currentSocialMediaGroup.put(chatId, newGroup);
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
