package polis.ok.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public final class OkAppProperties {
    public static final String APPLICATION_ID;
    public static final String APPLICATION_KEY;
    public static final String APPLICATION_SECRET_KEY;
    public static final String REDIRECT_URI;

    private static final Properties properties = new Properties();
    private static final Logger logger = LoggerFactory.getLogger(OkAppProperties.class);

    static {
        try {
            properties.load(new FileReader("application.properties"));
        } catch (IOException e) {
            logger.error("Cannot load file application.properties: " + e.getMessage());
            throw new IllegalStateException("Failed to read api keys from application.properties", e);
        }
        APPLICATION_ID = properties.getProperty("okapp.id");
        APPLICATION_KEY = properties.getProperty("okapp.app_key");
        APPLICATION_SECRET_KEY = properties.getProperty("okapp.secret_key");
        REDIRECT_URI = properties.getProperty("okapp.redirect_uri");
    }
}
