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
import polis.state.State;
import polis.commands.NonCommand;
import polis.commands.OkAuthCommand;
import polis.commands.StartCommand;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Bot extends TelegramLongPollingCommandBot {
    private final String botName;
    private final String botToken;
    private final NonCommand nonCommand;
    private final Map<Long, State> states = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(Bot.class);

    public Bot(@Value("${bot.name}") String botName,  @Value("${bot.token}") String botToken) {
        super();
        this.botName = botName;
        this.botToken = botToken;
        nonCommand = new NonCommand();

        register(new StartCommand(State.Start.getIdentifier(), "Старт"));
        register(new OkAuthCommand(State.OkAuth.getIdentifier(), "Авторизация в Одноклассниках"));
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
        String userName = getUserName(msg);

        State currentState = states.get(chatId);

        String answer = nonCommand.nonCommandExecute(msg.getText(), currentState);
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
            logger.error(String.format("Cannot execute command of user %s: %s" , userName, e.getMessage()));
        }
    }
}
