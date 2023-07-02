package polis.bot;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.commands.AccountsList;
import polis.commands.AddGroup;
import polis.commands.AddOkAccount;
import polis.commands.AddOkGroup;
import polis.commands.AddTgChannel;
import polis.commands.AddVkAccount;
import polis.commands.AddVkGroup;
import polis.commands.Autoposting;
import polis.commands.GroupDescription;
import polis.commands.Help;
import polis.commands.MainMenu;
import polis.commands.NonCommand;
import polis.commands.Notifications;
import polis.commands.OkAccountDescription;
import polis.commands.StartCommand;
import polis.commands.SyncGroupDescription;
import polis.commands.SyncOkTg;
import polis.commands.SyncVkTg;
import polis.commands.TgChannelDescription;
import polis.commands.TgChannelsList;
import polis.commands.TgSyncGroups;
import polis.commands.VkAccountDescription;
import polis.data.domain.CurrentState;
import polis.data.repositories.AccountsRepository;
import polis.data.repositories.ChannelGroupsRepository;
import polis.data.repositories.CurrentAccountRepository;
import polis.data.repositories.CurrentChannelRepository;
import polis.data.repositories.CurrentGroupRepository;
import polis.data.repositories.CurrentStateRepository;
import polis.data.repositories.UserChannelsRepository;
import polis.keyboards.ReplyKeyboard;
import polis.keyboards.callbacks.CallbacksHandlerHelper;
import polis.posting.IPostsProcessor;
import polis.util.IState;
import polis.util.State;
import polis.util.Substate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static polis.commands.NonCommand.VK_GROUP_ADDED;
import static polis.datacheck.OkDataCheck.OK_AUTH_STATE_ANSWER;
import static polis.datacheck.OkDataCheck.OK_AUTH_STATE_SERVER_EXCEPTION_ANSWER;
import static polis.datacheck.OkDataCheck.OK_AUTH_STATE_WRONG_AUTH_CODE_ANSWER;
import static polis.datacheck.OkDataCheck.OK_GROUP_ADDED;
import static polis.datacheck.OkDataCheck.SAME_OK_ACCOUNT;
import static polis.datacheck.OkDataCheck.USER_HAS_NO_RIGHTS;
import static polis.datacheck.OkDataCheck.WRONG_LINK_OR_USER_HAS_NO_RIGHTS;
import static polis.datacheck.VkDataCheck.SAME_VK_ACCOUNT;
import static polis.datacheck.VkDataCheck.VK_AUTH_STATE_ANSWER;
import static polis.datacheck.VkDataCheck.VK_AUTH_STATE_SERVER_EXCEPTION_ANSWER;
import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;
import static polis.telegram.TelegramDataCheck.BOT_NOT_ADMIN;
import static polis.telegram.TelegramDataCheck.RIGHT_LINK;
import static polis.telegram.TelegramDataCheck.WRONG_LINK_OR_BOT_NOT_ADMIN;

@Configuration
@Component("Bot")
public class Bot extends TelegramLongPollingCommandBot implements TgFileLoader, TgNotificator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);
    private static final List<String> EMPTY_LIST = List.of();
    private static final String TURN_ON_NOTIFICATIONS_MSG = "\nВы также можете включить уведомления, чтобы быть в "
            + "курсе автоматически опубликованных записей с помощью команды /notifications";
    private static final String AUTOPOSTING_ENABLE_AND_NOTIFICATIONS = "Функция автопостинга включена."
            + TURN_ON_NOTIFICATIONS_MSG;
    private static final String DEBUG_INFO_TEXT = "Update from ";
    private static final Map<String, List<String>> BUTTONS_TEXT_MAP = Map.ofEntries(
            Map.entry(String.format(OK_AUTH_STATE_ANSWER, State.OkAccountDescription.getIdentifier()),
                    List.of(State.OkAccountDescription.getDescription())),
            Map.entry(String.format(OK_GROUP_ADDED, State.SyncOkTg.getIdentifier()),
                    List.of(State.SyncOkTg.getDescription())),
            Map.entry(RIGHT_LINK, List.of(State.TgChannelDescription.getDescription())),
            Map.entry(OK_AUTH_STATE_WRONG_AUTH_CODE_ANSWER, EMPTY_LIST),
            Map.entry(OK_AUTH_STATE_SERVER_EXCEPTION_ANSWER, EMPTY_LIST),
            Map.entry(WRONG_LINK_OR_USER_HAS_NO_RIGHTS, EMPTY_LIST),
            Map.entry(USER_HAS_NO_RIGHTS, EMPTY_LIST),
            Map.entry(SAME_OK_ACCOUNT, EMPTY_LIST),
            Map.entry(VK_AUTH_STATE_SERVER_EXCEPTION_ANSWER, EMPTY_LIST),
            Map.entry(SAME_VK_ACCOUNT, EMPTY_LIST),
            Map.entry(WRONG_LINK_OR_BOT_NOT_ADMIN, EMPTY_LIST),
            Map.entry(BOT_NOT_ADMIN, EMPTY_LIST),
            Map.entry(AUTOPOSTING_ENABLE_AND_NOTIFICATIONS, List.of(State.Notifications.getDescription())),
            Map.entry(String.format(VK_AUTH_STATE_ANSWER, State.VkAccountDescription.getIdentifier()),
                    List.of(State.VkAccountDescription.getDescription())),
            Map.entry(String.format(VK_GROUP_ADDED, State.SyncVkTg.getIdentifier()),
                    List.of(State.SyncVkTg.getDescription()))
    );

    private final String botName;
    private final String botToken;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private ChannelGroupsRepository channelGroupsRepository;

    @Autowired
    private UserChannelsRepository userChannelsRepository;

    @Autowired
    private CurrentChannelRepository currentChannelRepository;

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    @Autowired
    private CurrentGroupRepository currentGroupRepository;

    @Autowired
    private CurrentStateRepository currentStateRepository;

    @Autowired
    private NonCommand nonCommand;

    @Autowired
    private TgChannelDescription tgChannelDescription;

    @Autowired
    private TgChannelsList tgChannelsList;

    @Autowired
    private TgSyncGroups tgSyncGroups;

    @Autowired
    private GroupDescription groupDescription;

    @Autowired
    private AddGroup addGroup;

    @Autowired
    private OkAccountDescription okAccountDescription;

    @Autowired
    private AccountsList accountsList;

    @Autowired
    private AddOkGroup addOkGroup;

    @Autowired
    private SyncGroupDescription syncGroupDescription;

    @Autowired
    private SyncOkTg syncOkTg;

    @Autowired
    private Autoposting autoposting;

    @Autowired
    private Notifications notifications;

    @Autowired
    private VkAccountDescription vkAccountDescription;

    @Autowired
    private AddVkGroup addVkGroup;

    @Autowired
    private SyncVkTg syncVkTg;

    @Autowired
    IPostsProcessor postsProcessor;

    @Lazy
    @Autowired
    private TgContentManager tgContentManager;

    @Autowired
    private ReplyKeyboard replyKeyboard;

    @Autowired
    CallbacksHandlerHelper callbacksHandlerHelper;

    public Bot(
            @Value("${bot.name}") String botName,
            @Value("${bot.token}") String botToken
    ) {
        super();
        this.botName = botName;
        this.botToken = botToken;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onRegister() {
        super.onRegister();
        register(new StartCommand());
        register(new AddTgChannel());
        register(new Help());
        register(new MainMenu());
        register(tgChannelDescription);
        register(tgChannelsList);
        register(tgSyncGroups);
        register(groupDescription);
        register(addGroup);
        register(new AddOkAccount());
        register(okAccountDescription);
        register(accountsList);
        register(addOkGroup);
        register(syncGroupDescription);
        register(syncOkTg);
        register(autoposting);
        register(notifications);
        register(new AddVkAccount());
        register(vkAccountDescription);
        register(addVkGroup);
        register(syncVkTg);
    }

    @Override
    public void onUpdatesReceived(List<Update> overallUpdates) {
        Map<Boolean, List<Update>> updates = overallUpdates.stream()
                .collect(Collectors.partitioningBy(Update::hasChannelPost));
        boolean channelPosts = true;

        updates.get(!channelPosts).forEach(this::processNonCommandUpdate);
        updates.get(channelPosts).stream()
                .map(Update::getChannelPost)
                .collect(Collectors.groupingBy(Message::getChatId))
                .forEach((channelId, posts) -> postsProcessor.processPostsInChannel(channelId, posts));
    }

    /**
     * Устанавливает бота в определенное состояние в зависимости от введенной пользователем команды.
     *
     * @param message отправленное пользователем сообщение
     */
    public void setStateForMessage(Message message) {
        if (message == null) {
            return;
        }
        if (LOGGER.isDebugEnabled()) {
            String s = messageDebugInfo(message);
            LOGGER.debug(s);
        }
        if (message.getText() == null) {
            return;
        }
        State currentState = State.findState(message.getText().replace("/", ""));
        if (currentState != null) {
            currentStateRepository.insertCurrentState(new CurrentState(message.getChatId(),
                    currentState.getIdentifier()));
        }
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        setStateForMessage(update.getMessage());
        Message msg;
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            msg = callbackQuery.getMessage();
            String callbackQueryData = callbackQuery.getData();
            if (callbackQueryData.startsWith("/")) {
                getRegisteredCommand(callbackQueryData.replace("/", ""))
                        .processMessage(this, msg, null);
            } else {
                try {
                    callbacksHandlerHelper.handleCallback(msg, callbackQueryData);
                } catch (TelegramApiException e) {
                    LOGGER.error(String.format("Cannot perform Telegram API operation: %s", e.getMessage()));
                }
            }
            return;
        }

        msg = update.getMessage();

        if (msg == null) {
            LOGGER.warn("Message is null");
            return;
        }

        Long chatId = msg.getChatId();
        String messageText = msg.getText();

        State customCommand = messageText.startsWith("/")
                ? State.findState(messageText.replace("/", ""))
                : State.findStateByDescription(messageText);
        if (customCommand != null) {
            IBotCommand command = getRegisteredCommand(customCommand.getIdentifier());
            currentStateRepository.insertCurrentState(new CurrentState(chatId,
                    Objects.requireNonNull(State.findState(command.getCommandIdentifier())).getIdentifier()));
            command.processMessage(this, msg, null);
            return;
        }

        CurrentState currentState = currentStateRepository.getCurrentState(chatId);
        if (messageText.equals(GO_BACK_BUTTON_TEXT) && currentState != null) {
            IState previousState = State.getPrevState(currentState.getState());
            if (previousState == null) {
                LOGGER.error("Previous state = null, tmp state = {}", currentStateRepository.getCurrentState(chatId)
                        .getState().getIdentifier());
                return;
            }
            currentStateRepository.insertCurrentState(new CurrentState(chatId, previousState.getIdentifier()));
            getRegisteredCommand(previousState.getIdentifier()).processMessage(this, msg, null);
            return;
        }

        if (currentState != null) {
            IState currState = currentState.getState();
            if (currState instanceof State) {
                currState = Substate.nextSubstate(currState);
                currentStateRepository.insertCurrentState(new CurrentState(chatId, currState.getIdentifier()));
            }

            NonCommand.AnswerPair answer = nonCommand.nonCommandExecute(messageText, chatId, currState);
            if (!answer.getError()) {
                currentStateRepository.insertCurrentState(new CurrentState(chatId,
                        Substate.nextSubstate(currState).getIdentifier()));
            }
            sendAnswer(chatId, getUserName(msg), answer.getAnswer());
        }

    }

    private String getUserName(Message msg) {
        User user = msg.getFrom();
        String userName = user.getUserName();
        return (userName != null) ? userName : String.format("%s %s", user.getLastName(), user.getFirstName());
    }

    void sendAnswer(Long chatId, String text) {
        sendAnswer(chatId, null, text);
    }

    private void sendAnswer(Long chatId, String userName, String text) {
        SendMessage answer = new SendMessage();

        if (BUTTONS_TEXT_MAP.containsKey(text)) {
            List<String> commandsList = BUTTONS_TEXT_MAP.get(text);
            answer = replyKeyboard.createSendMessage(chatId, text, commandsList.size(), commandsList,
                    GO_BACK_BUTTON_TEXT);
        } else {
            answer.setParseMode(ParseMode.HTML);
            answer.disableWebPagePreview();
        }
        answer.setChatId(chatId.toString());
        answer.setText(text);

        try {
            execute(answer);
        } catch (TelegramApiException e) {
            if (userName != null) {
                LOGGER.error(String.format("Cannot execute command of user %s: %s", userName, e.getMessage()));
            } else {
                LOGGER.error(String.format("Cannot execute command: %s", e.getMessage()));
            }
        }
    }

    @Override
    public void sendNotification(long userChatId, String message) {
        sendAnswer(userChatId, message);
    }

    private static String messageDebugInfo(Message message) {
        String debugInfo = new ReflectionToStringBuilder(message).toString();
        return DEBUG_INFO_TEXT + message.getChatId() + "\n" + debugInfo;
    }
}
