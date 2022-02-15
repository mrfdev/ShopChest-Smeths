package de.epiceric.shopchest.config.hologram.condition;

import de.epiceric.shopchest.config.hologram.HologramFormat;

import java.util.Map;
import java.util.Objects;

public class AndCondition implements Condition {

    private final Condition firstCondition;
    private final Condition secondCondition;

    public AndCondition(Condition firstCondition, Condition secondCondition) {
        this.firstCondition = firstCondition;
        this.secondCondition = secondCondition;
    }

    @Override
    public boolean test(Map<HologramFormat.Requirement, Object> requirementValues) {
        return firstCondition.test(requirementValues) && secondCondition.test(requirementValues);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AndCondition that = (AndCondition) o;
        return Objects.equals(firstCondition, that.firstCondition) && Objects.equals(secondCondition, that.secondCondition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstCondition, secondCondition);
    }

    @Override
    public String toString() {
        return "(" + firstCondition.toString() + " && " + secondCondition.toString() + ")";
    }
}
