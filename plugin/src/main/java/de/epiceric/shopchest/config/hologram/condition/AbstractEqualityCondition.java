package de.epiceric.shopchest.config.hologram.condition;

import de.epiceric.shopchest.config.hologram.HologramFormat;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public abstract class AbstractEqualityCondition<T> implements Condition {

    protected final Function<Map<HologramFormat.Requirement, Object>, T> firstArgProvider;
    protected final Function<Map<HologramFormat.Requirement, Object>, T> secondArgProvider;

    public AbstractEqualityCondition(
            Function<Map<HologramFormat.Requirement, Object>, T> firstArgProvider,
            Function<Map<HologramFormat.Requirement, Object>, T> secondArgProvider
    ) {
        this.firstArgProvider = firstArgProvider;
        this.secondArgProvider = secondArgProvider;
    }

    public static class EqualityCondition<T> extends AbstractEqualityCondition<T> {

        public EqualityCondition(
                Function<Map<HologramFormat.Requirement, Object>, T> firstArgProvider,
                Function<Map<HologramFormat.Requirement, Object>, T> secondArgProvider
        ) {
            super(firstArgProvider, secondArgProvider);
        }

        @Override
        public boolean test(Map<HologramFormat.Requirement, Object> requirementValues) {
            return Objects.equals(firstArgProvider.apply(requirementValues), secondArgProvider.apply(requirementValues));
        }

        @Override
        public String toString() {
            return "(" + firstArgProvider.toString() + " == " + secondArgProvider.toString() + ")";
        }

    }

    public static class InequalityCondition<T> extends AbstractEqualityCondition<T> {

        public InequalityCondition(
                Function<Map<HologramFormat.Requirement, Object>, T> firstArgProvider,
                Function<Map<HologramFormat.Requirement, Object>, T> secondArgProvider
        ) {
            super(firstArgProvider, secondArgProvider);
        }

        @Override
        public boolean test(Map<HologramFormat.Requirement, Object> requirementValues) {
            return !Objects.equals(firstArgProvider.apply(requirementValues), secondArgProvider.apply(requirementValues));
        }

        @Override
        public String toString() {
            return "(" + firstArgProvider.toString() + " != " + secondArgProvider.toString() + ")";
        }

    }

}
