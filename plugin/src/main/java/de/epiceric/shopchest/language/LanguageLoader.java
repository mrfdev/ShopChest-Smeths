package de.epiceric.shopchest.language;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.FileLoader;
import de.epiceric.shopchest.config.LanguageConfigurationLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

public class LanguageLoader {

    private final static String DEFAULT_LOCALE = "en_US";
    private final static String MESSAGES_FILENAME = "messages";
    private final static String ITEMS_FILENAME = "items";

    @NotNull
    public LanguageManager loadLanguageManager(@NotNull ShopChest shopChestPlugin, @NotNull String locale) {
        final Logger logger = shopChestPlugin.getLogger();
        final FileLoader fileLoader = new FileLoader();
        final LanguageConfigurationLoader languageConfigurationLoader = new LanguageConfigurationLoader();

        final String requestedMessagePath = generateLocalizedPath(MESSAGES_FILENAME, locale);
        final String defaultResourceMessagePath = generateLocalizedPath(MESSAGES_FILENAME, DEFAULT_LOCALE);
        final File messagesFile;
        try {
            messagesFile = fileLoader.loadFile(requestedMessagePath, shopChestPlugin, requestedMessagePath, defaultResourceMessagePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final Map<String, String> storedMessages = languageConfigurationLoader.getTranslations(messagesFile, logger);
        final MessageRegistryLoader messageRegistryLoader = new MessageRegistryLoader(storedMessages);
        final String[] messages = messageRegistryLoader.getMessages();
        final MessageRegistry messageRegistry = new MessageRegistry(messages, p -> shopChestPlugin.getEconomy().format(p));

        final String requestedItemsPath = generateLocalizedPath(ITEMS_FILENAME, locale);
        final File itemsFile;
        try {
            itemsFile = fileLoader.loadFile(requestedItemsPath, shopChestPlugin);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final Map<String, String> storedItems = languageConfigurationLoader.getTranslations(itemsFile, logger);
        final LocalizedItemManager localizedItemManager = new LocalizedItemManager(storedItems);
        return new LanguageManager(messageRegistry, localizedItemManager);
    }

    @NotNull
    private String generateLocalizedPath(@NotNull String fileName, @NotNull String locale) {
        return "lang" + File.separator + fileName + "-" + locale + ".lang";
    }

}
