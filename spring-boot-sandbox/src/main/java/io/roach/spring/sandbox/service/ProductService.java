package io.roach.spring.sandbox.service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;
import org.springframework.util.Assert;

import io.roach.spring.sandbox.domain.Product;
import io.roach.spring.sandbox.repository.ProductRepository;

@Service
public class ProductService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProductRepository productRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Product createProduct(Product product) {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "No active tx");
        return productRepository.saveAndFlush(product);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Product createProduct(Product product, int delay) {
        Product attached = productRepository.saveAndFlush(product);
        try {
            logger.info("Sleeping for %d sec".formatted(delay));
            TimeUnit.SECONDS.sleep(delay);
            logger.info("Done sleeping for %d sec".formatted(delay));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return attached;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Product findProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ObjectRetrievalFailureException(Product.class, id));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateProduct(Product product) {
        Product p = productRepository.getReferenceById(product.getId());
        p.setName(product.getName());
        p.setPrice(product.getPrice());
        p.setSku(product.getSku());
        p.setInventory(product.getInventory());
        productRepository.save(p);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteProductById(UUID id) {
        productRepository.deleteById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Page<Product> findProductPage(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
}
