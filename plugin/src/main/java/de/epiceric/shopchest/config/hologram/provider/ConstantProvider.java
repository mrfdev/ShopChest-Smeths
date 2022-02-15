package de.epiceric.shopchest.config.hologram.provider;

import de.epiceric.shopchest.config.hologram.HologramFormat;

import java.util.Map;

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
    public String toString() {
        return constant.toString();
    }
}
