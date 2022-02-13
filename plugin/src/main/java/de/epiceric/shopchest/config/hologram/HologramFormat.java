package de.epiceric.shopchest.config.hologram;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import de.epiceric.shopchest.config.Placeholder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import de.epiceric.shopchest.ShopChest;

public class HologramFormat {

    /*
    TODO Change implementation of this class
      -> Deserialize from the configuration and load it some way to avoid String manipulation at each method invocation
       -> Rework the current String manipulation process to add complex expression evaluation WITHOUT nashorn engine
     */

    public enum Requirement {
        VENDOR, AMOUNT, ITEM_TYPE, ITEM_NAME, HAS_ENCHANTMENT, BUY_PRICE,
        SELL_PRICE, HAS_POTION_EFFECT, IS_MUSIC_DISC, IS_POTION_EXTENDED, IS_BANNER_PATTERN,
        IS_WRITTEN_BOOK, ADMIN_SHOP, NORMAL_SHOP, IN_STOCK, MAX_STACK, CHEST_SPACE, DURABILITY
    }

    // no "-" sign since no variable can be negative
    // e.g.: 100.0 >= 50.0
    private static final Pattern SIMPLE_NUMERIC_CONDITION = Pattern.compile("^(\\d+(?:\\.\\d+)?) ([<>][=]?|[=!]=) (\\d+(?:\\.\\d+)?)$");

    // e.g.: "STONE" == "DIAMOND_SWORD"
    private static final Pattern SIMPLE_STRING_CONDITION = Pattern.compile("^\"([^\"]*)\" ([=!]=) \"([^\"]*)\"$");

    private ScriptEngineManager manager = new ScriptEngineManager();
    private ScriptEngine engine = manager.getEngineByName("JavaScript");

    private ShopChest plugin;
    private File configFile;
    private YamlConfiguration config;

    public HologramFormat(ShopChest plugin) {
        this.configFile = new File(plugin.getDataFolder(), "hologram-format.yml");
        this.config = YamlConfiguration.loadConfiguration(configFile);
        this.plugin = plugin;
    }

    /**
     * Get the format for the given line of the hologram
     * @param line Line of the hologram
     * @param reqMap Values of the requirements that might be needed by the format (contains {@code null} if not comparable)
     * @param plaMap Values of the placeholders that might be needed by the format
     * @return  The format of the first working option, or an empty String if no option is working
     *          because of not fulfilled requirements
     */
    public String getFormat(int line, Map<Requirement, Object> reqMap, Map<Placeholder, Object> plaMap) {
        ConfigurationSection options = config.getConfigurationSection("lines." + line + ".options");

        // For every option
        optionLoop:
        for (String key : options.getKeys(false)) {
            ConfigurationSection option = options.getConfigurationSection(key);
            List<String> requirements = option.getStringList("requirements");

            // Check every requirement
            for (String sReq : requirements) {
                //TODO Maybe remove some loops as every requirements are re evaluated in the #evalRequirement
                for (Requirement req : reqMap.keySet()) {
                    // If the configuration requirement contain a requirement specified by the shop
                    if (sReq.contains(req.toString())) {
                        // Then evaluate the requirement.
                        // If the requirement is not fulfilled, skip this option and go to the next one
                        if (!evalRequirement(sReq, reqMap)) {
                            continue optionLoop;
                        }
                        // TODO Maybe skip to the next config requirement as the requirement has been found and is valid
                    }
                }
            }

            // Here, every requirement is fulfilled and this is the first valid option

            String format = option.getString("format");

            // Evaluate placeholders and return the formatted line
            return evalPlaceholder(format, plaMap);
        }

        // No option matching to que shop requirements
        // Returning an empty string
        return "";
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * @return Whether the hologram text has to change dynamically without reloading
     */
    public boolean isDynamic() {
        // Return whether an option contains STOCK or CHEST_SPACE :
        // - In the format
        // - In one of its requirement
        int count = getLineCount();
        for (int i = 0; i < count; i++) {
            ConfigurationSection options = config.getConfigurationSection("lines." + i + ".options");

            for (String key : options.getKeys(false)) {
                ConfigurationSection option = options.getConfigurationSection(key);

                String format = option.getString("format");
                if (format.contains(Placeholder.STOCK.toString()) || format.contains(Placeholder.CHEST_SPACE.toString())) {
                    return true;
                }

                for (String req : option.getStringList("requirements")) {
                    if (req.contains(Requirement.IN_STOCK.toString()) || req.contains(Requirement.CHEST_SPACE.toString())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * @return Amount of lines in a hologram
     */
    public int getLineCount() {
        return config.getConfigurationSection("lines").getKeys(false).size();
    }

    /**
     * @return Configuration of the "hologram-format.yml" file
     */
    public YamlConfiguration getConfig() {
        return config;
    }

    /**
     * Parse and evaluate a condition
     * @param condition Condition to evaluate
     * @param values Values of the requirements
     * @return Result of the condition
     */
    public boolean evalRequirement(String condition, Map<Requirement, Object> values) {
        String cond = condition;

        // Double-check the presence of a requirement (WTF ?)
        for (HologramFormat.Requirement req : HologramFormat.Requirement.values()) {
            if (cond.contains(req.toString()) && values.containsKey(req)) {
                Object val = values.get(req);
                String sVal = String.valueOf(val);

                if (val instanceof String && !(sVal.startsWith("\"") && sVal.endsWith("\""))) {
                    sVal = String.format("\"%s\"", sVal);
                }

                cond = cond.replace(req.toString(), sVal);
            }
        }

        // Evaluate three basic condition : Direct Boolean, Math comparison and String equality

        if (cond.equals("true")) {
            // e.g.: ADMIN_SHOP
            return true;
        } else if (cond.equals("false")) {
            return false;
        } else {
            char firstChar = cond.charAt(0);

            // numeric cond: first char must be a digit (no variable can be negative)
            if (firstChar >= '0' && firstChar <= '9') {
                Matcher matcher = SIMPLE_NUMERIC_CONDITION.matcher(cond);

                if (matcher.find()) {
                    double a, b;
                    Operator operator;
                    try {
                        a = Double.parseDouble(matcher.group(1));
                        operator = Operator.from(matcher.group(2));
                        b = Double.parseDouble(matcher.group(3));

                        return operator.compare(a, b);
                    } catch (IllegalArgumentException ignored) {
                        // should not happen, since regex checked that there is valid number and valid operator
                    }
                }
            }

            // string cond: first char must be a: "
            if (firstChar == '"') {
                Matcher matcher = SIMPLE_STRING_CONDITION.matcher(cond);

                if (matcher.find()) {
                    String a, b;
                    Operator operator;
                    try {
                        a = matcher.group(1);
                        operator = Operator.from(matcher.group(2));
                        b = matcher.group(3);

                        return operator.compare(a, b);
                    } catch (IllegalArgumentException | UnsupportedOperationException ignored) {
                        // should not happen, since regex checked that there is valid operator
                    }
                }
            }

            // complex comparison
            // Like && and || or other arithmetic operations
            try {
                return (boolean) engine.eval(cond);
            } catch (ScriptException e) {
                plugin.debug("Failed to eval condition: " + condition);
                plugin.debug(e);
                return false;
            }
        }
    }

    /**
     * Parse and evaluate a condition
     * @param string Message or hologram format whose containing scripts to execute
     * @param values Values of the placeholders
     * @return Result of the condition
     */
    public String evalPlaceholder(String string, Map<Placeholder, Object> values) {
        // Detect and evaluate accolade inner parts
        try {
            Matcher matcher = Pattern.compile("\\{([^}]+)}").matcher(string);
            String newString = string;

            while (matcher.find()) {
                String withBrackets = matcher.group();
                String script = withBrackets.substring(1, withBrackets.length() - 1);

                for (Placeholder placeholder : values.keySet()) {
                    if (script.contains(placeholder.toString())) {
                        Object val = values.get(placeholder);
                        String sVal = String.valueOf(val);

                        if (val instanceof String && !(sVal.startsWith("\"") && sVal.endsWith("\""))) {
                            sVal = String.format("\"%s\"", sVal);
                        }

                        script = script.replace(placeholder.toString(), sVal);
                    }
                }

                String result = String.valueOf(engine.eval(script));
                newString = newString.replace(withBrackets, result);
            }

            return newString;
        } catch (ScriptException e) {
            plugin.debug("Failed to eval placeholder script in string: " + string);
            plugin.debug(e);
        }

        return string;
    }
}
