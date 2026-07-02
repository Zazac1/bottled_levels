package com.zazac1.bottledlevels.client;

import com.zazac1.bottledlevels.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class ModConfigScreen extends Screen {

    private final Screen parent;

    private boolean mustDrink;
    private int maxLevels;
    private boolean stackable;

    private static final int BTN_W = 200;
    private static final int BTN_H = 20;

    public ModConfigScreen(Screen parent) {
        super(Text.literal("Experience Bottle - Config"));
        this.parent = parent;
        this.mustDrink = ModConfig.INSTANCE.mustDrink;
        this.maxLevels = ModConfig.INSTANCE.maxLevels;
        this.stackable = ModConfig.INSTANCE.stackable;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int startY = this.height / 2 - 50;

        // Drink to get XP toggle
        addDrawableChild(CyclingButtonWidget.onOffBuilder(mustDrink)
                .build(cx - BTN_W / 2, startY, BTN_W, BTN_H,
                        Text.literal("Drink to get XP"),
                        (btn, val) -> mustDrink = val));

        // Max Levels slider (5 to 100, snapped to multiples of 5)
        addDrawableChild(new SliderWidget(cx - BTN_W / 2, startY + 28, BTN_W, BTN_H,
                Text.literal("Max Levels: " + maxLevels), (double)(maxLevels - 5) / (100 - 5)) {
            @Override
            protected void updateMessage() {
                int val = 5 + (int) Math.round(this.value * (100 - 5));
                maxLevels = Math.max(5, (val / 5) * 5);
                setMessage(Text.literal("Max Levels: " + maxLevels));
            }
            @Override
            protected void applyValue() {
                int val = 5 + (int) Math.round(this.value * (100 - 5));
                maxLevels = Math.max(5, (val / 5) * 5);
            }
        });

        // Stackable toggle
        addDrawableChild(CyclingButtonWidget.onOffBuilder(stackable)
                .build(cx - BTN_W / 2, startY + 56, BTN_W, BTN_H,
                        Text.literal("Stackable (restart)"),
                        (btn, val) -> stackable = val));

        // Done
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> {
            ModConfig.INSTANCE.mustDrink = mustDrink;
            ModConfig.INSTANCE.maxLevels = maxLevels;
            ModConfig.INSTANCE.stackable = stackable;
            ModConfig.save();
            if (client != null) client.setScreen(parent);
        }).dimensions(cx - BTN_W / 2, startY + 90, BTN_W / 2 - 2, BTN_H).build());

        // Cancel
        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), btn -> {
            if (client != null) client.setScreen(parent);
        }).dimensions(cx + 2, startY + 90, BTN_W / 2 - 2, BTN_H).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, this.width / 2, this.height / 2 - 80, 0xFFFFFF);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
}

