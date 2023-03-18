package polis.commands;

import polis.ok.OKDataCheck;
import polis.telegram.TelegramDataCheck;
import polis.util.AuthData;
import polis.util.IState;
import polis.util.SocialMedia;
import polis.util.State;
import polis.util.Substate;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class NonCommand {
    private static final String START_STATE_ANSWER = "Не могу распознать команду. Попробуйте еще раз.";
    private static final String BOT_WRONG_STATE_ANSWER = "Неверное состояние бота. Попробуйте еще раз.";
    private static final String WRONG_LINK_TELEGRAM = """
             Ссылка неверная.
             Пожалуйста, проверьте, что ссылка на канал является верной и введите ссылку еще раз.""";
    private final Map<Long, List<AuthData>> socialMedia;
    private final OKDataCheck okDataCheck;
    private final TelegramDataCheck telegramDataCheck;

    public NonCommand(Map<Long, IState> states, Map<Long, List<AuthData>> socialMedia, Properties properties) {
        this.socialMedia = socialMedia;

        okDataCheck = new OKDataCheck(properties, socialMedia, states);
        telegramDataCheck = new TelegramDataCheck(properties);
    }

    public String nonCommandExecute(String text, Long chatId, IState state) {
        if (state == null) {
            return BOT_WRONG_STATE_ANSWER;
        }
        if (state.equals(State.Start)) {
            return START_STATE_ANSWER;
        } else if (state.equals(Substate.OkAuth_AuthCode)) {
            return okDataCheck.getOKAuthCode(text, chatId);
        } else if (state.equals(Substate.OkAuth_GroupSync)) {
            String accessToken = "";
            for (AuthData authData : socialMedia.get(chatId)) {
                if (authData.getSocialMedia() == SocialMedia.OK) {
                    accessToken = authData.getAccessToken();
                    break;
                }
            }

            Long groupId = okDataCheck.getOKGroupId(text, accessToken);

            return okDataCheck.checkOKGroupAdminRights(accessToken, groupId);
        } else if (state.equals(Substate.Sync_TelegramChannel)) {
            String[] split = text.split("/");
            if (split.length < 2) {
                return WRONG_LINK_TELEGRAM;
            }
            String checkChatId = text.split("/")[split.length - 1];

            return telegramDataCheck.checkTelegramChannelLink(checkChatId);
        }
        return BOT_WRONG_STATE_ANSWER;
    }
}
