package de.epiceric.shopchest.config.hologram.condition;

import de.epiceric.shopchest.config.hologram.HologramFormat;

import java.util.Map;
import java.util.Objects;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BooleanCondition that = (BooleanCondition) o;
        return Objects.equals(booleanProvider, that.booleanProvider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(booleanProvider);
    }

    @Override
    public String toString() {
        return booleanProvider.toString();
    }
}
