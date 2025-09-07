package ru.d5kostya.skinswitcher.client.gui.screen.select;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.editbox.EditBoxBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.selector.SelectorBuilder;
import ru.kelcuprum.alinlib.gui.components.buttons.Button;
import ru.kelcuprum.alinlib.gui.components.editbox.EditBox;
import ru.kelcuprum.alinlib.gui.components.selector.SelectorButton;
import ru.kelcuprum.alinlib.gui.screens.ConfirmScreen;
import ru.kelcuprum.alinlib.gui.toast.ToastBuilder;
import ru.d5kostya.skinswitcher.client.skin_switcher;
import ru.d5kostya.skinswitcher.client.api.SkinOption;
import ru.d5kostya.skinswitcher.client.gui.cicada.GuiEntityRenderer;

import java.io.File;
import java.util.Arrays;

import static com.mojang.blaze3d.Blaze3D.getTime;
import static ru.kelcuprum.alinlib.gui.Colors.BLACK_ALPHA;
import static ru.kelcuprum.alinlib.gui.Icons.DONT;
import static ru.d5kostya.skinswitcher.client.skin_switcher.getPath;

public class EditSkinPreset extends Screen {
    public Screen parent;
    public SkinOption skinOption;
    public final SkinOption skinOptionOriginal;
    public String key;
    public boolean isSelected;
    public boolean isDeleted = false;
    public EditSkinPreset(Screen screen, SkinOption skinOption, String key, Boolean isSelected) {
        super(Component.translatable("skin_switcher.edit"));
        this.parent = screen;
        this.skinOption = skinOption;
        this.skinOptionOriginal = skinOption;
        this.key = key;
        this.isSelected = isSelected;
    }

    Button file;
    EditBox editBox;
    SelectorButton selectorType;
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
        y+=25;
        editBox = (EditBox) addRenderableWidget(new EditBoxBuilder(Component.translatable("skin_switcher.edit.url"), (s) -> skinOption.setSkinTexture(skinOption.type, s)).setValue(skinOption.skin).setWidth(componentSize).setPosition(x, y).build());
        file = (Button) addRenderableWidget(new ButtonBuilder(Component.translatable("skin_switcher.edit.file_select"), (s) -> {
            openTrackEditor();
        }).setSprite(GuiUtils.getResourceLocation("skin_switcher", "texture/icons/file.png")).setWidth(20).setPosition(x+componentSize+5, y).build());
        y+=25;
        selectorType = (SelectorButton) addRenderableWidget(new SelectorBuilder(Component.translatable("skin_switcher.edit.type")).setList(new String[] {
            Component.translatable("skin_switcher.edit.type.nickname").getString(),
                "URL",
                "File"
        }).setValue(Arrays.stream(skinTypes).toList().indexOf(skinOption.toJSON().get("type").getAsString())).setOnPress((s) -> {
            skinOption.setSkinTexture(s.getPosition() == 0 ? SkinOption.SkinType.NICKNAME : s.getPosition() == 1 ? SkinOption.SkinType.URL : SkinOption.SkinType.FILE, skinOption.skin);
        }).setWidth(componentSize/2-2).setPosition(x,y).build());
        addRenderableWidget(new SelectorBuilder(Component.translatable("skin_switcher.edit.model")).setList(new String[] {
                "Default",
                "Slim"
        }).setOnPress((s) -> skinOption.model = s.getPosition() == 0 ? PlayerSkin.Model.WIDE : PlayerSkin.Model.SLIM).setValue(skinOption.model == PlayerSkin.Model.WIDE ? 0 : 1).setWidth(componentSize/2-2).setPosition(x+componentSize/2+2, y).build());
        addRenderableWidget(new ButtonBuilder(Component.translatable("skin_switcher.edit.remove"), (s) -> {
            AlinLib.MINECRAFT.setScreen(new ConfirmScreen(parent, Component.translatable("skin_switcher.edit.remove.title"), Component.translatable("skin_switcher.edit.remove.description"), (b) -> {
                if(b){
                    this.isDeleted = true;
                    onClose();
                }
            }));
        }).setSprite(DONT).setWidth(20).setPosition(x+componentSize+5, y).build());
        y+=25;
        addRenderableWidget(new ButtonBuilder(Component.translatable("skin_switcher.edit.select_cape"), (s) -> AlinLib.MINECRAFT.setScreen(new SelectCape(AlinLib.MINECRAFT.screen, skinOption, key))).setWidth(componentSize).setPosition(x, y).build());
    }

    public void openTrackEditor(){
        MemoryStack stack = MemoryStack.stackPush();
        PointerBuffer filters = stack.mallocPointer(1);
        filters.put(stack.UTF8("*.png"));

        filters.flip();
        File defaultPath = new File(getPath()).getAbsoluteFile();
        String defaultString = defaultPath.getAbsolutePath();
        if(defaultPath.isDirectory() && !defaultString.endsWith(File.separator)){
            defaultString += File.separator;
        }

        String result = TinyFileDialogs.tinyfd_openFileDialog(Component.translatable("skin_switcher.edit.file_selector").getString(), defaultString, filters, Component.translatable("skin_switcher.edit.file_selector.description").getString(), false);
        if(result == null) return;
        File file = new File(result);
        if(file.exists()) {
            editBox.setValue(file.getAbsolutePath());
            skinOption.type = SkinOption.SkinType.FILE;
            selectorType.setPosition(2);
        }
        else new ToastBuilder().setIcon(DONT).setType(ToastBuilder.Type.ERROR).setTitle(Component.literal("skin_switcher")).setMessage(Component.translatable("skin_switcher.edit.file_not_exist")).buildAndShow();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        int playerHeight = (int) ((height - 20 - font.lineHeight - 30) * 0.8);
        guiGraphics.fill(0, 0, width, height, BLACK_ALPHA);
        guiGraphics.fill(0, height/2-playerHeight/2-15, playerHeight/2+30, height/2+playerHeight/2+15, BLACK_ALPHA);
        guiGraphics.drawCenteredString(font, Component.translatable("skin_switcher.edit"), width/2, 15, -1);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 1900.0);
        renderPlayer(guiGraphics, 15, height/2-playerHeight/2, playerHeight/2, f);
        guiGraphics.pose().translate(0, 0, -1900.0);
        guiGraphics.pose().popPose();
    }

    public double currentTime = getTime();
    public void renderPlayer(GuiGraphics guiGraphics, int x, int y, int size, float tick){
        float rotation = (float) ((getTime() - currentTime) * 35.0f);
        try {
            guiGraphics.pose().pushPose();
            GuiEntityRenderer.drawModel(
                    guiGraphics.pose(), x + (size / 2), y+size*2+15,
                    size, rotation, 0, 0, skinOption, tick
            );
            guiGraphics.pose().popPose();
        } catch (Exception ignored){}
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void onClose() {
        try {
            if(isDeleted){
                if(skin_switcher.currentSkin == skinOptionOriginal) {
                    skin_switcher.currentSkin = skin_switcher.skinOptions.getOrDefault("default", skin_switcher.safeSkinOption);
                    skin_switcher.config.setString("SELECTED", "new skin");
                    skin_switcher.currentSkin.uploadToMojangAPI();
                }
                skinOption.delete();
                skin_switcher.skinOptions.remove(key);
                AlinLib.MINECRAFT.setScreen(parent);
            } else {
                skinOption.save();
                if(skin_switcher.currentSkin == skinOptionOriginal) {
                    if(!skinOption.equals(skinOptionOriginal)) skinOption.uploadToMojangAPI();
                    skin_switcher.currentSkin = skinOption;
                }
                skin_switcher.skinOptions.put(key, skinOption);
            }
        } catch (Exception exception){
            exception.printStackTrace();
        }
        
        AlinLib.MINECRAFT.setScreen(parent);
    }
}
