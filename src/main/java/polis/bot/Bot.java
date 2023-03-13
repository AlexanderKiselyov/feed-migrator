package polis.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.commands.NonCommand;
import polis.commands.OkAuthCommand;
import polis.commands.StartCommand;
import polis.state.State;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;

@Component
public class Bot extends TelegramLongPollingCommandBot {
    private final String botName;
    private final String botToken;
    private final StartCommand startCommand;
    private final OkAuthCommand okAuthCommand;
    private final NonCommand nonCommand;
    private final Map<Long, State> states = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(Bot.class);
    private final Properties properties = new Properties();

    public Bot(@Value("${bot.name}") String botName, @Value("${bot.token}") String botToken) {
        super();
        this.botName = botName;
        this.botToken = botToken;
        nonCommand = new NonCommand();

        loadProperties();

        startCommand = new StartCommand(State.Start.getIdentifier(), State.Start.getDescription());
        register(startCommand);
        okAuthCommand = new OkAuthCommand(State.OkAuth.getIdentifier(), State.OkAuth.getDescription(), properties);
        register(okAuthCommand);
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
     * Устанавливает бота в определенное состояние в зависимости от введенной пользователем команды
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
        Message msg = update.getMessage();
        Long chatId = msg.getChatId();
        String messageText = msg.getText();

        if (messageText.equals(GO_BACK_BUTTON_TEXT)) {
            states.put(chatId, State.getPrevState(states.get(chatId)));
            startCommand.execute(this, msg.getFrom(), msg.getChat(), null);
            return;
        }

        State currentState = states.get(chatId);
        String userName = getUserName(msg);

        String answer = nonCommand.nonCommandExecute(messageText, currentState, properties);
        setAnswer(chatId, userName, answer);
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

        try {
            execute(answer);
        } catch (TelegramApiException e) {
            logger.error(String.format("Cannot execute command of user %s: %s", userName, e.getMessage()));
        }
    }

    private void loadProperties() {
        try {
            properties.load(new FileReader(String.format("%s\\application.properties",
                    System.getProperty("user.dir"))));
        } catch (IOException e) {
            logger.error(String.format("Cannot load file application.properties: %s", e.getMessage()));
        }
    }
}
