package polis.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
import polis.commands.GroupDescription;
import polis.commands.MainMenu;
import polis.commands.NonCommand;
import polis.commands.OkAccountDescription;
import polis.commands.OkGroupDescription;
import polis.commands.StartCommand;
import polis.commands.SyncOkTg;
import polis.commands.SyncOkTgDescription;
import polis.commands.TgChannelDescription;
import polis.commands.TgChannelsList;
import polis.commands.TgSyncGroups;
import polis.ok.OKDataCheck;
import polis.util.AuthData;
import polis.util.IState;
import polis.util.SocialMedia;
import polis.util.SocialMediaGroup;
import polis.util.State;
import polis.util.Substate;
import polis.util.TelegramChannel;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

@Component
public class Bot extends TelegramLongPollingCommandBot {
    private final String botName;
    private final String botToken;
    private final OKDataCheck okDataCheck;
    private final NonCommand nonCommand;
    private final Map<Long, List<AuthData>> socialMediaAccounts = new ConcurrentHashMap<>();
    private final Map<Long, List<TelegramChannel>> tgChannels = new ConcurrentHashMap<>();
    private final Map<Long, Long> tgChannelsOwners = new ConcurrentHashMap<>();
    private final Map<Long, IState> currentState = new ConcurrentHashMap<>();
    private final Map<Long, TelegramChannel> currentTgChannel = new ConcurrentHashMap<>();
    private final Map<Long, AuthData> currentSocialMediaAccount = new ConcurrentHashMap<>();
    private final Map<Long, SocialMediaGroup> currentSocialMediaGroup = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(Bot.class);
    public static final String NO_CALLBACK_TEXT = "NO_CALLBACK_TEXT";

    public Bot(@Value("${bot.name}") String botName, @Value("${bot.token}") String botToken) {
        super();
        this.botName = botName;
        this.botToken = botToken;

        okDataCheck = new OKDataCheck(currentSocialMediaAccount, currentState, socialMediaAccounts);
        nonCommand = new NonCommand(
                okDataCheck,
                currentSocialMediaAccount,
                tgChannels,
                currentTgChannel,
                currentSocialMediaGroup,
                socialMediaAccounts);

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
        register(new AddOkGroup(State.AddOkGroup.getIdentifier(), State.AddOkGroup.getDescription()));
        register(new OkGroupDescription(State.OkGroupDescription.getIdentifier(),
                State.OkGroupDescription.getDescription(), currentSocialMediaGroup, currentSocialMediaAccount,
                okDataCheck));
        register(new SyncOkTg(State.SyncOkTg.getIdentifier(), State.SyncOkTg.getDescription(),
                currentTgChannel, currentSocialMediaGroup, currentSocialMediaAccount, okDataCheck));
        register(new SyncOkTgDescription(State.SyncOkTgDescription.getIdentifier(),
                State.SyncOkTgDescription.getDescription(), currentTgChannel, currentSocialMediaGroup,
                currentSocialMediaAccount, okDataCheck));
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
     * @param message отправленное пользователем сообщение
     * @return false, так как боту необходимо всегда обработать входящее сообщение
     */
    @Override
    public boolean filter(Message message) {
        State currentState = State.findState(message.getText().replace("/", ""));
        if (currentState != null) {
            this.currentState.put(message.getChatId(), currentState);
        }
        return false;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        Message msg;
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            msg = callbackQuery.getMessage();
            String data = callbackQuery.getData().replace("/", "");
            if (State.findState(data) == null) {
                try {
                    parseInlineKeyboardData(data, msg);
                } catch (TelegramApiException e) {
                    logger.error(String.format("Cannot perform Telegram API operation: %s", e.getMessage()));
                }

            } else {
                getRegisteredCommand(data).processMessage(this, msg, null);
            }
            return;
        } else {
            msg = update.getMessage();
        }

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
        setAnswer(chatId, getUserName(msg), answer.getAnswer());
    }

    private String getUserName(Message msg) {
        User user = msg.getFrom();
        String userName = user.getUserName();
        return (userName != null) ? userName : String.format("%s %s", user.getLastName(), user.getFirstName());
    }

    private void setAnswer(Long chatId, String userName, String text) {
        SendMessage answer = new SendMessage();
        answer.setText(text);
        answer.setParseMode(ParseMode.HTML);
        answer.setChatId(chatId.toString());
        answer.disableWebPagePreview();

        try {
            execute(answer);
        } catch (TelegramApiException e) {
            logger.error(String.format("Cannot execute command of user %s: %s", userName, e.getMessage()));
        }
    }

    private void parseInlineKeyboardData(String data, Message msg) throws TelegramApiException {
        Long chatId = msg.getChatId();
        String[] dataParts = data.split(" ");
        switch (dataParts[0]) {
            case "tg_channel" -> {
                if (Objects.equals(dataParts[2], "0")) {
                    TelegramChannel currentTelegramChannel = null;
                    for (TelegramChannel ch : tgChannels.get(chatId)) {
                        if (Objects.equals(String.valueOf(ch.getTelegramChannelId()), dataParts[1])) {
                            currentTelegramChannel = ch;
                            break;
                        }
                    }
                    currentTgChannel.put(chatId, currentTelegramChannel);
                    getRegisteredCommand(State.TgChannelDescription.getIdentifier()).processMessage(this, msg,
                            null);
                } else if (Objects.equals(dataParts[2], "1")) {
                    List<TelegramChannel> channels = tgChannels.get(chatId);
                    for (TelegramChannel ch : channels) {
                        if (Objects.equals(String.valueOf(ch.getTelegramChannelId()), dataParts[1])) {
                            channels.remove(ch);
                            break;
                        }
                    }
                    if (channels.size() == 0) {
                        currentTgChannel.remove(chatId);
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
            case "group" -> {
                if (Objects.equals(dataParts[2], "0")) {
                    SocialMediaGroup currentSocialMedia = null;
                    for (SocialMediaGroup smg : currentTgChannel.get(chatId).getSynchronizedGroups()) {
                        if (Objects.equals(String.valueOf(smg.getId()), dataParts[1])) {
                            currentSocialMedia = smg;
                            break;
                        }
                    }
                    currentSocialMediaGroup.put(chatId, currentSocialMedia);
                    getRegisteredCommand(State.GroupDescription.getIdentifier()).processMessage(this, msg,
                            null);
                } else if (Objects.equals(dataParts[2], "1")) {
                    List<SocialMediaGroup> groups = currentTgChannel.get(chatId).getSynchronizedGroups();
                    for (SocialMediaGroup smg : groups) {
                        if (Objects.equals(String.valueOf(smg.getId()), dataParts[1])) {
                            currentTgChannel.get(chatId).deleteGroup(smg);
                            break;
                        }
                    }
                    if (groups.size() == 0) {
                        currentSocialMediaGroup.remove(chatId);
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
            case "account" -> {
                if (Objects.equals(dataParts[1], SocialMedia.OK.getName())) {
                    if (dataParts.length < 5) {
                        logger.error(String.format("Wrong account-callback data: %s", data));
                        return;
                    }
                    currentSocialMediaAccount.put(chatId, new AuthData(SocialMedia.OK, Integer.getInteger(dataParts[2]),
                            dataParts[3], dataParts[4]));
                    getRegisteredCommand(State.OkAccountDescription.getIdentifier())
                            .processMessage(this, msg, null);
                } else {
                    logger.error(String.format("Unknown social media. Inline keyboard data: %s", data));
                }
            }
            case "yesNo" -> {
                if (Objects.equals(dataParts[1], "0")) {
                    currentTgChannel.get(chatId).addGroup(currentSocialMediaGroup.get(chatId));
                    for (TelegramChannel tgChannel : tgChannels.get(chatId)) {
                        if (Objects.equals(tgChannel.getTelegramChannelId(),
                                currentTgChannel.get(chatId).getTelegramChannelId())) {
                            tgChannel.addGroup(currentSocialMediaGroup.get(chatId));
                            break;
                        }
                    }
                    getRegisteredCommand(State.SyncOkTgDescription.getIdentifier())
                            .processMessage(this, msg, null);
                } else {
                    boolean isFound = false;
                    for (TelegramChannel tgChannel : tgChannels.get(chatId)) {
                        if (Objects.equals(tgChannel.getTelegramChannelId(),
                                currentTgChannel.get(chatId).getTelegramChannelId())) {
                            for (SocialMediaGroup group : tgChannel.getSynchronizedGroups()) {
                                if (Objects.equals(group.getId(), currentSocialMediaGroup.get(chatId).getId())) {
                                    tgChannel.deleteGroup(group);
                                    isFound = true;
                                    break;
                                }
                            }
                            if (isFound) {
                                break;
                            }
                        }
                    }
                    getRegisteredCommand(State.OkGroupDescription.getIdentifier())
                            .processMessage(this, msg, null);
                }
            }
            case NO_CALLBACK_TEXT -> {
            }
            default -> logger.error(String.format("Unknown inline keyboard data: %s", data));
        }
    }
}
