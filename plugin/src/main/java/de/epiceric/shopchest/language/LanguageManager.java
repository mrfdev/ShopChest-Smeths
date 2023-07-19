package de.epiceric.shopchest.language;

import org.jetbrains.annotations.NotNull;

public class LanguageManager {

    private final MessageRegistry messageRegistry;

    public LanguageManager(@NotNull MessageRegistry messageRegistry) {
        this.messageRegistry = messageRegistry;
    }

    @NotNull
    public MessageRegistry getMessageRegistry() {
        return messageRegistry;
    }

}
