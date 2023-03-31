package io.roach.spring.statemachine;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;

import io.roach.spring.statemachine.util.Randomizer;

@Tag("unit-test")
public class RandomizerTest {
    @RepeatedTest(10)
    public void givenProbability_whenInvoked_invokeWithProbability() {
        int numSamples = 1_000_000;
        int probability = 10;

        int timesInvoked = Stream.generate(() ->
                        Randomizer.withProbability(() -> 1, () -> 0, probability))
                .limit(numSamples)
                .mapToInt(e -> e).sum();

        int monteCarloProbability = (timesInvoked * 100) / numSamples;

        Assertions.assertTrue(() -> monteCarloProbability >= 9 && monteCarloProbability < 11);
    }

    @RepeatedTest(10)
    public void givenAnotherProbability_whenInvoked_invokeWithProbability() {
        int numSamples = 1_000_000;
        int probability = 100;

        int timesInvoked = Stream.generate(() ->
                        Randomizer.withProbability(() -> 1, () -> 0, probability))
                .limit(numSamples)
                .mapToInt(e -> e).sum();

        int monteCarloProbability = (timesInvoked * 100) / numSamples;

        Assertions.assertTrue(() -> monteCarloProbability >= 99 && monteCarloProbability < 101);
    }
}
