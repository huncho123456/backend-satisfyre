package com.Satisfyre.app.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

@Configuration
public class dotenvConfig {

    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    public static String get(String key) {

        String value = System.getenv(key);
        return value != null ? value : dotenv.get(key);
    }
}
