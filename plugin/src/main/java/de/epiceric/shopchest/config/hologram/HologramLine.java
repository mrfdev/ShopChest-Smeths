package de.epiceric.shopchest.config.hologram;

import de.epiceric.shopchest.config.Placeholder;

import java.util.List;
import java.util.Map;

public class HologramLine {

    private final List<HologramOption> options;

    public HologramLine(List<HologramOption> options) {
        this.options = options;
    }

    public final String get(Map<HologramFormat.Requirement, Object> reqMap, Map<Placeholder, Object> plaMap) {
        for (HologramOption option : options) {
            if (option.isValid(reqMap)) {
                return option.getFormat(plaMap);
            }
        }
        return "";
    }

}
