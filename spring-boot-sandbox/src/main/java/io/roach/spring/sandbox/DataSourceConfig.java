package io.roach.spring.sandbox;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.zaxxer.hikari.HikariDataSource;

import net.ttddyy.dsproxy.listener.logging.DefaultQueryLogEntryCreator;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;

@Configuration
public class DataSourceConfig {
    private final Logger sqlTraceLogger = LoggerFactory.getLogger("io.roach.SQL_TRACE");

    @Bean
    @Primary
    public DataSource primaryDataSource() {
        HikariDataSource ds = hikariDataSource();

        DefaultQueryLogEntryCreator creator = new DefaultQueryLogEntryCreator();
        creator.setMultiline(true);

        SLF4JQueryLoggingListener listener = new SLF4JQueryLoggingListener();
        listener.setLogger(sqlTraceLogger);
        listener.setLogLevel(SLF4JLogLevel.TRACE);
        listener.setQueryLogEntryCreator(creator);

        return sqlTraceLogger.isTraceEnabled()
                ? ProxyDataSourceBuilder
                .create(ds)
                .asJson()
                .listener(listener)
                .multiline()
                .build()
                : ds;
    }

    @Bean
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource hikariDataSource() {
        HikariDataSource ds = dataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();

        ds.addDataSourceProperty("reWriteBatchedInserts", "true");
        ds.addDataSourceProperty("application_name", "OOM");

        return ds;
    }
}
