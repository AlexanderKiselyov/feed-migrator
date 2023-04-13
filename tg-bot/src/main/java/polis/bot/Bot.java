package polis.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.commands.AccountsList;
import polis.commands.AddGroup;
import polis.commands.AddOkAccount;
import polis.commands.AddOkGroup;
import polis.commands.AddTgChannel;
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
import polis.data.domain.Account;
import polis.keyboards.ReplyKeyboard;
import polis.ok.OKDataCheck;
import polis.ok.api.OkClientImpl;
import polis.data.repositories.AccountsRepository;
import polis.util.AuthData;
import polis.util.IState;
import polis.util.SocialMediaGroup;
import polis.util.State;
import polis.util.Substate;
import polis.util.TelegramChannel;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;
import static polis.ok.OKDataCheck.OK_AUTH_STATE_ANSWER;
import static polis.ok.OKDataCheck.OK_GROUP_ADDED;
import static polis.telegram.TelegramDataCheck.RIGHT_LINK;

@Component
public class Bot extends TelegramLongPollingCommandBot {
    private static final Map<String, List<String>> BUTTONS_TEXT_MAP = Map.of(
            String.format(OK_AUTH_STATE_ANSWER, State.OkAccountDescription.getIdentifier()),
            List.of(State.OkAccountDescription.getDescription()),
            String.format(OK_GROUP_ADDED, State.SyncOkTg.getIdentifier()),
            List.of(State.SyncOkTg.getDescription()),
            RIGHT_LINK,
            List.of(State.TgChannelDescription.getDescription())
    );
    private final String botName;
    private final String botToken;
    private final NonCommand nonCommand;
    private final Map<Long, List<AuthData>> socialMediaAccounts = new ConcurrentHashMap<>();
    private final Map<Long, List<TelegramChannel>> tgChannels = new ConcurrentHashMap<>();
    private final Map<Long, Long> tgChannelOwner = new ConcurrentHashMap<>();
    private final Map<Long, IState> currentState = new ConcurrentHashMap<>();
    private final Map<Long, TelegramChannel> currentTgChannel = new ConcurrentHashMap<>();
    private final Map<Long, AuthData> currentSocialMediaAccount = new ConcurrentHashMap<>();
    private final Map<Long, SocialMediaGroup> currentSocialMediaGroup = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> isAutoposting = new ConcurrentHashMap<>();
    private final OkPostingHelper helper;
    private final Logger logger = LoggerFactory.getLogger(Bot.class);
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

    public Bot(@Value("${bot.name}") String botName, @Value("${bot.token}") String botToken) {
        super();
        this.botName = botName;
        this.botToken = botToken;
        this.helper = new OkPostingHelper(this, botToken, new TgApiHelper(), new OkClientImpl());

        OKDataCheck okDataCheck = new OKDataCheck(currentSocialMediaAccount, currentState, socialMediaAccounts);
        nonCommand = new NonCommand(
                okDataCheck,
                currentSocialMediaAccount,
                tgChannels,
                currentTgChannel,
                currentSocialMediaGroup,
                tgChannelOwner);

        register(new StartCommand(State.Start.getIdentifier(), State.Start.getDescription()));
        register(new AddTgChannel(State.AddTgChannel.getIdentifier(), State.AddTgChannel.getDescription()));
        register(new MainMenu(State.MainMenu.getIdentifier(), State.MainMenu.getDescription()));
        register(new TgChannelDescription(State.TgChannelDescription.getIdentifier(),
                State.TgChannelDescription.getDescription(), currentTgChannel));
        register(new TgChannelsList(State.TgChannelsList.getIdentifier(), State.TgChannelsList.getDescription(),
                tgChannels));
        register(new TgSyncGroups(State.TgSyncGroups.getIdentifier(), State.TgChannelsList.getDescription(),
                currentTgChannel, socialMediaAccounts, okDataCheck));
        register(new GroupDescription(State.GroupDescription.getIdentifier(), State.GroupDescription.getDescription(),
                currentSocialMediaGroup, currentSocialMediaAccount, okDataCheck));
        register(new AddGroup(State.AddGroup.getIdentifier(), State.AddGroup.getDescription(), currentTgChannel));
        register(new AddOkAccount(State.AddOkAccount.getIdentifier(), State.AddOkAccount.getDescription()));
        register(new OkAccountDescription(State.OkAccountDescription.getIdentifier(),
                State.OkAccountDescription.getDescription(), currentSocialMediaAccount, okDataCheck));
        register(new AccountsList(State.AccountsList.getIdentifier(), State.AccountsList.getDescription(),
                socialMediaAccounts, okDataCheck));
        register(new AddOkGroup(State.AddOkGroup.getIdentifier(), State.AddOkGroup.getDescription(), currentTgChannel));
        register(new SyncOkGroupDescription(State.SyncOkGroupDescription.getIdentifier(),
                State.SyncOkGroupDescription.getDescription(), currentTgChannel, currentSocialMediaGroup,
                currentSocialMediaAccount, okDataCheck));
        register(new SyncOkTg(State.SyncOkTg.getIdentifier(), State.SyncOkTg.getDescription(),
                currentTgChannel, currentSocialMediaGroup, currentSocialMediaAccount, okDataCheck));
        register(new Autoposting(State.Autoposting.getIdentifier(), State.Autoposting.getDescription(),
                currentTgChannel, currentSocialMediaGroup, currentSocialMediaAccount, okDataCheck));
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
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
                this.currentState.put(message.getChatId(), currentState);
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
                    logger.error(String.format("Cannot perform Telegram API operation: %s", e.getMessage()));
                }
                return;
            } else if (callbackQueryData.startsWith("/")) {
                getRegisteredCommand(callbackQueryData.replace("/", ""))
                        .processMessage(this, msg, null);
                return;
            }
        }
        msg = update.getMessage();

        Long chatId = msg.getChatId();
        String messageText = msg.getText();

        State customCommand = State.findStateByDescription(messageText);
        if (customCommand != null) {
            IBotCommand command = getRegisteredCommand(customCommand.getIdentifier());
            currentState.put(chatId, State.findState(command.getCommandIdentifier()));
            command.processMessage(this, msg, null);
            return;
        }

        if (messageText.equals(GO_BACK_BUTTON_TEXT)) {
            IState previousState = State.getPrevState(currentState.get(chatId));
            if (previousState == null) {
                logger.error("Previous state = null, tmp state = {}", currentState.get(chatId).getIdentifier());
                return;
            }
            currentState.put(chatId, previousState);
            getRegisteredCommand(previousState.getIdentifier()).processMessage(this, msg, null);
            return;
        }

        IState currState = currentState.get(chatId);
        if (currState instanceof State) {
            currState = Substate.nextSubstate(currState);
            currentState.put(chatId, currState);
        }

        NonCommand.AnswerPair answer = nonCommand.nonCommandExecute(messageText, chatId, currState);
        if (answer.getError()) {
            msg.setText(answer.getAnswer());
            if (currState != null) {
                getRegisteredCommand(currState.getIdentifier()).processMessage(this, msg, null);
                return;
            }
        } else {
            currentState.put(chatId, Substate.nextSubstate(currState));
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
            if (!isAutoposting.containsKey(chatId) || !isAutoposting.get(chatId)) {
                return;
            }
            List<PhotoSize> photos = new ArrayList<>();
            Video video = null;
            String text = null;
            Poll poll = null;
            for (Message postItem : postItems) {
                if (postItem.hasPhoto()) {
                    postItem.getPhoto().stream()
                            .max(Comparator.comparingInt(PhotoSize::getFileSize))
                            .ifPresent(photos::add);
                }
                if (postItem.hasVideo()) {
                    video = postItem.getVideo();
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
            }
            for (TelegramChannel tgChannel : tgChannels.get(tgChannelOwner.get(chatId))) {
                if (!Objects.equals(tgChannel.getTelegramChannelId(), chatId)) {
                    return;
                }
                for (SocialMediaGroup smg : tgChannel.getSynchronizedGroups()) {
                    String accessToken = "";
                    for (AuthData account : socialMediaAccounts.get(tgChannelOwner.get(chatId))) {
                        if (Objects.equals(account.getTokenId(), smg.getTokenId())) {
                            accessToken = account.getAccessToken();
                            break;
                        }
                    }
                    switch (smg.getSocialMedia()) { //Здесь бы смапить группы на подходящие PostingHelper'ы
                        // и с помощью каждого запостить пост
                        case OK -> {
                            try {
                                helper.newPost(chatId, smg.getId(), accessToken)
                                        .addPhotos(photos)
                                        .addVideo(video)
                                        .addText(text)
                                        .addPoll(poll)
                                        .post(accessToken, smg.getId());
                                sendAnswer(chatId, "Успешно опубликовал пост в ok.ru/group/" + smg.getId());
                            } catch (URISyntaxException | IOException ignored) {
                                //Наверное, стоит в принципе не кидать эти исключения из PostingHelper'а
                            }
                        }
                        default -> logger.error(String.format("Social media not found: %s",
                                smg.getSocialMedia()));
                    }
                }
            }
        } catch (Exception e) {
            sendAnswer(chatId, "Произошла непредвиденная ошибка  " + e);
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
            answer = ReplyKeyboard.INSTANCE.createSendMessage(chatId, text, 1, BUTTONS_TEXT_MAP.get(text),
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
                logger.error(String.format("Cannot execute command of user %s: %s", userName, e.getMessage()));
            } else {
                logger.error(String.format("Cannot execute command: %s", e.getMessage()));
            }
        }
    }

    private void parseInlineKeyboardData(String data, Message msg) throws TelegramApiException {
        Long chatId = msg.getChatId();
        String[] dataParts = data.split(" ");
        switch (dataParts[0]) {
            case TG_CHANNEL_CALLBACK_TEXT -> {
                if (Objects.equals(dataParts[2], "0")) {
                    TelegramChannel currentTelegramChannel = null;
                    for (TelegramChannel ch : tgChannels.get(chatId)) {
                        if (Objects.equals(String.valueOf(ch.getTelegramChannelId()), dataParts[1])) {
                            currentTelegramChannel = ch;
                            break;
                        }
                    }
                    if (currentTelegramChannel != null) {
                        currentTgChannel.put(chatId, currentTelegramChannel);
                        getRegisteredCommand(State.TgChannelDescription.getIdentifier()).processMessage(this, msg,
                                null);
                    } else {
                        logger.error(String.format("Cannot find such a telegram channel id: %s", dataParts[1]));
                    }
                } else if (Objects.equals(dataParts[2], "1")) {
                    List<TelegramChannel> channels = tgChannels.get(chatId);
                    for (TelegramChannel ch : channels) {
                        if (Objects.equals(String.valueOf(ch.getTelegramChannelId()), dataParts[1])) {
                            channels.remove(ch);
                            tgChannelOwner.remove(ch.getTelegramChannelId());
                            break;
                        }
                    }
                    if (channels.size() == 0) {
                        currentTgChannel.remove(chatId);
                        for (Map.Entry<Long, Long> entry : tgChannelOwner.entrySet()) {
                            if (Objects.equals(entry.getValue(), chatId)) {
                                tgChannelOwner.remove(entry.getKey());
                            }
                        }
                    }
                    tgChannels.put(chatId, channels);
                    DeleteMessage lastMessage = new DeleteMessage();
                    lastMessage.setChatId(chatId);
                    lastMessage.setMessageId(msg.getMessageId());
                    execute(lastMessage);
                    getRegisteredCommand(State.TgChannelsList.getIdentifier()).processMessage(this, msg, null);
                } else {
                    logger.error(String.format("Wrong Telegram channel data. Inline keyboard data: %s", data));
                }
            }
            case GROUP_CALLBACK_TEXT -> {
                if (Objects.equals(dataParts[2], "0")) {
                    SocialMediaGroup currentSocialMedia = null;
                    for (SocialMediaGroup smg : currentTgChannel.get(chatId).getSynchronizedGroups()) {
                        if (Objects.equals(String.valueOf(smg.getId()), dataParts[1])) {
                            currentSocialMedia = smg;
                            break;
                        }
                    }
                    if (currentSocialMedia != null) {
                        currentSocialMediaGroup.put(chatId, currentSocialMedia);
                        getRegisteredCommand(State.GroupDescription.getIdentifier()).processMessage(this, msg,
                                null);
                    } else {
                        logger.error(String.format("Cannot find such a social media group id: %s", dataParts[1]));
                    }
                } else if (Objects.equals(dataParts[2], "1")) {
                    boolean isFound = false;
                    for (TelegramChannel tgChannel : tgChannels.get(chatId)) {
                        if (Objects.equals(tgChannel.getTelegramChannelId(),
                                currentTgChannel.get(chatId).getTelegramChannelId())) {
                            for (SocialMediaGroup smg : tgChannel.getSynchronizedGroups()) {
                                if (Objects.equals(String.valueOf(smg.getId()), dataParts[1])) {
                                    currentTgChannel.get(chatId).deleteGroup(smg.getId());
                                    tgChannel.deleteGroup(smg.getId());
                                    isFound = true;
                                    break;
                                }
                            }
                            if (isFound) {
                                break;
                            }
                        }
                    }
                    DeleteMessage lastMessage = new DeleteMessage();
                    lastMessage.setChatId(chatId);
                    lastMessage.setMessageId(msg.getMessageId());
                    execute(lastMessage);
                    getRegisteredCommand(State.TgSyncGroups.getIdentifier()).processMessage(this, msg, null);
                } else {
                    logger.error(String.format("Wrong group data. Inline keyboard data: %s", data));
                }
            }
            case ACCOUNT_CALLBACK_TEXT -> {
                if (dataParts.length < 2) {
                    logger.error(String.format("Wrong account-callback data: %s", data));
                    return;
                }
                for (AuthData account : socialMediaAccounts.get(chatId)) {
                    if (Objects.equals(String.valueOf(account.getTokenId()), dataParts[1])) {
                        currentSocialMediaAccount.put(chatId, account);
                        break;
                    }
                }

                getRegisteredCommand(State.OkAccountDescription.getIdentifier())
                        .processMessage(this, msg, null);
            }
            case YES_NO_CALLBACK_TEXT -> {
                if (Objects.equals(dataParts[1], "0")) {
                    SocialMediaGroup currentGroup = currentSocialMediaGroup.get(chatId);
                    currentTgChannel.get(chatId).addGroup(currentGroup);
                    boolean isFound = false;
                    for (AuthData authData : socialMediaAccounts.get(chatId)) {
                        if (Objects.equals(authData.getAccessToken(),
                                currentSocialMediaAccount.get(chatId).getAccessToken())) {
                            for (TelegramChannel tgChannel : tgChannels.get(chatId)) {
                                if (Objects.equals(tgChannel.getTelegramChannelId(),
                                        currentTgChannel.get(chatId).getTelegramChannelId())) {
                                    tgChannel.addGroup(new SocialMediaGroup(currentGroup.getId(),
                                            authData.getTokenId(), authData.getSocialMedia()));
                                    isFound = true;
                                    break;
                                }
                            }
                            if (isFound) {
                                break;
                            }
                        }
                    }
                    getRegisteredCommand(State.SyncOkGroupDescription.getIdentifier())
                            .processMessage(this, msg, null);
                } else {
                    boolean isFound = false;
                    for (TelegramChannel tgChannel : tgChannels.get(chatId)) {
                        if (Objects.equals(tgChannel.getTelegramChannelId(),
                                currentTgChannel.get(chatId).getTelegramChannelId())) {
                            for (SocialMediaGroup smg : tgChannel.getSynchronizedGroups()) {
                                if (Objects.equals(String.valueOf(smg.getId()), dataParts[1])) {
                                    currentTgChannel.get(chatId).deleteGroup(smg.getId());
                                    tgChannel.deleteGroup(smg.getId());
                                    isFound = true;
                                    break;
                                }
                            }
                            if (isFound) {
                                break;
                            }
                        }
                    }
                    currentSocialMediaGroup.remove(chatId);
                    getRegisteredCommand(State.OkAccountDescription.getIdentifier())
                            .processMessage(this, msg, null);
                }
            }
            case AUTOPOSTING -> {
                String enable = "включена";
                if (Objects.equals(dataParts[2], "0")) {
                    isAutoposting.put(Long.parseLong(dataParts[1]), true);
                } else {
                    isAutoposting.put(Long.parseLong(dataParts[1]), false);
                    enable = "выключена";
                }
                sendAnswer(chatId, String.format(AUTOPOSTING_ENABLE, enable));
                getRegisteredCommand(State.MainMenu.getIdentifier()).processMessage(this, msg, null);
            }
            case NO_CALLBACK_TEXT -> {

            }
            default -> logger.error(String.format("Unknown inline keyboard data: %s", data));
        }
    }
}
