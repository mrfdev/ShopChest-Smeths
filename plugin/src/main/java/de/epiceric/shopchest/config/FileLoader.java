package de.epiceric.shopchest.config;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class FileLoader {

    @NotNull
    public File loadFile(@NotNull String relativePath, @NotNull Plugin plugin, @NotNull String... resourcePaths) throws IOException {
        final File file = new File(plugin.getDataFolder(), relativePath);
        if (file.exists()) {
            if (!file.isFile()) {
                throw new RuntimeException("'" + file.getAbsoluteFile() + "'should be a file");
            }
            return file;
        }
        final File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        for (String resourcePath : resourcePaths) {
            if (loadResource(file, plugin, resourcePath)) {
                return file;
            }
        }
        file.createNewFile();
        return file;
    }

    private boolean loadResource(@NotNull File out, @NotNull Plugin plugin, @Nullable String resourcePath) throws IOException {
        final InputStream is;
        if (resourcePath == null || (is = plugin.getResource(resourcePath)) == null) {
            return false;
        }
        Files.copy(is, out.toPath());
        return true;
    }

}
