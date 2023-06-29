package io.roach.spring.timeouts;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.roach.spring.timeouts.domain.Address;
import io.roach.spring.timeouts.domain.Customer;
import io.roach.spring.timeouts.domain.Order;
import io.roach.spring.timeouts.domain.Product;
import io.roach.spring.timeouts.domain.CustomerRepository;
import io.roach.spring.timeouts.domain.OrderRepository;
import io.roach.spring.timeouts.domain.ProductRepository;
import io.roach.spring.timeouts.util.RandomData;

@Service
public class TestDoubles {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteTestDoubles() {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "TX not active");
        orderRepository.deleteAllOrderItems();
        orderRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        customerRepository.deleteAllInBatch();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createProducts(int numProducts, Consumer<Product> callback) {
        IntStream.rangeClosed(1, numProducts).forEach(value -> {
            Product p = newProduct();
            callback.accept(p);
            productRepository.save(p);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createCustomers(int numCustomers) {
        IntStream.rangeClosed(1, numCustomers).forEach(value -> customerRepository.save(newCustomer()));
    }

    public Product newProduct() {
        return Product.builder()
                .withName("CockroachDB Unleashed - First Edition")
                .withPrice(RandomData.randomMoneyBetween(20, 500, 2))
                .withSku(RandomData.randomWord(12))
                .withQuantity(RandomData.randomInt(3000, 9000))
                .build();
    }

    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    public Customer newCustomer() {
        String fn = RandomData.randomFirstName();
        String ln = RandomData.randomLastName();
        // sufficient randomness hopefully
        String un = fn.toLowerCase() + "." + ln.toLowerCase() + random.nextInt(1, 500);
        String email = un + "@example.com";
        return Customer.builder()
                .withFirstName(fn)
                .withLastName(ln)
                .withUserName(un)
                .withEmail(email)
                .withAddress(newAddress())
                .build();
    }

    private Address newAddress() {
        return Address.builder()
                .setAddress1(RandomData.randomWord(15))
                .setAddress2(RandomData.randomWord(15))
                .setCity(RandomData.randomCity())
                .setPostcode(RandomData.randomZipCode())
                .setCountry(RandomData.randomCountry())
                .build();
    }

    public void newOrders(int orderCount, int productCount, Consumer<Order> callback) {
        List<Customer> allCustomers = customerRepository.findAll();
        List<Product> allProducts = productRepository.findAll();

        IntStream.rangeClosed(1, orderCount).forEach(value -> {
            Order.Builder ob = Order.builder().withCustomer(RandomData.selectRandom(allCustomers));

            RandomData.selectRandomUnique(allProducts, productCount).forEach(product ->
                    ob.andOrderItem()
                            .withProduct(product)
                            .withUnitPrice(product.getPrice())
                            .withQuantity(RandomData.randomInt(2, 10))
                            .then());

            callback.accept(ob.build());
        });
    }
}
