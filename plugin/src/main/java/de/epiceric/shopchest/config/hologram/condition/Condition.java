package de.epiceric.shopchest.config.hologram.condition;

import de.epiceric.shopchest.config.hologram.HologramFormat;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Represent an hologram requirement condition
 */
public interface Condition extends Predicate<Map<HologramFormat.Requirement, Object>> {

}
