package de.epiceric.shopchest.config.hologram.condition;

import de.epiceric.shopchest.config.hologram.HologramFormat;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public abstract class ComparisonCondition implements Condition {

    protected final Function<Map<HologramFormat.Requirement, Object>, Double> firstArgProvider;
    protected final Function<Map<HologramFormat.Requirement, Object>, Double> secondArgProvider;

    public ComparisonCondition(
            Function<Map<HologramFormat.Requirement, Object>, Double> firstArgProvider,
            Function<Map<HologramFormat.Requirement, Object>, Double> secondArgProvider
    ) {
        this.firstArgProvider = firstArgProvider;
        this.secondArgProvider = secondArgProvider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComparisonCondition that = (ComparisonCondition) o;
        return Objects.equals(firstArgProvider, that.firstArgProvider) && Objects.equals(secondArgProvider, that.secondArgProvider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstArgProvider, secondArgProvider);
    }

    public static class GreaterCondition extends ComparisonCondition {

        public GreaterCondition(
                Function<Map<HologramFormat.Requirement, Object>, Double> firstArgProvider,
                Function<Map<HologramFormat.Requirement, Object>, Double> secondArgProvider
        ) {
            super(firstArgProvider, secondArgProvider);
        }

        @Override
        public boolean test(Map<HologramFormat.Requirement, Object> requirementValues) {
            return firstArgProvider.apply(requirementValues) > secondArgProvider.apply(requirementValues);
        }

        @Override
        public String toString() {
            return "(" + firstArgProvider.toString() + " > " + secondArgProvider.toString() + ")";
        }

    }

    public static class LessCondition extends ComparisonCondition {

        public LessCondition(
                Function<Map<HologramFormat.Requirement, Object>, Double> firstArgProvider,
                Function<Map<HologramFormat.Requirement, Object>, Double> secondArgProvider
        ) {
            super(firstArgProvider, secondArgProvider);
        }

        @Override
        public boolean test(Map<HologramFormat.Requirement, Object> requirementValues) {
            return firstArgProvider.apply(requirementValues) < secondArgProvider.apply(requirementValues);
        }

        @Override
        public String toString() {
            return "(" + firstArgProvider.toString() + " < " + secondArgProvider.toString() + ")";
        }

    }

    public static class GreaterOrEqualCondition extends ComparisonCondition {

        public GreaterOrEqualCondition(
                Function<Map<HologramFormat.Requirement, Object>, Double> firstArgProvider,
                Function<Map<HologramFormat.Requirement, Object>, Double> secondArgProvider
        ) {
            super(firstArgProvider, secondArgProvider);
        }

        @Override
        public boolean test(Map<HologramFormat.Requirement, Object> requirementValues) {
            return firstArgProvider.apply(requirementValues) >= secondArgProvider.apply(requirementValues);
        }

        @Override
        public String toString() {
            return "(" + firstArgProvider.toString() + " >= " + secondArgProvider.toString() + ")";
        }

    }

    public static class LessOrEqualCondition extends ComparisonCondition {

        public LessOrEqualCondition(
                Function<Map<HologramFormat.Requirement, Object>, Double> firstArgProvider,
                Function<Map<HologramFormat.Requirement, Object>, Double> secondArgProvider
        ) {
            super(firstArgProvider, secondArgProvider);
        }

        @Override
        public boolean test(Map<HologramFormat.Requirement, Object> requirementValues) {
            return firstArgProvider.apply(requirementValues) <= secondArgProvider.apply(requirementValues);
        }

        @Override
        public String toString() {
            return "(" + firstArgProvider.toString() + " <= " + secondArgProvider.toString() + ")";
        }

    }

}
