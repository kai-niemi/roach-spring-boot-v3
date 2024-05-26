package io.roach.spring.quartz.service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.roach.spring.quartz.domain.Customer;
import io.roach.spring.quartz.domain.Product;
import io.roach.spring.quartz.domain.PurchaseOrder;

@Component
public class OrderJob implements Job {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;

    @Override
    public void execute(JobExecutionContext context) {
        Assert.isTrue(!TransactionSynchronizationManager.isActualTransactionActive(), "Expected no tx");

        String customerId = (String) context.getJobDetail().getJobDataMap().get("customerId");
        String productSku = context.getJobDetail().getJobDataMap().getString("productSku");
        int quantity = Integer.parseInt(context.getJobDetail().getJobDataMap().getString("quantity"));
        int thinkTime = Integer.parseInt(context.getJobDetail().getJobDataMap().getString("thinkTime"));

        Customer customer = customerService.findCustomerById(UUID.fromString(customerId));
        Product product = orderService.findProductBySku(productSku);

        logger.info("Place purchase order for customer: %s: product: %s quantity: %d"
                .formatted(customerId, productSku, quantity));

        // Transaction boundary with retries
        orderService.placeOrder(PurchaseOrder.builder()
                .withCustomer(customer)
                .andOrderItem()
                .withProduct(product)
                .withUnitPrice(product.getPrice())
                .withQuantity(quantity)
                .then()
                .build());

        try {
            TimeUnit.MILLISECONDS.sleep(thinkTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
