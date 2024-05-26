package io.roach.spring.quartz.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.boot.autoconfigure.quartz.QuartzTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.quartz.LocalDataSourceJobStore;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.roach.spring.quartz.retry.CockroachExceptionClassifier;
import jakarta.persistence.EntityManagerFactory;

@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableRetry
@Configuration
public class TransactionConfiguration {
    @Bean
    public CockroachExceptionClassifier exceptionClassifier() {
        return new CockroachExceptionClassifier();
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(@Autowired EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        transactionManager.setJpaDialect(new HibernateJpaDialect());
        return transactionManager;
    }

    @Bean
    @QuartzTransactionManager
    public PlatformTransactionManager quartzTransactionManager(@Autowired
                                                               @QuartzDataSource DataSource dataSource) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        transactionManager.setRollbackOnCommitFailure(false);
        return transactionManager;
    }
}
