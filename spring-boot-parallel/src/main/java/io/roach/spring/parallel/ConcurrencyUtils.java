package io.roach.spring.parallel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class ConcurrencyUtils {
    private static final Logger logger = LoggerFactory.getLogger(ConcurrencyUtils.class);

    private ConcurrencyUtils() {
    }

    public static ExecutorService unboundedThreadPool() {
        return Executors.newCachedThreadPool();
    }

    public static ExecutorService boundedThreadPool() {
        return boundedThreadPool(Runtime.getRuntime().availableProcessors() * 4);
    }

    public static ExecutorService boundedThreadPool(int numThreads) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(numThreads / 2, numThreads,
                0L, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(numThreads));
        executor.setRejectedExecutionHandler((runnable, exec) -> {
            try {
                exec.getQueue().put(runnable);
                if (exec.isShutdown()) {
                    throw new RejectedExecutionException(
                            "Task " + runnable + " rejected from " + exec);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RejectedExecutionException("", e);
            }
        });
        return executor;
    }

    public static <V> void runConcurrentlyAndWait(List<Callable<V>> tasks,
                                                  long timeout,
                                                  TimeUnit timeUnit,
                                                  Consumer<V> completion) {
        runConcurrentlyAndWait(tasks, timeout, timeUnit, completion,
                throwable -> {
                    logger.error("", throwable);
                    return null;
                });
    }

    public static <V> void runConcurrentlyAndWait(List<Callable<V>> tasks,
                                                  long timeout,
                                                  TimeUnit timeUnit,
                                                  Consumer<V> completionFunction,
                                                  Function<Throwable, ? extends Void> throwableFunction) {
        ScheduledExecutorService cancellationService
                = Executors.newSingleThreadScheduledExecutor();

        ExecutorService executor = boundedThreadPool();

        List<CompletableFuture<Void>> allFutures = new ArrayList<>();

        final Instant expiryTime = Instant.now().plus(timeout, timeUnit.toChronoUnit());

        tasks.forEach(callable -> {
            allFutures.add(CompletableFuture.supplyAsync(() -> {
                                if (Instant.now().isAfter(expiryTime)) {
                                    logger.warn("Task scheduled after expiration time: " + expiryTime);
                                    return null;
                                }
                                Future<V> future = executor.submit(callable);
                                long cancellationTime = Duration.between(Instant.now(), expiryTime).toMillis();
                                cancellationService.schedule(() -> future.cancel(true), cancellationTime, TimeUnit.MILLISECONDS);
                                try {
                                    return future.get();
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    throw new IllegalStateException(e.getCause());
                                } catch (ExecutionException e) {
                                    throw new IllegalStateException(e.getCause());
                                }
                            }, executor)
                            .thenAccept(completionFunction)
                            .exceptionally(throwableFunction)
            );
        });

        CompletableFuture.allOf(
                allFutures.toArray(new CompletableFuture[]{})).join();

        executor.shutdownNow();
        cancellationService.shutdownNow();
    }
}
