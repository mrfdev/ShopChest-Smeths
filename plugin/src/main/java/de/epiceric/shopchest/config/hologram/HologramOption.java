package de.epiceric.shopchest.config.hologram;

import de.epiceric.shopchest.config.Placeholder;
import de.epiceric.shopchest.config.hologram.condition.Condition;
import de.epiceric.shopchest.config.hologram.line.FormattedLine;

import java.util.List;
import java.util.Map;

public class HologramOption {

    private final FormattedLine<Placeholder> formattedString;
    private final List<Condition<Map<HologramFormat.Requirement, Object>>> requirements;

    public HologramOption(FormattedLine<Placeholder> formattedString, List<Condition<Map<HologramFormat.Requirement, Object>>> requirements) {
        this.formattedString = formattedString;
        this.requirements = requirements;
    }

    public boolean isValid(Map<HologramFormat.Requirement, Object> requirementsValues) {
        if (requirementsValues == null) {
            return true;
        }
        for (Condition<Map<HologramFormat.Requirement, Object>> condition : requirements) {
            if (!condition.test(requirementsValues)) {
                return false;
            }
        }
        return true;
    }

    public String getFormat(Map<Placeholder, Object> placeholderValues) {
        return formattedString.get(placeholderValues);
    }

}
