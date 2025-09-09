package ru.d5kostya.skinswitcher.client.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.d5kostya.skinswitcher.client.skin_switcher;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "runTick", at = @At("HEAD"))
    public void render(boolean tick, CallbackInfo ci) {
        skin_switcher.TICKS += Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
    }
}
