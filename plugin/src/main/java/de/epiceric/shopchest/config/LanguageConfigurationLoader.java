package de.epiceric.shopchest.config;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LanguageConfigurationLoader {

    @NotNull
    public Map<String, String> getTranslations(@NotNull File file, @NotNull Logger logger) {
        final List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Can not read the message registry", e);
            return Collections.emptyMap();
        }
        final Map<String, String> registry = new HashMap<>();
        for (String line : lines) {
            line = line.trim();
            if (line.length() == 0 || line.charAt(0) == '#') {
                continue;
            }
            final String[] parts = line.split("=", 2);
            if (parts.length != 2) {
                logger.warning("Invalid line in message registry : '" + line + "'");
                continue;
            }
            final String id = parts[0];
            final String value = parts[1];
            registry.put(id, value);
        }
        return registry;
    }

}
