package polis.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
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
import polis.commands.OkAccountDescription;
import polis.commands.StartCommand;
import polis.commands.SyncOkGroupDescription;
import polis.commands.SyncOkTg;
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
import polis.ok.api.OkAuthorizator;
import polis.ok.api.OkClientImpl;
import polis.ok.api.exceptions.OkApiException;
import polis.ok.api.exceptions.TokenExpiredException;
import polis.posting.ApiException;
import polis.posting.OkPostingHelper;
import polis.posting.PostingHelper;
import polis.posting.TgApiHelper;
import polis.util.IState;
import polis.util.State;
import polis.util.Substate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
@Component
public class Bot extends TelegramLongPollingCommandBot {
    public static final List<String> EMPTY_LIST = List.of();
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
            EMPTY_LIST
    );

    private final String botName;
    private final String botToken;
    private final OkPostingHelper helper;
    private final OkAuthorizator okAuthorizator = null;

    private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);
    private static final String TG_CHANNEL_CALLBACK_TEXT = "tg_channel";
    private static final String GROUP_CALLBACK_TEXT = "group";
    private static final String ACCOUNT_CALLBACK_TEXT = "account";
    private static final String YES_NO_CALLBACK_TEXT = "yesNo";
    private static final String AUTOPOSTING = "autoposting";
    private static final String NO_CALLBACK_TEXT = "NO_CALLBACK_TEXT";
    private static final String AUTOPOSTING_ENABLE = "Функция автопостинга %s.";
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
    private SyncOkGroupDescription syncOkGroupDescription;

    @Autowired
    private SyncOkTg syncOkTg;

    @Autowired
    private Autoposting autoposting;

    @Autowired
    private VkAccountDescription vkAccountDescription;

    @Autowired
    private AddVkGroup addVkGroup;

    public Bot(@Value("${bot.name}") String botName, @Value("${bot.token}") String botToken) {
        super();
        this.botName = botName;
        this.botToken = botToken;
        this.helper = new OkPostingHelper(
                new TgApiHelper(botToken, this::downloadFile),
                new OkClientImpl()
        );
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
        register(syncOkGroupDescription);
        register(syncOkTg);
        register(autoposting);
        register(new AddVkAccount());
        register(vkAccountDescription);
        register(addVkGroup);
    }

    /**
     * Устанавливает бота в определенное состояние в зависимости от введенной пользователем команды.
     *
     * @param message отправленное пользователем сообщение
     * @return false, так как боту необходимо всегда обработать входящее сообщение
     */
    @Override
    public boolean filter(Message message) {
        if (message != null) {
            State currentState = State.findState(message.getText().replace("/", ""));
            if (currentState != null) {
                currentStateRepository.insertCurrentState(new CurrentState(message.getChatId(),
                        currentState.getIdentifier()));
            }
        }
        return false;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        Message msg;
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            msg = callbackQuery.getMessage();
            String callbackQueryData = callbackQuery.getData();
            if (callbackQueryData.startsWith(ACCOUNT_CALLBACK_TEXT) || callbackQueryData.startsWith(GROUP_CALLBACK_TEXT)
                    || callbackQueryData.startsWith(TG_CHANNEL_CALLBACK_TEXT)
                    || callbackQueryData.startsWith(YES_NO_CALLBACK_TEXT)
                    || callbackQueryData.startsWith(AUTOPOSTING)
                    || callbackQueryData.equals(NO_CALLBACK_TEXT)) {
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

        updates.get(!channelPosts).forEach(update -> filter(update.getMessage()));
        updates.get(!channelPosts).forEach(this::processNonCommandUpdate);
        updates.get(channelPosts).stream()
                .map(Update::getChannelPost)
                .collect(Collectors.groupingBy(Message::getChatId))
                .values()
                .forEach(this::processPostsInChannel);
        //То, что сейчас делает forEach, потом следует сабмитить в executor
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
        long chatId = postItems.get(0).getChatId();
        try {
            List<PhotoSize> photos = new ArrayList<>(1);
            List<Video> videos = new ArrayList<>(1);
            String text = null;
            Poll poll = null;
            List<Animation> animations = new ArrayList<>(1);
            List<Document> documents = new ArrayList<>(1);
            for (Message postItem : postItems) {
                if (postItem.hasPhoto()) {
                    postItem.getPhoto().stream()
                            .max(Comparator.comparingInt(PhotoSize::getFileSize))
                            .ifPresent(photos::add);
                }
                if (postItem.hasVideo()) {
                    videos.add(postItem.getVideo());
                }
                if (postItem.getCaption() != null && !postItem.getCaption().isEmpty()) {
                    text = postItem.getCaption();
                }
                if (postItem.hasText() && !postItem.getText().isEmpty()) {
                    text = postItem.getText();
                }
                if (postItem.hasPoll()) {
                    poll = postItem.getPoll();
                }
                if (postItem.hasAnimation()) {
                    animations.add(postItem.getAnimation());
                }
                if (postItem.hasDocument()) {
                    documents.add(postItem.getDocument());
                }
            }
            long userChatId = userChannelsRepository.getUserChatId(chatId);
            List<UserChannels> tgChannels = userChannelsRepository.getUserChannels(userChatId);
            for (UserChannels tgChannel : tgChannels) {
                if (!Objects.equals(tgChannel.getChannelId(), chatId)) {
                    return;
                }
                if (!tgChannel.isAutoposting()) {
                    return;
                }
                for (ChannelGroup group : channelGroupsRepository.getGroupsForChannel(tgChannel.getChannelId())) {
                    switch (group.getSocialMedia()) { //Здесь бы смапить группы на подходящие PostingHelper'ы
                        // и с помощью каждого запостить пост
                        case OK -> {

                            if (!documents.isEmpty() && animations.isEmpty()) {
                                sendAnswer(chatId, """
                                        Тип 'Документ' не поддерживается в социальной сети Одноклассники""");
                            }
                            PostingHelper.Post post;
                            try {
                                post = helper.newPost(group.getGroupId(), group.getAccessToken())
                                        .addPhotos(photos)
                                        .addVideos(videos)
                                        .addText(text)
                                        .addPoll(poll)
                                        .addAnimations(animations);
                            } catch (ApiException | IOException | TelegramApiException | URISyntaxException ignored) {
                                return; //TODO log
                            }
                            doPostSafely(post, group);
                        }
                        default -> LOGGER.error(String.format("Social media not found: %s",
                                group.getSocialMedia()));
                    }
                }
            }
        } catch (RuntimeException e) {
            sendAnswer(chatId, "Произошла непредвиденная ошибка  " + e);
        }
    }

    private void doPostSafely(PostingHelper.Post post, ChannelGroup group) {
        try {
            post.post();
        } catch (URISyntaxException | IOException e) {
            //TODO log
        } catch (ApiException e) {
            if (e.getCause() instanceof TokenExpiredException) {
                Account account = accountsRepository.getAccountByKey(group.getChatId(),
                        group.getSocialMedia().toString(), group.getAccountId());
                String token;
                try {
                    token = refreshToken(group, account);
                } catch (URISyntaxException | IOException | OkApiException ex) {
                    return; //TODO log
                }
                try {
                    post.post(token);
                } catch (URISyntaxException | IOException | ApiException ex) {
                    //TODO log
                }
            }
        }
    }

    private String refreshToken(ChannelGroup group, Account account) throws URISyntaxException, IOException,
            OkApiException {
        OkAuthorizator.TokenPair tokenPair = okAuthorizator.refreshToken(account.getRefreshToken());

        account.setRefreshToken(tokenPair.refreshToken());
        account.setAccessToken(tokenPair.accessToken());
        group.setAccessToken(tokenPair.accessToken());
        accountsRepository.insertNewAccount(account);
        channelGroupsRepository.insertChannelGroup(group);

        return tokenPair.accessToken();
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
                    boolean isFound = false;
                    List<UserChannels> tgChannels = userChannelsRepository.getUserChannels(chatId);
                    for (UserChannels tgChannel : tgChannels) {
                        if (Objects.equals(tgChannel.getChannelId(),
                                currentChannelRepository.getCurrentChannel(chatId).getChannelId())) {
                            for (ChannelGroup smg : channelGroupsRepository
                                    .getGroupsForChannel(tgChannel.getChannelId())) {
                                if (Objects.equals(String.valueOf(smg.getAccountId()), dataParts[1])) {
                                    channelGroupsRepository.deleteChannelGroup(smg.getAccountId(),
                                            smg.getSocialMedia().getName(), smg.getGroupId());
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
                    getRegisteredCommand(State.TgSyncGroups.getIdentifier()).processMessage(this, msg, null);
                } else if (Objects.equals(dataParts[2], "2")) {
                    changeCurrentSocialMediaGroupAndExecuteCommand(chatId, dataParts, msg, State.Autoposting);
                } else {
                    LOGGER.error(String.format("Wrong group data. Inline keyboard data: %s", data));
                }
            }
            case ACCOUNT_CALLBACK_TEXT -> {
                if (dataParts.length < 2) {
                    LOGGER.error(String.format("Wrong account-callback data: %s", data));
                    return;
                }
                for (Account account : accountsRepository.getAccountsForUser(chatId)) {
                    if (Objects.equals(String.valueOf(account.getAccountId()), dataParts[1])) {
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
                }
                deleteLastMessage(msg, chatId);
                getRegisteredCommand(State.OkAccountDescription.getIdentifier())
                        .processMessage(this, msg, null);
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
                    getRegisteredCommand(State.SyncOkGroupDescription.getIdentifier())
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
                sendAnswer(chatId, String.format(AUTOPOSTING_ENABLE, enable));
                deleteLastMessage(msg, chatId);
                getRegisteredCommand(State.TgChannelDescription.getIdentifier()).processMessage(this, msg, null);
            }
            case NO_CALLBACK_TEXT -> deleteLastMessage(msg, chatId);
            default -> LOGGER.error(String.format("Unknown inline keyboard data: %s", data));
        }
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
}
