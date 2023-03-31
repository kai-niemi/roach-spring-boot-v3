package io.roach.spring.statemachine.aspect;

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.postgresql.util.PSQLState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

@Aspect
@Order(TransactionRetryAspect.PRECEDENCE)
public class TransactionRetryAspect {
    /**
     * The precedence at which this advice is ordered by which also controls
     * the order it is invoked in the call chain between a source and target.
     */
    public static final int PRECEDENCE = AdvisorOrder.TRANSACTION_RETRY_ADVISOR;

    public static final Duration MAX_BACKOFF = Duration.ofSeconds(15);

    public static final int MAX_RETRIES = 15;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Pointcut expression matching all transactional boundaries.
     */
    @Pointcut("execution(public * *(..)) "
            + "&& @annotation(transactional)")
    public void anyTransactionalOperation(Transactional transactional) {
    }

    @Around(value = "anyTransactionalOperation(transactional)", argNames = "pjp,transactional")
    public Object doAroundTransactionalMethod(ProceedingJoinPoint pjp, Transactional transactional) throws Throwable {
        // Grab from type if needed (for non-annotated methods)
        if (transactional == null) {
            transactional = AnnotationUtils.findAnnotation(pjp.getSignature().getDeclaringType(), Transactional.class);
        }

        Assert.notNull(transactional, "No @Transactional annotation found!?");

        // Check for boundary
        if (transactional.propagation() != Propagation.REQUIRES_NEW) {
            return pjp.proceed();
        }

        Assert.isTrue(!TransactionSynchronizationManager.isActualTransactionActive(),
                "Found active transaction - check advice @Order and @EnableTransactionManagement order");

        final String methodName = pjp.getSignature().toShortString();
        final Instant callTime = Instant.now();
        SQLException lastSQLException = null;
        int numCalls = 0;

        do {
            final Throwable throwable;
            try {
                numCalls++;
                Object rv = pjp.proceed();
                if (numCalls > 1) {
                    handleRecovery(lastSQLException, numCalls, methodName, Duration.between(callTime, Instant.now()));
                }
                return rv;
            } catch (UndeclaredThrowableException ex) {
                throwable = ex.getUndeclaredThrowable();
            } catch (Exception ex) { // Catch r/w and commit time exceptions
                throwable = ex;
            }

            Throwable cause = NestedExceptionUtils.getMostSpecificCause(throwable);
            if (cause instanceof SQLException) {
                SQLException sqlException = (SQLException) cause;
                if (isRetryable(sqlException)) {
                    handleTransientException(sqlException, numCalls, methodName);
                    lastSQLException = sqlException;
                } else {
                    handleNonTransientException(sqlException);
                    throw throwable;
                }
            } else {
                throw throwable;
            }
        } while (numCalls < MAX_RETRIES);

        throw new ConcurrencyFailureException(
                "Too many serialization errors (" + numCalls + ") for method [" + pjp.getSignature().toShortString()
                        + "]. Giving up!");
    }

    protected boolean isRetryable(SQLException sqlException) {
        // 40001 is the only state code we are looking for in terms of safe retries
        return PSQLState.SERIALIZATION_FAILURE.getState().equals(sqlException.getSQLState());
    }

    protected void handleRecovery(SQLException sqlException,
                                  int numCalls, String methodName, Duration elapsedTime) {
        logger.info("Recovered from transient SQL error ({}) after #{} calls to '{}' ({} spent in retries): {}",
                sqlException.getSQLState(), numCalls, methodName, elapsedTime, sqlException.getMessage());
    }

    protected void handleTransientException(SQLException sqlException, int numCalls, String methodName) {
        try {
            long backoffMillis = Math.min((long) (Math.pow(2, numCalls) + Math.random() * 1000),
                    MAX_BACKOFF.toMillis());
            if (numCalls <= 1 && logger.isWarnEnabled()) {
                logger.warn("Transient SQL error ({}) in call #{} to '{}' (backoff for {} ms before retry): {}",
                        sqlException.getSQLState(), numCalls, methodName, backoffMillis, sqlException.getMessage());
            }
            Thread.sleep(backoffMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected void handleNonTransientException(SQLException sqlException) {
        sqlException.forEach(ex -> {
            SQLException nested = (SQLException) ex;
            logger.warn("Non-transient SQL error ({}): {}",
                    nested.getSQLState(), nested.getMessage());
        });
    }
}

