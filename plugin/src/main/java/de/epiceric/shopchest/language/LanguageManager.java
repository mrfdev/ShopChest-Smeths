package de.epiceric.shopchest.language;

import org.jetbrains.annotations.NotNull;

public class LanguageManager {

    private final MessageRegistry messageRegistry;
    private final LocalizedItemManager localizedItemManager;

    public LanguageManager(@NotNull MessageRegistry messageRegistry, @NotNull LocalizedItemManager localizedItemManager) {
        this.messageRegistry = messageRegistry;
        this.localizedItemManager = localizedItemManager;
    }

    @NotNull
    public MessageRegistry getMessageRegistry() {
        return messageRegistry;
    }

    @NotNull
    public LocalizedItemManager getLocalizedItemManager() {
        return localizedItemManager;
    }

}
