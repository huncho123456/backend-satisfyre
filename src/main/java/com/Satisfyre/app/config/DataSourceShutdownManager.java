package com.Satisfyre.app.config;

import javax.annotation.PreDestroy;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@WebListener
@Component
public class DataSourceShutdownManager {

    private final HikariDataSource dataSource;

    public DataSourceShutdownManager(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PreDestroy
    public void shutdownPool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("✅ HikariCP pool shut down cleanly.");
        }
    }

}