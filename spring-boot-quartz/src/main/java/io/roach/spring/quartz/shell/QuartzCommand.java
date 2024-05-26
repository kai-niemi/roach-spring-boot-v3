package io.roach.spring.quartz.shell;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import org.quartz.DateBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import io.roach.spring.quartz.domain.Customer;
import io.roach.spring.quartz.domain.Product;
import io.roach.spring.quartz.repository.CustomerRepository;
import io.roach.spring.quartz.repository.ProductRepository;
import io.roach.spring.quartz.service.OrderJob;
import io.roach.spring.quartz.service.OrderSingletonJob;

@ShellComponent
@ShellCommandGroup("quartz")
public class QuartzCommand extends AbstractShellComponent {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @ShellMethod(value = "Schedule job(s)", key = {"j", "schedule-job"})
    public void schedulePlaceOrderJobs(
            @ShellOption(help = "number of jobs to create", defaultValue = "64") int count,
            @ShellOption(help = "job think time (ms)", defaultValue = "0") long thinkTime,
            @ShellOption(help = "cluster singleton job", defaultValue = "false") boolean singleton,
            @ShellOption(help = "number of triggers per job (one-off triggers)", defaultValue = "1") int triggers,
            @ShellOption(help = "repeat interval for trigger in ms (disable one-off triggers)", defaultValue = "0")
            long repeat) throws SchedulerException {
        if (!scheduler.isStarted()) {
            logger.warn("Scheduler is not started (hint: use start)");
        }

        UUID customerId = customerRepository.findRandomId()
                .orElseThrow(() -> new IllegalStateException("No customers found"));

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ObjectRetrievalFailureException(Customer.class, customerId));

        IntStream.rangeClosed(1, count)
                .parallel()
                .forEach(value -> {
                    UUID productId = productRepository.findRandomId()
                            .orElseThrow(() -> new IllegalStateException("No products found"));

                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new ObjectRetrievalFailureException(Product.class, productId));

                    Class<? extends Job> jobClass = singleton ? OrderSingletonJob.class : OrderJob.class;

                    // Defer order creation to a singleton, durable job
                    JobDetail job = JobBuilder.newJob(jobClass)
                            .withIdentity("place-order-job-" + UUID.randomUUID())
                            .usingJobData("customerId", customer.getId().toString())
                            .usingJobData("productSku", product.getSku())
                            .usingJobData("quantity", "1")
                            .usingJobData("thinkTime", "" + thinkTime)
                            .storeDurably()
                            .build();

                    Set<Trigger> triggerSet = new HashSet<>();

                    if (triggers > 0) {
                        IntStream.rangeClosed(1, triggers).forEach(i -> {
                            Trigger trigger = TriggerBuilder.newTrigger()
                                    .withIdentity("trigger-order-job-" + UUID.randomUUID())
                                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                            .withMisfireHandlingInstructionIgnoreMisfires())
                                    .startAt(DateBuilder.futureDate(i * 10,
                                            DateBuilder.IntervalUnit.MILLISECOND))
                                    .build();
                            triggerSet.add(trigger);
                        });
                    } else {
                        Trigger trigger = TriggerBuilder.newTrigger()
                                .withIdentity("trigger-order-job-" + UUID.randomUUID())
                                .withSchedule(SimpleScheduleBuilder
                                        .simpleSchedule()
                                        .repeatForever()
                                        .withIntervalInMilliseconds(repeat)
                                        .withMisfireHandlingInstructionIgnoreMisfires())
                                .build();
                        triggerSet.add(trigger);
                    }

                    try {
                        scheduler.scheduleJob(job, triggerSet, false);

                        logger.info("Scheduled %sjob key %s with %d triggers"
                                .formatted(singleton ? "singleton " : " ", job.getKey(), triggerSet.size()));
                    } catch (SchedulerException e) {
                        throw new RuntimeException(e);
                    }
                });

    }

    @ShellMethod(value = "Print scheduler status", key = {"u", "status"})
    public void status()
            throws SchedulerException {
        logger.info(">> Scheduler status <<");
        logger.info("Standby: %s".formatted(scheduler.isInStandbyMode()));
        logger.info("Shutdown: %s".formatted(scheduler.isShutdown()));
        logger.info("Started: %s".formatted(scheduler.isStarted()));
        logger.info("\n%s".formatted(scheduler.getMetaData().getSummary()));
    }

    @ShellMethod(value = "Start scheduler", key = {"s", "start"})
    public void start()
            throws SchedulerException {
        scheduler.start();
    }

    @ShellMethod(value = "Stop scheduler", key = {"p", "stop"})
    public void stop()
            throws SchedulerException {
        scheduler.standby();
    }

    @ShellMethod(value = "Clear scheduler", key = {"c", "clear"})
    public void cancel()
            throws SchedulerException {
        scheduler.clear();
    }
}
