package ru.d5kostya.skinswitcher.client.gui.screen.select;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.editbox.EditBoxBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.text.TextBuilder;
import ru.kelcuprum.alinlib.gui.toast.ToastBuilder;
import ru.d5kostya.skinswitcher.client.skin_switcher;
import ru.d5kostya.skinswitcher.client.api.SkinOption;

import java.util.Random;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static ru.kelcuprum.alinlib.gui.Colors.BLACK_ALPHA;
import static ru.d5kostya.skinswitcher.client.skin_switcher.getPath;

public class CreateSkin extends Screen {
    public final Screen parent;
    public CreateSkin(Screen parent) {
        super(Component.translatable("skin_switcher.create"));
        this.parent = parent;
    }

    public String name = "";

    @Override
    protected void init() {
        create();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        guiGraphics.drawCenteredString(font, title, width/2, 15, -1);
    }

    public void create(){
        Random r = new Random();
        int n = r.nextInt();
        String Hexadecimal = Integer.toHexString(n);

        File file = new File(getPath()+"/skins/"+Hexadecimal.toString()+".json");
        try {
            Files.writeString(file.toPath(), skin_switcher.safeSkinOption.toString(), StandardCharsets.UTF_8);
            SkinOption skinOption = SkinOption.getSkinOption(file);
            skin_switcher.skinOptions.put(name, skinOption);
            AlinLib.MINECRAFT.setScreen(new EditSkinPreset(parent, skinOption, name, false));
        } catch (Exception ex){
            skin_switcher.logger.log("file already exists");
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        guiGraphics.fill(0,0,width,height, BLACK_ALPHA);
    }

    @Override
    public void onClose() {
        if(minecraft == null) return;
        minecraft.setScreen(parent);
    }
}
