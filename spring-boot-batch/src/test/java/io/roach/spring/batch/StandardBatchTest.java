package io.roach.spring.batch;

import io.roach.spring.batch.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StandardBatchTest extends AbstractIntegrationTest {
    @Autowired
    private DataSource dataSource;

    public static <T> Stream<List<T>> chunkedStream(Stream<T> stream, int chunkSize) {
        AtomicInteger idx = new AtomicInteger();
        return stream.collect(Collectors.groupingBy(x -> idx.getAndIncrement() / chunkSize))
                .values().stream();
    }

    @Test
    public void testInsertBatch() throws SQLException {
        List<Product> products = Arrays.asList(
                Product.builder().withName("A").withQuantity(1).withPrice(new BigDecimal("10.15")).build(),
                Product.builder().withName("B").withQuantity(2).withPrice(new BigDecimal("11.15")).build(),
                Product.builder().withName("C").withQuantity(3).withPrice(new BigDecimal("12.15")).build()
                // .. etc
        );

        Stream<List<Product>> chunks = chunkedStream(products.stream(), 128);

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);

            chunks.forEach(chunk -> {
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO product (id,inventory,price,name,sku) values (?,?,?,?,?) "
                                + "ON CONFLICT (id) DO NOTHING")) {

                    for (Product product : chunk) {
                        ps.setObject(1, product.getId());
                        ps.setObject(2, product.getInventory());
                        ps.setObject(3, product.getPrice());
                        ps.setObject(4, product.getName());
                        ps.setObject(5, product.getSku());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }

    @Test
    public void testInsertBatchRewrite() throws SQLException {
        List<Product> products = Arrays.asList(
                Product.builder().withName("A").withQuantity(1).withPrice(new BigDecimal("10.15")).build(),
                Product.builder().withName("B").withQuantity(2).withPrice(new BigDecimal("11.15")).build(),
                Product.builder().withName("C").withQuantity(3).withPrice(new BigDecimal("12.15")).build()
                // .. etc
        );

        Stream<List<Product>> chunks = chunkedStream(products.stream(), 128);

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);

            chunks.forEach(chunk -> {
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO product(id,inventory,price,name,sku)"
                                + " select"
                                + " unnest(?) as id,"
                                + " unnest(?) as inventory, "
                                + " unnest(?) as price,"
                                + " unnest(?) as name, "
                                + " unnest(?) as sku"
                                + " ON CONFLICT (id) DO NOTHING")) {
                    List<Integer> qty = new ArrayList<>();
                    List<BigDecimal> price = new ArrayList<>();
                    List<UUID> ids = new ArrayList<>();
                    List<String> name = new ArrayList<>();
                    List<String> sku = new ArrayList<>();

                    for (Product product : chunk) {
                        ids.add(product.getId());
                        qty.add(product.getInventory());
                        price.add(product.getPrice());
                        name.add(product.getName());
                        sku.add(product.getSku());
                    }

                    ps.setArray(1, ps.getConnection().createArrayOf("UUID", ids.toArray()));
                    ps.setArray(2, ps.getConnection().createArrayOf("BIGINT", qty.toArray()));
                    ps.setArray(3, ps.getConnection().createArrayOf("DECIMAL", price.toArray()));
                    ps.setArray(4, ps.getConnection().createArrayOf("VARCHAR", name.toArray()));
                    ps.setArray(5, ps.getConnection().createArrayOf("VARCHAR", sku.toArray()));

                    ps.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
