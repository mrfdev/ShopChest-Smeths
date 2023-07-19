package de.epiceric.shopchest.language;

import de.epiceric.shopchest.config.Placeholder;

public class Replacement {

    private final Placeholder placeholder;
    private final String replacement;

    public Replacement(Placeholder placeholder, Object replacement) {
        this.placeholder = placeholder;
        this.replacement = String.valueOf(replacement);
    }

    /**
     * @return String which will replace the placeholder
     */
    public String getReplacement() {
        return replacement;
    }

    /**
     * @return Placeholder that will be replaced
     */
    public Placeholder getPlaceholder() {
        return placeholder;
    }

}
