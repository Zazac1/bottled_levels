package com.zazac1.bottledlevels;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("bottled_levels.json");

    public static ModConfig INSTANCE = new ModConfig();

    /** Must hold right-click to drink before getting XP. If false, XP is released instantly on click. */
    public boolean mustDrink = true;

    /** Maximum number of levels the bottle can store. */
    public int maxLevels = 30;

    /** Whether bottles at the same level can stack. Requires restart. */
    public boolean stackable = true;

    public static void load() {
        if (Files.exists(CONFIG_FILE)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
                if (loaded != null) INSTANCE = loaded;
            } catch (IOException e) {
                INSTANCE = new ModConfig();
            }
        }
        save();
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException ignored) {}
    }
}
