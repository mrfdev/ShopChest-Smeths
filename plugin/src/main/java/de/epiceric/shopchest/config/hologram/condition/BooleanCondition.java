package de.epiceric.shopchest.config.hologram.condition;

import de.epiceric.shopchest.config.hologram.HologramFormat;

import java.util.Map;
import java.util.function.Function;

public class BooleanCondition implements Condition {

    private final Function<Map<HologramFormat.Requirement, Object>, Boolean> booleanProvider;

    public BooleanCondition(Function<Map<HologramFormat.Requirement, Object>, Boolean> booleanProvider) {
        this.booleanProvider = booleanProvider;
    }

    @Override
    public boolean test(Map<HologramFormat.Requirement, Object> requirementValues) {
        return booleanProvider.apply(requirementValues);
    }

    @Override
    public String toString() {
        return booleanProvider.toString();
    }
}
