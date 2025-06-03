package ru.kelcuprum.clovskins.client.gui.screen.select;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.editbox.EditBoxBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.selector.SelectorBuilder;
import ru.kelcuprum.alinlib.gui.components.buttons.Button;
import ru.kelcuprum.alinlib.gui.components.editbox.EditBox;
import ru.kelcuprum.alinlib.gui.components.selector.SelectorButton;
import ru.kelcuprum.clovskins.client.ClovSkins;
import ru.kelcuprum.clovskins.client.api.SkinOption;
import ru.kelcuprum.clovskins.client.gui.cicada.DummyClientPlayerEntity;
import ru.kelcuprum.clovskins.client.gui.cicada.GuiEntityRenderer;

import java.util.Arrays;
import java.util.UUID;

import static ru.kelcuprum.alinlib.gui.Colors.BLACK_ALPHA;

public class EditSkinPreset extends Screen {
    public Screen parent;
    public SkinOption skinOption;
    public final SkinOption skinOptionOriginal;
    public String key;
    public boolean isSelected;
    public EditSkinPreset(Screen screen, SkinOption skinOption, String key, Boolean isSelected) {
        super(Component.translatable("clovskins.edit"));
        this.parent = screen;
        this.skinOption = skinOption;
        this.skinOptionOriginal = skinOption;
        this.key = key;
        this.isSelected = isSelected;
    }

    Button file;
    EditBox editBox;

    public static String[] skinTypes = new String[]{
            "nickname",
            "url",
            "file"
    };

    @Override
    protected void init() {
        int playerHeight = (int) ((height - 20 - font.lineHeight - 30) * 0.8);
        int pageSize = this.width-70-playerHeight/2;
        int componentSize =  Math.min(310, pageSize - 10);
        int x = ((pageSize - componentSize) / 2)+playerHeight/2+40;
        int buttonsHeight = 80+15;
        int y = height/2-buttonsHeight/2;

        //
        addRenderableWidget(new EditBoxBuilder(Component.translatable("clovskins.edit.name"), (s) -> skinOption.name = s).setValue(skinOption.name).setWidth(componentSize).setPosition(x, y).build());
        y+=25;
        editBox = (EditBox) addRenderableWidget(new EditBoxBuilder(Component.translatable("clovskins.edit.url"), (s) -> skinOption.setSkinTexture(skinOption.type, s)).setValue(skinOption.skin).setWidth(componentSize).setPosition(x, y).build());
        file = (Button) addRenderableWidget(new ButtonBuilder(Component.translatable("clovskins.edit.file_select"), (s) -> {}).setSprite(GuiUtils.getResourceLocation("clovskins", "texture/icons/file.png")).setWidth(20).setPosition(x+componentSize+5, y).build());
        y+=25;
        addRenderableWidget(new SelectorBuilder(Component.translatable("clovskins.edit.type")).setList(new String[] {
            Component.translatable("clovskins.edit.type.nickname").getString(),
                "URL",
                "File"
        }).setValue(Arrays.stream(skinTypes).toList().indexOf(skinOption.toJSON().get("type").getAsString())).setOnPress((s) -> {
            skinOption.setSkinTexture(s.getPosition() == 0 ? SkinOption.SkinType.NICKNAME : s.getPosition() == 1 ? SkinOption.SkinType.URL : SkinOption.SkinType.FILE, skinOption.skin);
        }).setWidth(componentSize/2-2).setPosition(x,y).build());
        addRenderableWidget(new SelectorBuilder(Component.translatable("clovskins.edit.model")).setList(new String[] {
                "Default",
                "Slim"
        }).setOnPress((s) -> skinOption.model = s.getPosition() == 0 ? PlayerSkin.Model.WIDE : PlayerSkin.Model.SLIM).setValue(skinOption.model == PlayerSkin.Model.WIDE ? 0 : 1).setWidth(componentSize/2-2).setPosition(x+componentSize/2+2, y).build());
        y+=25;
        addRenderableWidget(new ButtonBuilder(Component.translatable("clovskins.edit.select_cape"), (s) -> AlinLib.MINECRAFT.setScreen(new SelectCape(AlinLib.MINECRAFT.screen, skinOption, key))).setWidth(componentSize).setPosition(x, y).build());

        // --------------------
        // -------------------- -
        // --------- ----------
        // --------------------
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        int playerHeight = (int) ((height - 20 - font.lineHeight - 30) * 0.8);
        guiGraphics.fill(0, 0, width, height, BLACK_ALPHA);
        guiGraphics.fill(0, height/2-playerHeight/2-15, playerHeight/2+30, height/2+playerHeight/2+15, BLACK_ALPHA);
        guiGraphics.drawCenteredString(font, Component.translatable("clovskins.edit"), width/2, 15, -1);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 1900.0);
        renderPlayer(guiGraphics, 15, height/2-playerHeight/2, playerHeight/2);
        guiGraphics.pose().translate(0, 0, -1900.0);
        guiGraphics.pose().popPose();
    }

    public DummyClientPlayerEntity entity;
    public UUID SillyUUID = UUID.randomUUID();

    public void renderPlayer(GuiGraphics guiGraphics, int x, int y, int size){
        float rotation = 360F * ((float) (System.currentTimeMillis() % 10000) / 10000);
        try {
            if(entity == null) entity = new DummyClientPlayerEntity(null, SillyUUID, skinOption.getPlayerSkin(), AlinLib.MINECRAFT.options, false);
            else entity.setSkin(skinOption.getPlayerSkin());
            guiGraphics.pose().pushPose();
            GuiEntityRenderer.drawEntity(
                    guiGraphics.pose(), x + (size / 2), y+size*2,
                    size, rotation, 0, 0, entity
            );
            guiGraphics.pose().popPose();
        } catch (Exception ignored){}
    }

    @Override
    public void tick() {
        super.tick();
        file.active = skinOption.type == SkinOption.SkinType.FILE;
    }

    @Override
    public void onClose() {
        if(minecraft == null) return;
        try {
            skinOption.uploadToMojangAPI();
            skinOption.save();
            if(ClovSkins.currentSkin == skinOptionOriginal) {
                ClovSkins.currentSkin = skinOption;
                if(!skinOption.equals(skinOptionOriginal)) skinOption.uploadToMojangAPI();
            }
            ClovSkins.skinOptions.put(key, skinOption);
        } catch (Exception exception){
            exception.printStackTrace();
        }
        minecraft.setScreen(parent);
    }
}
