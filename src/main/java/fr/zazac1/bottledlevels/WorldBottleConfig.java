package fr.zazac1.bottledlevels;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Configuration stored beside level.dat, so each world/server has its own rules. */
public final class WorldBottleConfig {
    public static final int MIN_CAPACITY = 1;
    public static final int MAX_CAPACITY = 1_000_000;
    public static final float MIN_DEPOSIT_DAMAGE = 0.0f;
    public static final float MAX_DEPOSIT_DAMAGE = 1_000.0f;
    public static final int MIN_COOLDOWN_SECONDS = 0;
    public static final int MAX_COOLDOWN_SECONDS = 3_600;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static MinecraftServer loadedServer;
    private static WorldBottleConfig loadedConfig;

    /** Capacity expressed in whole levels. */
    public int maxLevels = 30;
    /** Whether a successful deposit hurts the depositing player. */
    public boolean damageOnDeposit = false;
    /** Damage dealt once per successful deposit, in health points. */
    public float depositDamage = 2.0f;
    /** Cooldown after a successful deposit or drink, in seconds. Zero disables it. */
    public int cooldownSeconds = 5;

    public static WorldBottleConfig get(MinecraftServer server) {
        if (loadedServer != server) {
            loadedServer = server;
            loadedConfig = load(server);
        }
        return loadedConfig;
    }

    public static void save(MinecraftServer server) {
        WorldBottleConfig config = get(server);
        normalize(config);
        Path file = file(server);
        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save Bottled Levels world configuration", e);
        }
    }

    private static WorldBottleConfig load(MinecraftServer server) {
        Path file = file(server);
        WorldBottleConfig config = new WorldBottleConfig();
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                WorldBottleConfig read = GSON.fromJson(reader, WorldBottleConfig.class);
                if (read != null) config = read;
            } catch (IOException | RuntimeException ignored) {
                // Keep safe defaults when a world config cannot be read.
            }
        }
        normalize(config);
        return config;
    }

    private static Path file(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("bottled_levels.json");
    }

    private static void normalize(WorldBottleConfig config) {
        config.maxLevels = Math.clamp(config.maxLevels, MIN_CAPACITY, MAX_CAPACITY);
        config.depositDamage = Math.clamp(config.depositDamage, MIN_DEPOSIT_DAMAGE, MAX_DEPOSIT_DAMAGE);
        config.cooldownSeconds = Math.clamp(config.cooldownSeconds, MIN_COOLDOWN_SECONDS, MAX_COOLDOWN_SECONDS);
    }
}
