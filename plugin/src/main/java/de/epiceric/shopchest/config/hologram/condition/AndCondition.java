package de.epiceric.shopchest.config.hologram.condition;

import de.epiceric.shopchest.config.hologram.HologramFormat;

import java.util.Map;

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
    public String toString() {
        return "(" + firstCondition.toString() + " && " + secondCondition.toString() + ")";
    }
}
