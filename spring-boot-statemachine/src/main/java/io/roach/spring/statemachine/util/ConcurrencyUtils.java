package io.roach.spring.statemachine.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Utility for submitting concurrent workers with a collective timeout and
 * graceful cancellation.
 */
public abstract class ConcurrencyUtils {
    private ConcurrencyUtils() {
    }

    public static <V> void runConcurrentlyAndWait(int numThreads, List<Callable<V>> callables) {
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

        List<CompletableFuture<V>> allFutures = new ArrayList<>();

        callables.forEach(c -> allFutures.add(CompletableFuture.supplyAsync(() -> {
            try {
                return c.call();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }, executor)));

        CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[] {})).join();

        executor.shutdown();
    }
}
