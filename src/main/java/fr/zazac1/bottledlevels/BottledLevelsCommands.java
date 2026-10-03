package fr.zazac1.bottledlevels;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class BottledLevelsCommands {
    private BottledLevelsCommands() { }

    public static void register() { NeoForge.EVENT_BUS.addListener(BottledLevelsCommands::registerCommands); }

    private static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("bottledlevels")
                .requires(source -> source.hasPermission(2))
                .executes(context -> show(context.getSource()))
                .then(Commands.literal("capacity").then(Commands.argument("levels", IntegerArgumentType.integer(WorldBottleConfig.MIN_CAPACITY, WorldBottleConfig.MAX_CAPACITY)).executes(context -> setCapacity(context.getSource(), IntegerArgumentType.getInteger(context, "levels")))))
                .then(Commands.literal("damage")
                        .then(Commands.argument("enabled", BoolArgumentType.bool()).executes(context -> setDamageEnabled(context.getSource(), BoolArgumentType.getBool(context, "enabled"))))
                        .then(Commands.literal("amount").then(Commands.argument("health", FloatArgumentType.floatArg(WorldBottleConfig.MIN_DEPOSIT_DAMAGE, WorldBottleConfig.MAX_DEPOSIT_DAMAGE)).executes(context -> setDamageAmount(context.getSource(), FloatArgumentType.getFloat(context, "health"))))))
                .then(Commands.literal("cooldown").then(Commands.argument("seconds", IntegerArgumentType.integer(WorldBottleConfig.MIN_COOLDOWN_SECONDS, WorldBottleConfig.MAX_COOLDOWN_SECONDS)).executes(context -> setCooldown(context.getSource(), IntegerArgumentType.getInteger(context, "seconds"))))));
    }

    private static int show(CommandSourceStack source) { WorldBottleConfig c = WorldBottleConfig.get(source.getServer()); source.sendSuccess(() -> Component.literal("Bottled Levels — capacity: " + c.maxLevels + ", deposit damage: " + (c.damageOnDeposit ? c.depositDamage : "off") + ", cooldown: " + c.cooldownSeconds + "s"), false); return Command.SINGLE_SUCCESS; }
    private static int setCapacity(CommandSourceStack source, int levels) { WorldBottleConfig c = WorldBottleConfig.get(source.getServer()); c.maxLevels = levels; WorldBottleConfig.save(source.getServer()); source.sendSuccess(() -> Component.literal("Bottled Levels capacity set to " + levels + " levels."), true); return Command.SINGLE_SUCCESS; }
    private static int setDamageEnabled(CommandSourceStack source, boolean enabled) { WorldBottleConfig c = WorldBottleConfig.get(source.getServer()); c.damageOnDeposit = enabled; WorldBottleConfig.save(source.getServer()); source.sendSuccess(() -> Component.literal("Bottled Levels deposit damage " + (enabled ? "enabled." : "disabled.")), true); return Command.SINGLE_SUCCESS; }
    private static int setDamageAmount(CommandSourceStack source, float amount) { WorldBottleConfig c = WorldBottleConfig.get(source.getServer()); c.depositDamage = amount; WorldBottleConfig.save(source.getServer()); source.sendSuccess(() -> Component.literal("Bottled Levels deposit damage set to " + amount + "."), true); return Command.SINGLE_SUCCESS; }
    private static int setCooldown(CommandSourceStack source, int seconds) { WorldBottleConfig c = WorldBottleConfig.get(source.getServer()); c.cooldownSeconds = seconds; WorldBottleConfig.save(source.getServer()); source.sendSuccess(() -> Component.literal("Bottled Levels cooldown set to " + seconds + " seconds."), true); return Command.SINGLE_SUCCESS; }
}
