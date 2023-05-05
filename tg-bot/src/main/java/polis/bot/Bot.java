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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
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
import polis.data.domain.Account;
import polis.data.domain.ChannelGroup;
import polis.data.domain.CurrentAccount;
import polis.data.domain.CurrentChannel;
import polis.data.domain.CurrentGroup;
import polis.data.domain.CurrentState;
import polis.data.domain.UserChannels;
import polis.data.repositories.AccountsRepository;
import polis.data.repositories.ChannelGroupsRepository;
import polis.data.repositories.CurrentAccountRepository;
import polis.data.repositories.CurrentChannelRepository;
import polis.data.repositories.CurrentGroupRepository;
import polis.data.repositories.CurrentStateRepository;
import polis.data.repositories.UserChannelsRepository;
import polis.keyboards.ReplyKeyboard;
import polis.posting.ok.OkPostProcessor;
import polis.posting.vk.VkPostProcessor;
import polis.util.IState;
import polis.util.SocialMedia;
import polis.util.State;
import polis.util.Substate;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static polis.datacheck.OkDataCheck.OK_AUTH_STATE_ANSWER;
import static polis.datacheck.OkDataCheck.OK_AUTH_STATE_SERVER_EXCEPTION_ANSWER;
import static polis.datacheck.OkDataCheck.OK_AUTH_STATE_WRONG_AUTH_CODE_ANSWER;
import static polis.datacheck.OkDataCheck.OK_GROUP_ADDED;
import static polis.datacheck.OkDataCheck.USER_HAS_NO_RIGHTS;
import static polis.datacheck.OkDataCheck.WRONG_LINK_OR_USER_HAS_NO_RIGHTS;
import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;
import static polis.telegram.TelegramDataCheck.BOT_NOT_ADMIN;
import static polis.telegram.TelegramDataCheck.RIGHT_LINK;
import static polis.telegram.TelegramDataCheck.WRONG_LINK_OR_BOT_NOT_ADMIN;

@Configuration
@Component("Bot")
public class Bot extends TelegramLongPollingCommandBot implements TgFileLoader, TgNotificator {
    private static final List<String> EMPTY_LIST = List.of();
    private static final String TURN_ON_NOTIFICATIONS_MSG = "\nВы также можете включить уведомления, чтобы быть в "
            + "курсе автоматически опубликованных записей с помощью команды /notifications";
    private static final String AUTOPOSTING_ENABLE_AND_NOTIFICATIONS = "Функция автопостинга включена."
            + TURN_ON_NOTIFICATIONS_MSG;
    private static final Map<String, List<String>> BUTTONS_TEXT_MAP = Map.of(
            String.format(OK_AUTH_STATE_ANSWER, State.OkAccountDescription.getIdentifier()),
            List.of(State.OkAccountDescription.getDescription()),
            String.format(OK_GROUP_ADDED, State.SyncOkTg.getIdentifier()),
            List.of(State.SyncOkTg.getDescription()),
            RIGHT_LINK,
            List.of(State.TgChannelDescription.getDescription()),
            OK_AUTH_STATE_WRONG_AUTH_CODE_ANSWER,
            EMPTY_LIST,
            OK_AUTH_STATE_SERVER_EXCEPTION_ANSWER,
            EMPTY_LIST,
            WRONG_LINK_OR_USER_HAS_NO_RIGHTS,
            EMPTY_LIST,
            USER_HAS_NO_RIGHTS,
            EMPTY_LIST,
            WRONG_LINK_OR_BOT_NOT_ADMIN,
            EMPTY_LIST,
            BOT_NOT_ADMIN,
            EMPTY_LIST,
            AUTOPOSTING_ENABLE_AND_NOTIFICATIONS,
            List.of(State.Notifications.getDescription())
    );
    private final String botName;
    private final String botToken;
    private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);
    private static final String TG_CHANNEL_CALLBACK_TEXT = "tg_channel";
    private static final String GROUP_CALLBACK_TEXT = "group";
    private static final String ACCOUNT_CALLBACK_TEXT = "account";
    private static final String YES_NO_CALLBACK_TEXT = "yesNo";
    private static final String AUTOPOSTING = "autoposting";
    private static final String NOTIFICATIONS = "notifications";
    private static final String NO_CALLBACK_TEXT = "NO_CALLBACK_TEXT";
    private static final String AUTOPOSTING_ENABLE = "Функция автопостинга %s.";
    private static final String ERROR_POST_MSG = "Упс, что-то пошло не так \uD83D\uDE1F \n"
            + "Не удалось опубликовать пост в ok.ru/group/";
    private static final String SINGLE_ITEM_POSTS = "";

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

    @Lazy
    @Autowired
    private OkPostProcessor okPostProcessor;

    @Lazy
    @Autowired
    private VkPostProcessor vkPostProcessor;

    @Lazy
    @Autowired
    private TgContentManager tgContentManager;

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

    /**
     * Устанавливает бота в определенное состояние в зависимости от введенной пользователем команды.
     *
     * @param message отправленное пользователем сообщение
     */
    public void setStateForMessage(Message message) {
        if (message == null) {
            LOGGER.warn("Received null message");
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
            if (callbackQueryData.startsWith(ACCOUNT_CALLBACK_TEXT) || callbackQueryData.startsWith(GROUP_CALLBACK_TEXT)
                    || callbackQueryData.startsWith(TG_CHANNEL_CALLBACK_TEXT)
                    || callbackQueryData.startsWith(YES_NO_CALLBACK_TEXT)
                    || callbackQueryData.startsWith(AUTOPOSTING)
                    || callbackQueryData.equals(NO_CALLBACK_TEXT)
                    || callbackQueryData.startsWith(NOTIFICATIONS)) {
                try {
                    parseInlineKeyboardData(callbackQueryData, msg);
                } catch (TelegramApiException e) {
                    LOGGER.error(String.format("Cannot perform Telegram API operation: %s", e.getMessage()));
                }
                return;
            } else if (callbackQueryData.startsWith("/")) {
                getRegisteredCommand(callbackQueryData.replace("/", ""))
                        .processMessage(this, msg, null);
                return;
            }
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

        if (messageText.equals(GO_BACK_BUTTON_TEXT)) {
            IState previousState = State.getPrevState(currentStateRepository.getCurrentState(chatId).getState());
            if (previousState == null) {
                LOGGER.error("Previous state = null, tmp state = {}", currentStateRepository.getCurrentState(chatId)
                        .getState().getIdentifier());
                return;
            }
            currentStateRepository.insertCurrentState(new CurrentState(chatId, previousState.getIdentifier()));
            getRegisteredCommand(previousState.getIdentifier()).processMessage(this, msg, null);
            return;
        }

        IState currState = currentStateRepository.getCurrentState(chatId).getState();
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

    @Override
    public void onUpdatesReceived(List<Update> overallUpdates) {
        Map<Boolean, List<Update>> updates = overallUpdates.stream()
                .collect(Collectors.partitioningBy(Update::hasChannelPost));
        boolean channelPosts = true;

        updates.get(!channelPosts).forEach(this::processNonCommandUpdate);
        updates.get(channelPosts).stream()
                .map(Update::getChannelPost)
                .collect(Collectors.groupingBy(Message::getChatId))
                .values()
                .forEach(this::processPostsInChannel);
    }

    private void processPostsInChannel(List<Message> channelPosts) {
        Map<String, List<Message>> posts = channelPosts.stream().collect(
                Collectors.groupingBy(
                        post -> post.getMediaGroupId() == null ? SINGLE_ITEM_POSTS : post.getMediaGroupId(),
                        Collectors.toList()
                ));
        posts.getOrDefault(SINGLE_ITEM_POSTS, Collections.emptyList())
                .forEach(post -> processPostItems(Collections.singletonList(post)));
        posts.remove(SINGLE_ITEM_POSTS); // :)
        posts.values().forEach(this::processPostItems);
    }

    private void processPostItems(List<Message> postItems) {
        long channelId = postItems.get(0).getChatId();
        long ownerChatId = userChannelsRepository.getUserChatId(channelId);
        try {
            if (!userChannelsRepository.isSetAutoposting(ownerChatId, channelId)) {
                return;
            }
            long userChatId = userChannelsRepository.getUserChatId(channelId);
            UserChannels tgChannel = userChannelsRepository.getUserChannel(channelId, userChatId);
            if (!tgChannel.isAutoposting()) {
                return;
            }
            for (ChannelGroup group : channelGroupsRepository.getGroupsForChannel(tgChannel.getChannelId())) {
                String accessToken = null;
                Long userId = null;
                for (Account account : accountsRepository.getAccountsForUser(userChatId)) {
                    if (Objects.equals(account.getAccountId(), group.getAccountId())) {
                        accessToken = account.getAccessToken();
                        userId = account.getAccountId();
                        break;
                    }
                }

                if (accessToken == null) {
                    sendAnswer(ownerChatId, "Аккаунт не был найден.");
                    return;
                }

                switch (group.getSocialMedia()) {
                    case OK -> okPostProcessor.processPostInChannel(postItems, ownerChatId, group.getGroupId(),
                            channelId, userId, accessToken);
                    case VK -> vkPostProcessor.processPostInChannel(postItems, ownerChatId, group.getGroupId(),
                            channelId, userId, accessToken);
                    default -> {
                        LOGGER.error(String.format("Social media not found: %s",
                                group.getSocialMedia()));
                        checkAndSendNotification(ownerChatId, channelId, ERROR_POST_MSG + group.getGroupId());
                    }

                }
            }
        } catch (RuntimeException e) {
            LOGGER.error("Error when handling post in " + channelId, e);
            sendAnswer(ownerChatId, "Произошла непредвиденная ошибка при обработке поста " + e);
        }
    }

    private void checkAndSendNotification(long userChatId, long channelId, String message) {
        if (userChannelsRepository.isSetNotification(userChatId, channelId)) {
            sendAnswer(userChatId, message);
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
            answer = ReplyKeyboard.INSTANCE.createSendMessage(chatId, text, commandsList.size(), commandsList,
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

    private void parseInlineKeyboardData(String data, Message msg) throws TelegramApiException {
        Long chatId = msg.getChatId();
        String[] dataParts = data.split(" ");
        switch (dataParts[0]) {
            case TG_CHANNEL_CALLBACK_TEXT -> {
                if (Objects.equals(dataParts[2], "0")) {
                    UserChannels currentTelegramChannel = null;
                    List<UserChannels> tgChannels = userChannelsRepository.getUserChannels(chatId);
                    for (UserChannels ch : tgChannels) {
                        if (Objects.equals(String.valueOf(ch.getChannelId()), dataParts[1])) {
                            currentTelegramChannel = ch;
                            break;
                        }
                    }
                    if (currentTelegramChannel != null) {
                        currentChannelRepository.insertCurrentChannel(new CurrentChannel(chatId,
                                currentTelegramChannel.getChannelId(), currentTelegramChannel.getChannelUsername()));
                        deleteLastMessage(msg, chatId);
                        getRegisteredCommand(State.TgChannelDescription.getIdentifier()).processMessage(this, msg,
                                null);
                    } else {
                        LOGGER.error(String.format("Cannot find such a telegram channel id: %s", dataParts[1]));
                    }
                } else if (Objects.equals(dataParts[2], "1")) {
                    List<UserChannels> tgChannels = userChannelsRepository.getUserChannels(chatId);
                    for (UserChannels ch : tgChannels) {
                        if (Objects.equals(String.valueOf(ch.getChannelId()), dataParts[1])) {
                            userChannelsRepository.deleteUserChannel(ch);
                            break;
                        }
                    }
                    if (tgChannels.size() == 0) {
                        currentChannelRepository.deleteCurrentChannel(chatId);
                    }
                    deleteLastMessage(msg, chatId);
                    getRegisteredCommand(State.TgChannelsList.getIdentifier()).processMessage(this, msg, null);
                } else {
                    LOGGER.error(String.format("Wrong Telegram channel data. Inline keyboard data: %s", data));
                }
            }
            case GROUP_CALLBACK_TEXT -> {
                if (Objects.equals(dataParts[2], "0")) {
                    changeCurrentSocialMediaGroupAndExecuteCommand(chatId, dataParts, msg, State.GroupDescription);
                } else if (Objects.equals(dataParts[2], "1")) {
                    CurrentChannel currentChannel = currentChannelRepository.getCurrentChannel(chatId);
                    CurrentAccount currentAccount = currentAccountRepository.getCurrentAccount(chatId);
                    channelGroupsRepository.deleteChannelGroup(currentChannel.getChannelId(),
                            currentAccount.getSocialMedia());
                    currentGroupRepository.deleteCurrentGroup(chatId);
                    deleteLastMessage(msg, chatId);
                    getRegisteredCommand(State.TgSyncGroups.getIdentifier()).processMessage(this, msg, null);
                } else {
                    LOGGER.error(String.format("Wrong group data. Inline keyboard data: %s", data));
                }
            }
            case ACCOUNT_CALLBACK_TEXT -> {
                if (dataParts.length < 3) {
                    LOGGER.error(String.format("Wrong account-callback data: %s", data));
                    return;
                }
                boolean shouldDelete = dataParts[2].equals("1");
                String currentAccountSocialMedia = currentAccountRepository.getCurrentAccount(chatId).getSocialMedia();
                State state = shouldDelete ? State.AddGroup :
                        (currentAccountSocialMedia.equals(SocialMedia.OK.getName()) ? State.OkAccountDescription
                                : State.VkAccountDescription);
                processAccountCallback(msg, chatId, dataParts, state, shouldDelete);
                currentStateRepository.insertCurrentState(new CurrentState(
                        chatId,
                        state.getIdentifier()
                ));
            }
            case YES_NO_CALLBACK_TEXT -> {
                if (Objects.equals(dataParts[1], "0")) {
                    CurrentGroup currentGroup = currentGroupRepository.getCurrentGroup(chatId);
                    boolean isFound = false;
                    for (Account authData : accountsRepository.getAccountsForUser(chatId)) {
                        if (Objects.equals(authData.getAccessToken(),
                                currentAccountRepository.getCurrentAccount(chatId).getAccessToken())) {
                            List<UserChannels> tgChannels = userChannelsRepository.getUserChannels(chatId);
                            for (UserChannels tgChannel : tgChannels) {
                                if (Objects.equals(tgChannel.getChannelId(),
                                        currentChannelRepository.getCurrentChannel(chatId).getChannelId())) {
                                    channelGroupsRepository.insertChannelGroup(
                                            new ChannelGroup(currentGroup.getAccessToken(),
                                                    currentGroup.getGroupName(),
                                                    authData.getAccountId(),
                                                    currentGroup.getChatId(),
                                                    currentGroup.getGroupId(),
                                                    authData.getSocialMedia().getName()
                                            ).setChannelId(tgChannel.getChannelId())
                                                    .setChannelUsername(tgChannel.getChannelUsername())
                                    );
                                    isFound = true;
                                    break;
                                }
                            }
                            if (isFound) {
                                break;
                            }
                        }
                    }
                    deleteLastMessage(msg, chatId);
                    getRegisteredCommand(State.SyncGroupDescription.getIdentifier())
                            .processMessage(this, msg, null);
                } else {
                    currentGroupRepository.deleteCurrentGroup(chatId);
                    deleteLastMessage(msg, chatId);
                    getRegisteredCommand(State.OkAccountDescription.getIdentifier())
                            .processMessage(this, msg, null);
                }
            }
            case AUTOPOSTING -> {
                String enable = "включена";
                if (Objects.equals(dataParts[3], "0")) {
                    userChannelsRepository.setAutoposting(chatId, Long.parseLong(dataParts[2]), false);
                    enable = "выключена";
                } else {
                    userChannelsRepository.setAutoposting(chatId, Long.parseLong(dataParts[2]), true);
                }
                deleteLastMessage(msg, chatId);
                String text = String.format(AUTOPOSTING_ENABLE, enable);
                if ("включена".equals(enable)) {
                    text += TURN_ON_NOTIFICATIONS_MSG;
                }
                sendAnswer(chatId, text);
            }
            case NOTIFICATIONS -> {
                boolean areEnable = Objects.equals(dataParts[2], "0");
                userChannelsRepository.setNotification(chatId, Long.parseLong(dataParts[1]), areEnable);
                sendAnswer(chatId, String.format("Уведомления %s.", (areEnable ? "включены" : "выключены")));
                deleteLastMessage(msg, chatId);
                currentStateRepository.insertCurrentState(new CurrentState(
                        chatId,
                        State.GroupDescription.getIdentifier()
                ));
                getRegisteredCommand(State.GroupDescription.getIdentifier()).processMessage(this, msg, null);
            }
            case NO_CALLBACK_TEXT -> deleteLastMessage(msg, chatId);
            default -> LOGGER.error(String.format("Unknown inline keyboard data: %s", data));
        }
    }

    private void processAccountCallback(Message msg, Long chatId, String[] dataParts, State state, boolean shouldDelete)
            throws TelegramApiException {
        for (Account account : accountsRepository.getAccountsForUser(chatId)) {
            if (Objects.equals(String.valueOf(account.getAccountId()), dataParts[1])) {
                if (!shouldDelete) {
                    currentAccountRepository.insertCurrentAccount(
                            new CurrentAccount(
                                    chatId,
                                    account.getSocialMedia().getName(),
                                    account.getAccountId(),
                                    account.getUserFullName(),
                                    account.getAccessToken(),
                                    account.getRefreshToken()
                            )
                    );
                    break;
                }
                currentGroupRepository.deleteCurrentGroup(chatId);
                currentAccountRepository.deleteCurrentAccount(chatId);
                List<UserChannels> userChannels = userChannelsRepository.getUserChannels(chatId);
                for (UserChannels userChannel : userChannels) {
                    channelGroupsRepository.deleteChannelGroup(userChannel.getChannelId(),
                            account.getSocialMedia().getName());
                }
                accountsRepository.deleteAccount(chatId, account.getAccountId(), account.getSocialMedia().getName());
                break;
            }
        }
        deleteLastMessage(msg, chatId);
        getRegisteredCommand(state.getIdentifier()).processMessage(this, msg, null);
    }

    private void changeCurrentSocialMediaGroupAndExecuteCommand(Long chatId, String[] dataParts, Message msg,
                                                                State command) throws TelegramApiException {
        CurrentGroup currentGroup = getCurrentGroup(chatId, dataParts);
        if (currentGroup != null) {
            currentGroupRepository.insertCurrentGroup(currentGroup);
            deleteLastMessage(msg, chatId);
            getRegisteredCommand(command.getIdentifier()).processMessage(this, msg,
                    null);
        } else {
            LOGGER.error(String.format("Cannot find such a social media group id: %s", dataParts[1]));
        }
    }

    private CurrentGroup getCurrentGroup(Long chatId, String[] dataParts) {
        CurrentGroup currentSocialMedia = null;
        for (ChannelGroup smg : channelGroupsRepository
                .getGroupsForChannel(currentChannelRepository.getCurrentChannel(chatId).getChannelId())) {
            if (Objects.equals(String.valueOf(smg.getGroupId()), dataParts[1])) {
                currentSocialMedia = new CurrentGroup(smg.getChatId(), smg.getSocialMedia().getName(), smg.getGroupId(),
                        smg.getGroupName(), smg.getAccountId(), smg.getAccessToken());
                break;
            }
        }
        return currentSocialMedia;
    }

    private void deleteLastMessage(Message msg, Long chatId) throws TelegramApiException {
        DeleteMessage lastMessage = new DeleteMessage();
        lastMessage.setChatId(chatId);
        lastMessage.setMessageId(msg.getMessageId());
        execute(lastMessage);
    }

    @Override
    public File downloadFileById(String fileId) throws URISyntaxException, IOException, TelegramApiException {
        TgContentManager.GetFilePathResponse pathResponse = tgContentManager.retrieveFilePath(botToken, fileId);
        String tgApiFilePath = pathResponse.getFilePath();
        File file = downloadFile(tgApiFilePath);
        return TgContentManager.fileWithOrigExtension(tgApiFilePath, file);
    }

    @Override
    public void sendNotification(long userChatId, long channelId, String message) {
        checkAndSendNotification(userChatId, channelId, message);
    }

    private static String messageDebugInfo(Message message) {
        String debugInfo = new ReflectionToStringBuilder(message).toString();
        return "Update from " + message.getChatId() + "\n" + debugInfo;
    }
}
