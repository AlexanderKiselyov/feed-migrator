package polis.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.commands.NonCommand;
import polis.commands.OkAuthCommand;
import polis.commands.StartCommand;
import polis.commands.SyncCommand;
import polis.ok.api.OkClientImpl;
import polis.util.AuthData;
import polis.util.IState;
import polis.util.State;
import polis.util.Substate;

import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

@Component
public class Bot extends TelegramLongPollingCommandBot {
    private final String botName;
    private final String botToken;
    private final NonCommand nonCommand;
    private final Map<Long, IState> states = new ConcurrentHashMap<>();
    private final Map<Long, List<AuthData>> socialMedia = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(Bot.class);
    private final Properties properties = new Properties();
    private final OkPostingHelper helper;

    public Bot(@Value("${bot.name}") String botName, @Value("${bot.token}") String botToken) {
        super();
        this.botName = botName;
        this.botToken = botToken;
        this.helper = new OkPostingHelper(this, botToken, new TgApiHelper(), new OkClientImpl());

        loadProperties();

        nonCommand = new NonCommand(states, socialMedia, properties);

        register(new StartCommand(State.Start.getIdentifier(), State.Start.getDescription()));
        register(new OkAuthCommand(State.OkAuth.getIdentifier(), State.OkAuth.getDescription(), properties));
        register(new SyncCommand(State.Sync.getIdentifier(), State.Sync.getDescription(), socialMedia));
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
            states.put(message.getChatId(), currentState);
        }
        return false;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        Message msg;
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            msg = callbackQuery.getMessage();
            String data = callbackQuery.getData();
            if (State.findState(data) != null) {
                getRegisteredCommand(data).processMessage(this, msg, null);
                return;
            }
        } else {
            msg = update.getMessage();
        }

        Long chatId = msg.getChatId();
        String messageText = msg.getText();

        IState previousState = State.getPrevState(states.get(chatId));
        if (messageText.equals(GO_BACK_BUTTON_TEXT)) {
            Collection<IBotCommand> commands = getRegisteredCommands();
            for (IBotCommand command : commands) {
                if (command.getCommandIdentifier().equals(previousState.getIdentifier())) {
                    states.put(chatId, previousState);
                    command.processMessage(this, msg, null);
                    return;
                }
            }
        }

        IState currentState = states.get(chatId);
        if (currentState instanceof State) {
            states.put(chatId, Substate.nextSubstate(currentState));
        }

        currentState = states.get(chatId);

        String answer = nonCommand.nonCommandExecute(messageText, chatId, currentState);
        sendAnswer(chatId, getUserName(msg), answer);
    }

    //FIXME remove this with user data storage
    private final String accessToken = "";
    private final long groupId = 0L;

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        Map<String, List<Update>> updatesByPost = updates.stream().collect(Collectors.groupingBy(
                update -> update.getChannelPost() == null ? "" : update.getChannelPost().getMediaGroupId(),
                Collectors.toList()
        ));
        if (updatesByPost.containsKey("")) {
            for (Update update : updatesByPost.get("")) {
                processNonCommandUpdate(update);
            }
            updatesByPost.remove("");
        }
        if (updatesByPost.isEmpty()) {
            return;
        }
        List<Update> postUpdates = updatesByPost.values().stream().findFirst().get(); //Пока думаем, что все апдейты из одного канала для одного поста
        if (postUpdates.isEmpty()) {
            return;
        }
        long chatId = postUpdates.get(0).getChannelPost().getChatId();
        try {
            List<PhotoSize> photos = new ArrayList<>();
            Video video = null;
            String text = null;
            Poll poll = null;

            for (Update update : updates) {
                Message channelPost = update.getChannelPost();
                if (channelPost == null) {
                    processNonCommandUpdate(update);
                    continue;
                }
                if (channelPost.hasPhoto()) {
                    channelPost.getPhoto().stream().max(Comparator.comparingInt(PhotoSize::getFileSize)).ifPresent(photos::add);
                }
                if (channelPost.hasVideo()) {
                    video = channelPost.getVideo();
                }
                if (channelPost.getCaption() != null && !channelPost.getCaption().isEmpty()) {
                    text = channelPost.getCaption();
                }
                if (channelPost.hasText() && !channelPost.getText().isEmpty()) {
                    text = channelPost.getText();
                }
                if (channelPost.hasPoll()) {
                    poll = channelPost.getPoll();
                }
            }

            OkPostingHelper.OkPost post = helper.newPost(chatId, groupId, accessToken);
            try {
                post.addPhotos(photos).addVideo(video);
            } catch (URISyntaxException | IOException | TelegramApiException logged) {
                return;
            }
            try {
                post.addText(text)
                        .addPoll(poll)
                        .post(accessToken, groupId);
                sendAnswer(chatId, "Успешно опубликовал пост в ok.ru/group/" + groupId);
            } catch (URISyntaxException | IOException ignored) {
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
        answer.setText(text);
        answer.setParseMode(ParseMode.HTML);
        answer.setChatId(chatId.toString());
        answer.disableWebPagePreview();

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

    private void loadProperties() {
        try {
            properties.load(new FileReader("application.properties"));
        } catch (IOException e) {
            logger.error(String.format("Cannot load file application.properties: %s", e.getMessage()));
        }
    }
}
