package de.epiceric.shopchest.config.hologram.provider;

import de.epiceric.shopchest.config.hologram.HologramFormat;

import java.util.Map;
import java.util.function.Function;

public interface RequirementProvider<T> extends Function<Map<HologramFormat.Requirement, Object>, T> {

}
