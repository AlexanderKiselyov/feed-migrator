package polis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import polis.bot.Bot;

@SpringBootApplication
public class FeedMigratorBot {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedMigratorBot.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(FeedMigratorBot.class, args);
        String botName = ctx.getEnvironment().getProperty("bot.name");
        String botToken = ctx.getEnvironment().getProperty("bot.token");
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new Bot(botName, botToken));
        } catch (TelegramApiException e) {
            LOGGER.error(String.format("Cannot register new bot: %s", e.getMessage()));
        }
    }
}
