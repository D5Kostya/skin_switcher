package ru.d5kostya.skinswitcher.client.mixin.gui;

import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.d5kostya.skinswitcher.client.skin_switcher;
import ru.d5kostya.skinswitcher.client.gui.components.PlayerButton;

@Mixin(value = TitleScreen.class, priority = Integer.MAX_VALUE)
public class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "init", at = @At("RETURN"))
    void createPauseMenu(CallbackInfo ci) {
        if(skin_switcher.config.getBoolean("MENU.TITLE", true)) {
            boolean isLeft = skin_switcher.config.getBoolean("MENU.TITLE.LEFT", false);
            int x =  isLeft ? width : 0;
            int y = 0;
            for (GuiEventListener widget : this.children) {
                if (widget instanceof Button && !(widget instanceof PlainTextButton)) {
                    x = isLeft ? Math.min(x, ((AbstractWidget) widget).getX() - 55) : Math.max(x, ((AbstractWidget) widget).getRight()+60);
                    y = Math.max(y, ((AbstractWidget) widget).getY());
                }
            }
            addRenderableWidget(new PlayerButton(x, y - 100, 60));
        }
    }
}
