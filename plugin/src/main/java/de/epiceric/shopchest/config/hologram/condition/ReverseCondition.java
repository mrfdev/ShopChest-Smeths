package de.epiceric.shopchest.config.hologram.condition;

import de.epiceric.shopchest.config.hologram.HologramFormat;

import java.util.Map;

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
    public String toString() {
        return "(!" + condition.toString() + ")";
    }
}
