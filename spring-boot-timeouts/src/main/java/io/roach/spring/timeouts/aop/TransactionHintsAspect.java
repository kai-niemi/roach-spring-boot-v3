package io.roach.spring.timeouts.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

@Aspect
@Order(Ordered.LOWEST_PRECEDENCE - 2)
public class TransactionHintsAspect {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Pointcut("execution(public * *(..)) "
            + "&& @annotation(transactional)")
    public void anyTransactionalOperation(Transactional transactional) {
    }

    @Around(value = "anyTransactionalOperation(transactional)", argNames = "pjp,transactional")
    public Object doAroundTransactionalMethod(ProceedingJoinPoint pjp, Transactional transactional) throws Throwable {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "Explicit transaction required");

        if (logger.isTraceEnabled()) {
            logger.trace("Transaction attributes applied for {}: {}",
                    pjp.getSignature().toShortString(),
                    transactional);
        }

        applyVariables(transactional);

        return pjp.proceed();
    }

    private void applyVariables(Transactional transactional) {
        if (transactional.timeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
            jdbcTemplate.update("SET transaction_timeout=?", transactional.timeout() * 1000);
        }

        if (transactional.readOnly()) {
            jdbcTemplate.execute("SET transaction_read_only=true");
        }
    }
}
