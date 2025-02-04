package io.roach.spring.sandbox.shell;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import io.roach.spring.sandbox.domain.Product;
import io.roach.spring.sandbox.service.ProductService;

@ShellComponent
public class ProductCommands {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProductService productService;

    private final LinkedList<UUID> lastIds = new LinkedList<>();

    @ShellMethod(value = "Create product", key = {"cp"})
    public void createProduct() {
        Product product = productService.createProduct(Product.builder()
                .withName("CockroachDB Unleashed")
                .withPrice(new BigDecimal("249.50"))
                .withQuantity(1)
                .withSku(UUID.randomUUID().toString())
                .build());
        lastIds.add(product.getId());
        logger.info("Created: %s".formatted(product));
    }

    @ShellMethod(value = "Create product delayed (open-ended txn)", key = {"cpd"})
    public void createProductDelayed(@ShellOption(help = "Sleep time in sec", defaultValue = "30") int delay) {
        Product product = productService.createProduct(Product.builder()
                .withName("CockroachDB Unleashed")
                .withPrice(new BigDecimal("149.50"))
                .withQuantity(10)
                .withSku(UUID.randomUUID().toString())
                .build(),
                delay);
        lastIds.add(product.getId());
        logger.info("Created: %s".formatted(product));
    }

    private String lastId(String id) {
        if ("(last)".equals(id) && !lastIds.isEmpty()) {
            id = lastIds.removeLast().toString();
        }
        return id;
    }

    @ShellMethod(value = "Update product", key = {"up"})
    public void updateProduct(@ShellOption(help = "Product id", defaultValue = "(last)") String id) {
        id = lastId(id);

        Product product = productService.findProductById(UUID.fromString(id));

        product.setPrice(product.getPrice().add(new BigDecimal(10.00)));
        product.addInventoryQuantity(10);

        productService.updateProduct(product);

        logger.info("Updated: %s".formatted(product));
    }

    @ShellMethod(value = "Delete product", key = {"dp"})
    public void deleteProduct(@ShellOption(help = "Product id", defaultValue = "(last)") String id) {
        id = lastId(id);
        productService.deleteProductById(UUID.fromString(id));
        logger.info("Deleted: %s".formatted(id));
    }

    @ShellMethod(value = "List products", key = {"lp"})
    public void listProducts(
            @ShellOption(help = "page size", defaultValue = "16") int size) {
        Page<Product> page = productService.findProductPage(Pageable.ofSize(size));
        while (!page.isEmpty()) {
            logger.info("%s".formatted(page));

            page.getContent().forEach(product -> {
                logger.info("%s".formatted(product));
            });

            if (page.hasNext()) {
                page = productService.findProductPage(page.nextPageable());
            } else {
                break;
            }
        }
    }
}
