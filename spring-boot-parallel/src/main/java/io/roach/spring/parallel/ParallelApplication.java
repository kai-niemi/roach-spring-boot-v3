package io.roach.spring.parallel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableTransactionManagement
public class ParallelApplication implements ApplicationRunner {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        new SpringApplicationBuilder(ParallelApplication.class)
                .logStartupInfo(true)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.CONSOLE)
                .run(args);
    }

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(ApplicationArguments args) {
        List<Callable<Pair<String, Integer>>> tasks = new ArrayList<>();

        StringUtils.commaDelimitedListToSet("SE,UK,DK,NO,ES,US,FI,FR,BE,DE")
                .forEach(country ->
                        tasks.add(() -> Pair.of(country, productRepository.sumInventory(country)))
                );

        List<Pair<String, Integer>> sums = new ArrayList<>();

        Instant start = Instant.now();

        ConcurrencyUtils.runConcurrentlyAndWait(tasks, 10, TimeUnit.MINUTES, sums::add);

        Duration firstPass = Duration.between(start, Instant.now());

        start = Instant.now();

        int serialTotal = productRepository.sumTotalInventory();

        Duration secondPass = Duration.between(start, Instant.now());

        sums.forEach(v -> logger.info("Inventory sum for {} is {}", v.getFirst(), v.getSecond()));
        logger.info("Total inventory sum is {}", sums.stream().mapToInt(Pair::getSecond).sum());
        logger.info("Verified inventory sum is {}", serialTotal);
        logger.info("Parallel execution time: {}", firstPass.toString());
        logger.info("Serial execution time: {}", secondPass.toString());
        logger.info("Execution time diff: {}%",
                Math.round(((double) secondPass.toNanos() / (double) firstPass.toNanos()) * 100.00));
    }
}
