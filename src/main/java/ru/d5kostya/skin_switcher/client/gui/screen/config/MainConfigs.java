package ru.d5kostya.skinswitcher.client.gui.screen.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.config.Config;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBooleanBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.editbox.EditBoxBuilder;
import ru.kelcuprum.alinlib.gui.components.text.CategoryBox;
import ru.kelcuprum.alinlib.gui.screens.ConfigScreenBuilder;
import ru.d5kostya.skinswitcher.client.skin_switcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class MainConfigs {
    public static Screen build(Screen parent){
        ConfigScreenBuilder screenBuilder = new ConfigScreenBuilder(parent, Component.literal("Skin switcher"));
        screenBuilder.setCategoryTitle(Component.translatable("skin_switcher.config.main"));
        screenBuilder.addPanelWidget(new ButtonBuilder(Component.translatable("skin_switcher.config.main"), (s) -> AlinLib.MINECRAFT.setScreen(MainConfigs.build(parent))));
        screenBuilder.addPanelWidget(new ButtonBuilder(Component.translatable("skin_switcher.config.menu"), (s) -> AlinLib.MINECRAFT.setScreen(MenuConfigs.build(parent))));

        screenBuilder.addWidget(new CategoryBox(Component.translatable("skin_switcher.config.main.command_on_select"))
                .addValue(new ButtonBooleanBuilder(Component.translatable("skin_switcher.config.main.command_on_select.enabled"), false).setConfig(skin_switcher.config, "COMMAND.ON.SELECT.ENABLED"))
                .addValue(new EditBoxBuilder(Component.translatable("skin_switcher.config.main.command_on_select.comand")).setValue("/skin url {URL}").setConfig(skin_switcher.config, "COMMAND.ON.SELECT.COMAND"))
        );

        screenBuilder.addWidget(new CategoryBox(Component.translatable("skin_switcher.config.main.data"))
                .addValue(new ButtonBooleanBuilder(Component.translatable("skin_switcher.config.main.data.use_global"), false).setConfig(skin_switcher.pathConfig, "USE_GLOBAL"))
                .addValue(new EditBoxBuilder(Component.translatable("skin_switcher.config.main.data.path")).setValue("{HOME}/skin_switcher").setConfig(skin_switcher.pathConfig, "PATH"))
                .addValue(new EditBoxBuilder(Component.translatable("skin_switcher.config.main.data.path.unix")).setValue("/home/{USER}/skin_switcher").setConfig(skin_switcher.pathConfig, "PATH.UNIX"))
                .addValue(new ButtonBuilder(Component.translatable("skin_switcher.config.main.data.move")).setOnPress((s) -> {
                    try {
                        if(!new File(skin_switcher.getPath()).exists()) Files.createDirectory(Path.of(skin_switcher.getPath()));
                        if(new File("config/skin_switcher/config.json").exists()) Files.copy(Path.of("config/skin_switcher/config.json"), Path.of(skin_switcher.getPath()+"/config.json"), REPLACE_EXISTING);
                        if(new File("config/skin_switcher/skins").exists()) {
                            if(!new File(skin_switcher.getPath()+"/skins").exists()) Files.copy(Path.of("config/skin_switcher/skins"), Path.of(skin_switcher.getPath()+"/skins"), REPLACE_EXISTING);
                            for(File file : new File("config/skin_switcher/skins").listFiles()) Files.copy(file.toPath(), Path.of(skin_switcher.getPath()+"/skins/"+file.getName()), REPLACE_EXISTING);
                        }
                        skin_switcher.config = new Config(skin_switcher.getPath()+"/config.json");
                        AlinLib.MINECRAFT.setScreen(build(parent));
                    } catch (IOException e) {
                        skin_switcher.logger.log(e.getMessage(), Level.ERROR);
                        e.printStackTrace();
                    }
                }))
                .addValue(new ButtonBuilder(Component.translatable("skin_switcher.config.main.data.update_config")).setOnPress((s) -> {
                    skin_switcher.config = new Config(skin_switcher.getPath()+"/config.json");
                    AlinLib.MINECRAFT.setScreen(build(parent));
                })));
        return screenBuilder.build();
    }
}
