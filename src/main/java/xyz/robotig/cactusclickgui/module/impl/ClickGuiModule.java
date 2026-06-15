package xyz.robotig.cactusclickgui.module.impl;

import com.dwarslooper.cactus.client.feature.module.Module;
import com.dwarslooper.cactus.client.feature.module.ModuleManager;
import com.dwarslooper.cactus.client.gui.screen.impl.ModuleListScreen;
import com.dwarslooper.cactus.client.systems.config.settings.impl.ColorSetting;
import com.dwarslooper.cactus.client.systems.config.settings.impl.ColorSetting.ColorValue;
import com.dwarslooper.cactus.client.systems.key.KeyBind;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import xyz.robotig.cactusclickgui.ui.ClickGuiScreen;

import java.awt.Color;

public class ClickGuiModule extends Module {
    public static final String ID = "cactusclickgui";
    public static final String NAME = "Clickgui";
    private static final int DEFAULT_ACCENT_COLOR = 0xFFF8AEFF;

    private final ColorSetting accentColor;
    private boolean restoringFromConfig;

    public ClickGuiModule() {
        super(ID, ModuleManager.CATEGORY_UTILITY, new Options().set(Flag.HUD_LISTED, false).set(Flag.RUN_IN_MENU, true));
        setBind(KeyBind.of(GLFW.GLFW_KEY_RIGHT_SHIFT));
        accentColor = new ColorSetting("accentColor", new ColorValue(new Color(DEFAULT_ACCENT_COLOR, true), false), false);
        mainGroup.add(accentColor);
    }

    public static int getAccentColor() {
        ClickGuiModule module = getRegistered();
        if (module != null) {
            return module.getAccentColorValue();
        }
        return DEFAULT_ACCENT_COLOR;
    }

    public static boolean isThemeEnabled() {
        ClickGuiModule module = getRegistered();
        return module != null && module.active();
    }

    @Override
    public String getDisplayName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Uses the Cactus-Clickgui instead of the default Cactus module screen.";
    }

    @Override
    public boolean sendsToggleFeedback() {
        return false;
    }

    @Override
    public void onEnable() {
        if (restoringFromConfig) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client != null && !(client.screen instanceof ClickGuiScreen)) {
            client.setScreen(new ClickGuiScreen());
        }
    }

    @Override
    public void onDisable() {
        if (restoringFromConfig) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client != null && client.screen instanceof ClickGuiScreen) {
            client.setScreen(new ModuleListScreen());
        }
    }

    @Override
    public ClickGuiModule fromJson(JsonObject jsonObject) {
        restoringFromConfig = true;
        try {
            super.fromJson(jsonObject);
            return this;
        } finally {
            restoringFromConfig = false;
        }
    }

    private int getAccentColorValue() {
        return accentColor.get().color();
    }

    private static ClickGuiModule getRegistered() {
        try {
            ModuleManager manager = ModuleManager.get();
            return manager == null ? null : manager.get(ClickGuiModule.class);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
