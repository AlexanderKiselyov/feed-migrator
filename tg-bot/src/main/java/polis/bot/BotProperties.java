package polis.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public final class BotProperties {
    public static final String NAME;
    public static final String USERNAME;
    public static final String ID;
    public static final String TOKEN;
    private static final Properties properties = new Properties();
    private static final Logger LOGGER = LoggerFactory.getLogger(BotProperties.class);

    static {
        try {
            properties.load(new FileReader("application.properties"));
        } catch (IOException e) {
            LOGGER.error("Cannot load file application.properties: " + e.getMessage());
            throw new IllegalStateException("Failed to read api keys from application.properties", e);
        }
        NAME = properties.getProperty("bot.name");
        USERNAME = properties.getProperty("bot.username");
        ID = properties.getProperty("bot.id");
        TOKEN = properties.getProperty("bot.token");
    }
}
