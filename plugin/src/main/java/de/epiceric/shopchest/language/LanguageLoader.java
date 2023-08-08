package de.epiceric.shopchest.language;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.FileLoader;
import de.epiceric.shopchest.config.LanguageConfigurationLoader;
import de.epiceric.shopchest.language.item.ItemNameManager;

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

        final String messageLocalizedFileName = getLocalizedFileName(MESSAGES_FILENAME, locale);
        final String messageSavePath = getSavePath(messageLocalizedFileName);
        final String messageResourcePath = getResourcePath(messageLocalizedFileName);
        final String messageDefaultResourcePath = getResourcePath(getLocalizedFileName(MESSAGES_FILENAME, DEFAULT_LOCALE));
        final File messagesFile;
        try {
            messagesFile = fileLoader.loadFile(messageSavePath, shopChestPlugin, messageResourcePath, messageDefaultResourcePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final Map<String, String> storedMessages = languageConfigurationLoader.getTranslations(messagesFile, logger);
        final MessageRegistryLoader messageRegistryLoader = new MessageRegistryLoader(storedMessages);
        final String[] messages = messageRegistryLoader.getMessages();
        final MessageRegistry messageRegistry = new MessageRegistry(messages, p -> shopChestPlugin.getEconomy().format(p));

        final String itemSavePath = getSavePath(getLocalizedFileName(ITEMS_FILENAME, locale));
        final File itemsFile;
        try {
            itemsFile = fileLoader.loadFile(itemSavePath, shopChestPlugin);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final Map<String, String> storedItems = languageConfigurationLoader.getTranslations(itemsFile, logger);
        final ItemNameManager localizedItemManager = new ItemNameManager(storedItems);
        return new LanguageManager(messageRegistry, localizedItemManager);
    }

    @NotNull
    private String getLocalizedFileName(@NotNull String baseName, @NotNull String locale) {
        return baseName + "-" + locale + ".lang";
    }

    @NotNull
    private String getResourcePath(@NotNull String fileName) {
        return "lang/" + fileName;
    }

    @NotNull
    private String getSavePath(@NotNull String fileName) {
        return "lang" + File.separator + fileName;
    }

}
