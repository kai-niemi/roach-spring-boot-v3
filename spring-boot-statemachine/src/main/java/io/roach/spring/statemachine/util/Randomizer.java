package io.roach.spring.statemachine.util;

import java.util.SplittableRandom;
import java.util.function.Supplier;

import org.springframework.data.util.Lazy;
import org.springframework.util.Assert;

public abstract class Randomizer {
    private static final Lazy<SplittableRandom> random = Lazy.of(SplittableRandom::new);

    public static <T> T withProbability(Supplier<T> positiveCase, Supplier<T> negativeCase, int probability) {
        Assert.isTrue(probability >= 0, "probability must be 0 >= N <= 100");
        Assert.isTrue(probability <= 100, "probability must be 0 >= N <= 100");
        if (random.get().nextInt(1, 101) <= probability) {
            return positiveCase.get();
        } else {
            return negativeCase.get();
        }
    }
}
