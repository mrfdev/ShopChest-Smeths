package de.epiceric.shopchest.config.hologram.provider;

import de.epiceric.shopchest.config.hologram.HologramFormat;

import java.util.Map;
import java.util.Objects;

public class ConstantProvider<T> implements RequirementProvider<T> {

    private final T constant;

    public ConstantProvider(T constant) {
        this.constant = constant;
    }


    @Override
    public T apply(Map<HologramFormat.Requirement, Object> requirementObjectMap) {
        return constant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstantProvider<?> that = (ConstantProvider<?>) o;
        return Objects.equals(constant, that.constant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(constant);
    }

    @Override
    public String toString() {
        return constant.toString();
    }
}
