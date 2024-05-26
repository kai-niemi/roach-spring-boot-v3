package io.roach.spring.quartz.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
public class SchedulingConfiguration implements SchedulingConfigurer {
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(Runtime.getRuntime().availableProcessors());
        threadPoolTaskScheduler.setThreadNamePrefix("quartz-task-pool-");
        threadPoolTaskScheduler.initialize();

        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
    }

//    @Bean
//    public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory, PlatformTransactionManager transactionManager)
//            throws IOException {
//        SchedulerFactoryBean factory = new SchedulerFactoryBean();
//        factory.setJobFactory(jobFactory);
//        factory.setDataSource(dataSource);
//        factory.setTransactionManager(transactionManager);
//        factory.setQuartzProperties(quartzProperties());
//        factory.setGlobalJobListeners(jobsListenerService);
//        return factory;
//    }
}
