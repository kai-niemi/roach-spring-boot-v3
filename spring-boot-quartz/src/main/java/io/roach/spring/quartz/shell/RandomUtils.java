package io.roach.spring.quartz.shell;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public abstract class RandomUtils {
    private RandomUtils() {
    }

    public static <E> E selectRandom(List<E> collection) {
        return collection.get(ThreadLocalRandom.current().nextInt(collection.size()));
    }
}
