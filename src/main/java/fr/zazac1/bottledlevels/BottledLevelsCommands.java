package fr.zazac1.bottledlevels;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class BottledLevelsCommands {
    private BottledLevelsCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("bottledlevels")
                        .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS)))
                        .executes(context -> show(context.getSource()))
                        .then(literal("capacity").then(argument("levels", IntegerArgumentType.integer(
                                WorldBottleConfig.MIN_CAPACITY, WorldBottleConfig.MAX_CAPACITY))
                                .executes(context -> setCapacity(context.getSource(), IntegerArgumentType.getInteger(context, "levels")))))
                        .then(literal("damage")
                                .then(argument("enabled", BoolArgumentType.bool())
                                        .executes(context -> setDamageEnabled(context.getSource(), BoolArgumentType.getBool(context, "enabled"))))
                                .then(literal("amount").then(argument("health", FloatArgumentType.floatArg(
                                                WorldBottleConfig.MIN_DEPOSIT_DAMAGE, WorldBottleConfig.MAX_DEPOSIT_DAMAGE))
                                        .executes(context -> setDamageAmount(context.getSource(), FloatArgumentType.getFloat(context, "health"))))))
                        .then(literal("cooldown").then(argument("seconds", IntegerArgumentType.integer(
                                WorldBottleConfig.MIN_COOLDOWN_SECONDS, WorldBottleConfig.MAX_COOLDOWN_SECONDS))
                                .executes(context -> setCooldown(context.getSource(), IntegerArgumentType.getInteger(context, "seconds")))))
        ));
    }

    private static int show(CommandSourceStack source) {
        WorldBottleConfig config = WorldBottleConfig.get(source.getServer());
        source.sendSuccess(() -> Component.literal("Bottled Levels — capacity: " + config.maxLevels
                + ", deposit damage: " + (config.damageOnDeposit ? config.depositDamage : "off")
                + ", cooldown: " + config.cooldownSeconds + "s"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int setCapacity(CommandSourceStack source, int levels) {
        WorldBottleConfig config = WorldBottleConfig.get(source.getServer());
        config.maxLevels = levels;
        WorldBottleConfig.save(source.getServer());
        source.sendSuccess(() -> Component.literal("Bottled Levels capacity set to " + levels + " levels."), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int setDamageEnabled(CommandSourceStack source, boolean enabled) {
        WorldBottleConfig config = WorldBottleConfig.get(source.getServer());
        config.damageOnDeposit = enabled;
        WorldBottleConfig.save(source.getServer());
        source.sendSuccess(() -> Component.literal("Bottled Levels deposit damage " + (enabled ? "enabled." : "disabled.")), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int setDamageAmount(CommandSourceStack source, float amount) {
        WorldBottleConfig config = WorldBottleConfig.get(source.getServer());
        config.depositDamage = amount;
        WorldBottleConfig.save(source.getServer());
        source.sendSuccess(() -> Component.literal("Bottled Levels deposit damage set to " + amount + "."), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int setCooldown(CommandSourceStack source, int seconds) {
        WorldBottleConfig config = WorldBottleConfig.get(source.getServer());
        config.cooldownSeconds = seconds;
        WorldBottleConfig.save(source.getServer());
        source.sendSuccess(() -> Component.literal("Bottled Levels cooldown set to " + seconds + " seconds."), true);
        return Command.SINGLE_SUCCESS;
    }
}
