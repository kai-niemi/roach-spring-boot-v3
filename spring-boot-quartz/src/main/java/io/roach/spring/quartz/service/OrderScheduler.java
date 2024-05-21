package io.roach.spring.quartz.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.quartz.DateBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.roach.spring.quartz.domain.Customer;
import io.roach.spring.quartz.domain.Product;
import io.roach.spring.quartz.repository.CustomerRepository;
import io.roach.spring.quartz.repository.ProductRepository;

import static org.quartz.JobBuilder.newJob;

@Service
public class OrderScheduler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    public static <E> E selectRandom(List<E> collection) {
        return collection.get(ThreadLocalRandom.current().nextInt(collection.size()));
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void scheduleOrderEveryFiveSeconds() throws SchedulerException {
        Page<Customer> customerPage = customerRepository.findAll(Pageable.ofSize(1024));

        // Narrow product selection to promote high contention (on inventory updates)
        Page<Product> productPage = productRepository.findAll(Pageable.ofSize(2));

        Customer customer = selectRandom(customerPage.getContent());
        Product product = selectRandom(productPage.getContent());

        logger.info("Schedule purchase order for customer: %s product: %s"
                .formatted(customer.getId(), product.getId()));

        UUID instanceId = UUID.randomUUID();

        // Defer order creation to a singleton, durable job
        JobDetail job = newJob(OrderCreationJob.class)
                .withIdentity("place-order-job-" + instanceId)
                .usingJobData("customerId", customer.getId().toString())
                .usingJobData("productSku", product.getSku())
                .usingJobData("quantity", "1")
                .storeDurably()
                .build();

        // One-off trigger in the future
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger-order-job-" + instanceId)
                .startAt(DateBuilder.futureDate(15, DateBuilder.IntervalUnit.SECOND))
                .build();

        scheduler.scheduleJob(job, trigger);
    }
}
