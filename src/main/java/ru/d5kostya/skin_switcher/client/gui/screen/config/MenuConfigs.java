package ru.d5kostya.skinswitcher.client.gui.screen.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBooleanBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.text.HorizontalRuleBuilder;
import ru.kelcuprum.alinlib.gui.screens.ConfigScreenBuilder;
import ru.d5kostya.skinswitcher.client.skin_switcher;

public class MenuConfigs {
    public static Screen build(Screen parent){
        ConfigScreenBuilder screenBuilder = new ConfigScreenBuilder(parent, Component.literal("Skin switcher"));
        screenBuilder.setCategoryTitle(Component.translatable("skin_switcher.config.menu"));
        screenBuilder.addPanelWidget(new ButtonBuilder(Component.translatable("skin_switcher.config.main"), (s) -> AlinLib.MINECRAFT.setScreen(MainConfigs.build(parent))));
        screenBuilder.addPanelWidget(new ButtonBuilder(Component.translatable("skin_switcher.config.menu"), (s) -> AlinLib.MINECRAFT.setScreen(MenuConfigs.build(parent))));

        screenBuilder.addWidget(new ButtonBooleanBuilder(Component.translatable("skin_switcher.config.menu.change_default_ui"), true).setConfig(skin_switcher.config, "MENU.CHANGE_DEFAULT_UI"));
        screenBuilder.addWidget(new ButtonBooleanBuilder(Component.translatable("skin_switcher.config.menu.alinlib"), false).setConfig(skin_switcher.config, "MENU.ALINLIB"));
        screenBuilder.addWidget(new ButtonBooleanBuilder(Component.translatable("skin_switcher.config.menu.two_one_slot"), true).setConfig(skin_switcher.config, "MENU.TWO_ONE_SLOT"));
        screenBuilder.addWidget(new HorizontalRuleBuilder());
        screenBuilder.addWidget(new ButtonBooleanBuilder(Component.translatable("skin_switcher.config.menu.title"), true).setConfig(skin_switcher.config, "MENU.TITLE"));
        screenBuilder.addWidget(new ButtonBooleanBuilder(Component.translatable("skin_switcher.config.menu.title.left"), false).setConfig(skin_switcher.config, "MENU.TITLE.LEFT"));
        screenBuilder.addWidget(new HorizontalRuleBuilder());
        screenBuilder.addWidget(new ButtonBooleanBuilder(Component.translatable("skin_switcher.config.menu.pause"), true).setConfig(skin_switcher.config, "MENU.PAUSE"));
        screenBuilder.addWidget(new ButtonBooleanBuilder(Component.translatable("skin_switcher.config.menu.pause.left"), false).setConfig(skin_switcher.config, "MENU.PAUSE.LEFT"));
        return screenBuilder.build();
    }
}
