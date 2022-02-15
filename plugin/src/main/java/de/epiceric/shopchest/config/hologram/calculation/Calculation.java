package de.epiceric.shopchest.config.hologram.calculation;

import de.epiceric.shopchest.config.hologram.HologramFormat;

import java.util.Map;
import java.util.function.Function;

/**
 * Represents a hologram calculation
 */
public interface Calculation {

    double calculate(Map<HologramFormat.Requirement, Object> provider);

    abstract class AbstractCalculation implements Calculation {

        protected final Function<Map<HologramFormat.Requirement, Object>, Double> firstArgProvider;
        protected final Function<Map<HologramFormat.Requirement, Object>, Double> secondArgProvider;

        public AbstractCalculation(
                Function<Map<HologramFormat.Requirement, Object>, Double> firstArgProvider,
                Function<Map<HologramFormat.Requirement, Object>, Double> secondArgProvider
        ) {
            this.firstArgProvider = firstArgProvider;
            this.secondArgProvider = secondArgProvider;
        }

    }

    class Addition extends AbstractCalculation {

        public Addition(
                Function<Map<HologramFormat.Requirement, Object>, Double> firstArgProvider,
                Function<Map<HologramFormat.Requirement, Object>, Double> secondArgProvider
        ) {
            super(firstArgProvider, secondArgProvider);
        }

        @Override
        public double calculate(Map<HologramFormat.Requirement, Object> requirementValues) {
            return this.firstArgProvider.apply(requirementValues) + secondArgProvider.apply(requirementValues);
        }

        @Override
        public String toString() {
            return "(" + firstArgProvider.toString() + " + " + secondArgProvider.toString() + ")";
        }
    }

    class Subtraction extends AbstractCalculation {

        public Subtraction(
                Function<Map<HologramFormat.Requirement, Object>, Double> firstArgProvider,
                Function<Map<HologramFormat.Requirement, Object>, Double> secondArgProvider
        ) {
            super(firstArgProvider, secondArgProvider);
        }

        @Override
        public double calculate(Map<HologramFormat.Requirement, Object> requirementValues) {
            return this.firstArgProvider.apply(requirementValues) - secondArgProvider.apply(requirementValues);
        }

        @Override
        public String toString() {
            return "(" + firstArgProvider.toString() + " - " + secondArgProvider.toString() + ")";
        }
    }

    class Multiplication extends AbstractCalculation {

        public Multiplication(
                Function<Map<HologramFormat.Requirement, Object>, Double> firstArgProvider,
                Function<Map<HologramFormat.Requirement, Object>, Double> secondArgProvider
        ) {
            super(firstArgProvider, secondArgProvider);
        }

        @Override
        public double calculate(Map<HologramFormat.Requirement, Object> requirementValues) {
            return this.firstArgProvider.apply(requirementValues) * secondArgProvider.apply(requirementValues);
        }

        @Override
        public String toString() {
            return "(" + firstArgProvider.toString() + " * " + secondArgProvider.toString() + ")";
        }
    }

    class Division extends AbstractCalculation {

        public Division(
                Function<Map<HologramFormat.Requirement, Object>, Double> firstArgProvider,
                Function<Map<HologramFormat.Requirement, Object>, Double> secondArgProvider
        ) {
            super(firstArgProvider, secondArgProvider);
        }

        @Override
        public double calculate(Map<HologramFormat.Requirement, Object> requirementValues) {
            return this.firstArgProvider.apply(requirementValues) / secondArgProvider.apply(requirementValues);
        }

        @Override
        public String toString() {
            return "(" + firstArgProvider.toString() + " / " + secondArgProvider.toString() + ")";
        }
    }

    class Modulo extends AbstractCalculation {

        public Modulo(
                Function<Map<HologramFormat.Requirement, Object>, Double> firstArgProvider,
                Function<Map<HologramFormat.Requirement, Object>, Double> secondArgProvider
        ) {
            super(firstArgProvider, secondArgProvider);
        }

        @Override
        public double calculate(Map<HologramFormat.Requirement, Object> requirementValues) {
            return this.firstArgProvider.apply(requirementValues) % secondArgProvider.apply(requirementValues);
        }

        @Override
        public String toString() {
            return "(" + firstArgProvider.toString() + " % " + secondArgProvider.toString() + ")";
        }
    }

}
