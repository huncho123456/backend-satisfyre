package com.Satisfyre.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dotenvConfig.get("DB_URL"));
        config.setUsername(dotenvConfig.get("DB_USERNAME"));
        config.setPassword(dotenvConfig.get("DB_PASSWORD"));
        config.setDriverClassName(dotenvConfig.get("DB_DRIVER"));
        return new HikariDataSource(config);
    }
}
