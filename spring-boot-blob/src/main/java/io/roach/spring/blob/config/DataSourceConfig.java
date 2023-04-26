package io.roach.spring.blob.config;

import com.zaxxer.hikari.HikariDataSource;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.postgresql.PGProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    private final Logger traceLogger = LoggerFactory.getLogger("io.roach.SQL_TRACE");

    @Bean
    @Primary
    public DataSource primaryDataSource(DataSourceProperties properties) {
        HikariDataSource ds = hikariDataSource(properties);
        return traceLogger.isTraceEnabled()
                ? ProxyDataSourceBuilder
                .create(ds)
                .logQueryBySlf4j(SLF4JLogLevel.TRACE, traceLogger.getName())
                .asJson()
                .multiline()
                .build()
                : ds;
    }

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource hikariDataSource(DataSourceProperties properties) {
        HikariDataSource ds = properties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        ds.setPoolName("blob-demo");
        ds.setAutoCommit(true);
        ds.addDataSourceProperty(PGProperty.REWRITE_BATCHED_INSERTS.getName(), "true");
        ds.addDataSourceProperty(PGProperty.APPLICATION_NAME.getName(), "blob-demo");
        return ds;
    }
}
