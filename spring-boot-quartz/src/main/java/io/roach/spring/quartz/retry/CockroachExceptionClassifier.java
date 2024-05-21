package io.roach.spring.quartz.retry;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;

public class CockroachExceptionClassifier {
    public static final String SERIALIZATION_FAILURE = "40001";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public boolean shouldRetry(Throwable ex) {
        if (ex == null) {
            return false;
        }
        Throwable throwable = NestedExceptionUtils.getMostSpecificCause(ex);
        if (throwable instanceof SQLException) {
            return shouldRetry((SQLException) throwable);
        }
        logger.warn("Non-transient exception detected: {}", ex.getClass());
        return false;
    }

    public boolean shouldRetry(SQLException ex) {
        if (SERIALIZATION_FAILURE.equals(ex.getSQLState())) {
            logger.warn("Transient retryable SQL exception detected: sql state [{}], message [{}]",
                    ex.getSQLState(), ex.toString());
            return true;
        }
        return false;
    }
}
