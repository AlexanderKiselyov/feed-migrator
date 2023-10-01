package polis.bot;

import polis.callbacks.justmessages.handlers.AddVkGroupHandler;
import polis.callbacks.justmessages.handlers.OkAuthCodeCallbackHandler;
import polis.util.State;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static polis.datacheck.OkDataCheck.OK_AUTH_STATE_WRONG_AUTH_CODE_ANSWER;
import static polis.datacheck.OkDataCheck.OK_GROUP_ADDED;
import static polis.datacheck.OkDataCheck.SAME_OK_ACCOUNT;
import static polis.datacheck.OkDataCheck.USER_HAS_NO_RIGHTS;
import static polis.datacheck.OkDataCheck.WRONG_LINK_OR_USER_HAS_NO_RIGHTS;
import static polis.datacheck.VkDataCheck.SAME_VK_ACCOUNT;
import static polis.datacheck.VkDataCheck.VK_AUTH_STATE_ANSWER;
import static polis.datacheck.VkDataCheck.VK_AUTH_STATE_SERVER_EXCEPTION_ANSWER;
import static polis.telegram.TelegramDataCheck.BOT_NOT_ADMIN;
import static polis.telegram.TelegramDataCheck.RIGHT_LINK;
import static polis.telegram.TelegramDataCheck.WRONG_LINK_OR_BOT_NOT_ADMIN;

//TODO REMOVE THIS SHIT
@Deprecated
class ButtonsUtil {
    private static final String TURN_ON_NOTIFICATIONS_MSG = "\nВы также можете включить уведомления, чтобы быть в "
            + "курсе автоматически опубликованных записей с помощью команды /notifications";
    private static final String AUTOPOSTING_ENABLE_AND_NOTIFICATIONS = "Функция автопостинга включена."
            + TURN_ON_NOTIFICATIONS_MSG;
    private static final String OK_AUTH_STATE_SERVER_EXCEPTION_ANSWER = """
            Невозможно выполнить авторизацию в социальной сети Одноклассники.
            Пожалуйста, проверьте данные авторизации и попробуйте еще раз.""";
    public static final Map<String, List<String>> BUTTONS_TEXT_MAP = Map.ofEntries(
            Map.entry(RIGHT_LINK, List.of(State.TgChannelDescription.getDescription())),
            Map.entry(OK_AUTH_STATE_WRONG_AUTH_CODE_ANSWER, Collections.emptyList()),
            Map.entry(OK_AUTH_STATE_SERVER_EXCEPTION_ANSWER, Collections.emptyList()),
            Map.entry(WRONG_LINK_OR_USER_HAS_NO_RIGHTS, Collections.emptyList()),
            Map.entry(USER_HAS_NO_RIGHTS, Collections.emptyList()),
            Map.entry(SAME_OK_ACCOUNT, Collections.emptyList()),
            Map.entry(VK_AUTH_STATE_SERVER_EXCEPTION_ANSWER, Collections.emptyList()),
            Map.entry(SAME_VK_ACCOUNT, Collections.emptyList()),
            Map.entry(WRONG_LINK_OR_BOT_NOT_ADMIN, Collections.emptyList()),
            Map.entry(BOT_NOT_ADMIN, Collections.emptyList()),
            Map.entry(AUTOPOSTING_ENABLE_AND_NOTIFICATIONS, List.of(State.Notifications.getDescription())),
            Map.entry(
                    String.format(OK_GROUP_ADDED, State.SyncOkTg.getIdentifier()),
                    List.of(State.SyncOkTg.getDescription())
            ),
            Map.entry(
                    String.format(
                            OkAuthCodeCallbackHandler.OK_AUTH_STATE_ANSWER,
                            State.OkAccountDescription.getIdentifier()
                    ),
                    List.of(State.OkAccountDescription.getDescription())
            ),
            Map.entry(
                    String.format(
                            VK_AUTH_STATE_ANSWER,
                            State.VkAccountDescription.getIdentifier()
                    ),
                    List.of(State.VkAccountDescription.getDescription())
            ),
            Map.entry(
                    String.format(
                            AddVkGroupHandler.VK_GROUP_ADDED,
                            State.SyncVkTg.getIdentifier()
                    ),
                    List.of(State.SyncVkTg.getDescription())
            )
    );
}
