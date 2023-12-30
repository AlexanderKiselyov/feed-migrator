package polis.bot;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.callbacks.CallbacksHandlerHelper;
import polis.commands.Command;
import polis.commands.context.Context;
import polis.commands.context.ContextStorage;
import polis.keyboards.Keyboard;
import polis.posting.IPostsProcessor;
import polis.util.IState;
import polis.util.State;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class Bot extends TelegramLongPollingCommandBot implements TgFileLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);
    private static final String DEBUG_INFO_TEXT = "Update from ";

    private final String botName;
    private final String botToken;

    @Autowired
    private Collection<IBotCommand> commands;

    @Autowired
    IPostsProcessor postsProcessor;

    @Autowired
    CallbacksHandlerHelper callbacksHandlerHelper;

    @Autowired
    private ContextStorage contextStorage;

    @Autowired
    @Lazy
    private MessageSender messageSender;

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
        for (IBotCommand command : commands) {
            register(command);
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> overallUpdates) {
        Map<Boolean, List<Update>> updates = overallUpdates.stream()
                .collect(Collectors.partitioningBy(Update::hasChannelPost));
        boolean channelPosts = true;

        updates.get(!channelPosts).forEach(this::processUpdate);
        updates.get(channelPosts).stream()
                .map(Update::getChannelPost)
                .collect(Collectors.groupingBy(Message::getChatId))
                .forEach((channelId, posts) -> postsProcessor.processPostsInChannel(channelId, posts));
    }

    @Override
    public void processNonCommandUpdate(Update update) {
    }

    public void processUpdate(Update update) {
        if (LOGGER.isDebugEnabled()) {
            String s = messageDebugInfo(update.getMessage());
            LOGGER.debug(s);
        }
        final Message msg;
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            msg = callbackQuery.getMessage();
            String callbackQueryData = callbackQuery.getData();
            try {
                callbacksHandlerHelper.handleCallback(callbackQueryData, msg);
            } catch (TelegramApiException e) {
                LOGGER.error(String.format("Cannot perform Telegram API operation: %s", e.getMessage()));
            }
            return;
        }

        msg = update.getMessage();

        if (msg == null) {
            LOGGER.warn("Message is null");
            return;
        }

        String messageText = msg.getText();

        State customCommand = messageText.startsWith("/")
                ? State.findState(messageText.replace("/", ""))
                : State.findStateByDescription(messageText);

        if (customCommand != null) {
            IBotCommand command = getRegisteredCommand(customCommand.getIdentifier());
            command.processMessage(this, msg, null);
            return;
        }


        Context context = contextStorage.getByMessage(msg);
        IState currentState = context.currentState();

        //TODO Use GoBackCallbackHandler?
        if (messageText.equals(Keyboard.GO_BACK_BUTTON_TEXT) && currentState != null) {
            IState previousState = State.getPrevState(currentState);
            if (previousState == null) {
                LOGGER.error("Previous state = null, tmp state = {}", currentState.getIdentifier());
                return;
            }
            getRegisteredCommand(previousState.getIdentifier()).processMessage(this, msg, null);
            return;
        }

        if(currentState != null) {
            try {
                callbacksHandlerHelper.handleMessageCallback(messageText, currentState, msg);
            } catch (TelegramApiException e) {
                //TODO LOG or not throw
                throw new RuntimeException(e);
            }
        } else {
            messageSender.sendAnswer(msg.getChatId(), Command.NOT_ENOUGH_CONTEXT);
        }

    }

    private static String messageDebugInfo(Message message) {
        if (message == null) {
            return "Null message";
        }
        String debugInfo = new ReflectionToStringBuilder(message).toString();
        return DEBUG_INFO_TEXT + message.getChatId() + "\n" + debugInfo;
    }
}
