package io.roach.spring.sandbox;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication(exclude = {
        JdbcRepositoriesAutoConfiguration.class
})
public class Application implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        String sql = "select * from product where 1=1";

        List<String> values = args.getOptionValues("query");
        if (values != null) {
            sql = values.iterator().next();
        }

        int fetchSize = 0;
        values = args.getOptionValues("fetchSize");
        if (values != null) {
            fetchSize = Integer.parseInt(values.iterator().next());
        }

        if (args.containsOption("followerRead")) {
            sql = sql + " AS OF SYSTEM TIME follower_read_timestamp()";
        }

        final String sqlFinal = sql;
        final int fetchSizeFinal = fetchSize;

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute((ConnectionCallback<Void>) conn -> {
            int rows = 0;
            long bytes = 0;

            logger.info("Scanning rows (each dot is 10k)");

            try (PreparedStatement ps = conn.prepareStatement(sqlFinal)) {
                ps.setFetchSize(fetchSizeFinal);

                try (ResultSet rs = ps.executeQuery()) {
                    int c = rs.getMetaData().getColumnCount();
                    while (rs.next()) {
                        for (int i = 1; i <= c; i++) {
                            byte[] arr = rs.getBytes(i);
                            bytes += arr.length;
                        }

                        rows++;
                        if (rows % 10_000 == 0) {
                            System.out.print(".");
                            System.out.flush();
                        }
                    }
                }
            }

            System.out.println();

            logger.info("Scanned %,d rows and %,d bytes".formatted(rows, bytes));

            return null;
        });
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.CONSOLE)
                .logStartupInfo(true)
                .run(args);
    }
}
