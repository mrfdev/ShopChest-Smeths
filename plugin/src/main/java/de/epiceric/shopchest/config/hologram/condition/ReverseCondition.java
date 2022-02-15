package de.epiceric.shopchest.config.hologram.condition;

import de.epiceric.shopchest.config.hologram.HologramFormat;

import java.util.Map;
import java.util.Objects;

public class ReverseCondition implements Condition {

    private final Condition condition;

    public ReverseCondition(Condition condition) {
        this.condition = condition;
    }

    @Override
    public boolean test(Map<HologramFormat.Requirement, Object> requirementValues) {
        return !condition.test(requirementValues);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReverseCondition that = (ReverseCondition) o;
        return Objects.equals(condition, that.condition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition);
    }

    @Override
    public String toString() {
        return "(!" + condition.toString() + ")";
    }
}
