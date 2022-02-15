package de.epiceric.shopchest.config.hologram.provider;

import de.epiceric.shopchest.config.hologram.HologramFormat;

import java.util.Map;

public abstract class MapProvider<T> implements RequirementProvider<T> {

    protected final HologramFormat.Requirement requirement;

    public MapProvider(HologramFormat.Requirement requirement) {
        this.requirement = requirement;
    }

    @Override
    public String toString() {
        return requirement.toString();
    }

    public final static class StringMapProvider extends MapProvider<String> {

        public StringMapProvider(HologramFormat.Requirement requirement) {
            super(requirement);
        }

        @Override
        public String apply(Map<HologramFormat.Requirement, Object> requirementValues) {
            return (String) requirementValues.get(requirement);
        }

    }

    public final static class BooleanMapProvider extends MapProvider<Boolean> {

        public BooleanMapProvider(HologramFormat.Requirement requirement) {
            super(requirement);
        }

        @Override
        public Boolean apply(Map<HologramFormat.Requirement, Object> requirementValues) {
            return (Boolean) requirementValues.get(requirement);
        }

    }

    public final static class DoubleMapProvider extends MapProvider<Double> {

        public DoubleMapProvider(HologramFormat.Requirement requirement) {
            super(requirement);
        }

        @Override
        public Double apply(Map<HologramFormat.Requirement, Object> requirementValues) {
            return (Double) requirementValues.get(requirement);
        }

    }

}
