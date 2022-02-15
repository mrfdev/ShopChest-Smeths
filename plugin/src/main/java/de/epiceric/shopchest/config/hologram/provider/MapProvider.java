package de.epiceric.shopchest.config.hologram.provider;

import de.epiceric.shopchest.config.hologram.HologramFormat;

import java.util.Map;
import java.util.Objects;

public abstract class MapProvider<T> implements RequirementProvider<T> {

    protected final HologramFormat.Requirement requirement;

    public MapProvider(HologramFormat.Requirement requirement) {
        this.requirement = requirement;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapProvider<?> that = (MapProvider<?>) o;
        return requirement == that.requirement;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requirement);
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
