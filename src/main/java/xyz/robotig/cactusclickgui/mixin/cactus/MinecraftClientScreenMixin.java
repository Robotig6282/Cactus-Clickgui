package xyz.robotig.cactusclickgui.mixin.cactus;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.robotig.cactusclickgui.module.impl.ClickGuiModule;
import xyz.robotig.cactusclickgui.ui.ClickGuiScreen;

@Mixin(Minecraft.class)
public abstract class MinecraftClientScreenMixin {
    @Unique
    private static final String CACTUS_CLICKGUI_MODULE_LIST_SCREEN = "com.dwarslooper.cactus.client.gui.screen.impl.ModuleListScreen";

    @Unique
    private boolean cactusClickgui$replacingCactusScreen;

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void cactusClickgui$replaceCactusModuleListScreen(Screen screen, CallbackInfo ci) {
        if (screen == null || cactusClickgui$replacingCactusScreen) {
            return;
        }
        if (!CACTUS_CLICKGUI_MODULE_LIST_SCREEN.equals(screen.getClass().getName())) {
            return;
        }
        if (!ClickGuiModule.isThemeEnabled()) {
            return;
        }

        cactusClickgui$replacingCactusScreen = true;
        try {
            ((Minecraft) (Object) this).setScreen(new ClickGuiScreen());
        } finally {
            cactusClickgui$replacingCactusScreen = false;
        }
        ci.cancel();
    }
}
