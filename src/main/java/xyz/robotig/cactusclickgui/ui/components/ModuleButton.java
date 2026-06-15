package xyz.robotig.cactusclickgui.ui.components;

import com.dwarslooper.cactus.client.feature.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import xyz.robotig.cactusclickgui.module.impl.ClickGuiModule;
import xyz.robotig.cactusclickgui.ui.UiNameUtil;

public class ModuleButton {
    private final Module module;

    public ModuleButton(Module module) {
        this.module = module;
    }

    public void render(GuiGraphicsExtractor context, int x, int y, int width, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 14;

        int bgColor = module.active() ? 0x80282828 : 0x80181818;
        if (hovered) {
            bgColor = module.active() ? 0x80353535 : 0x80252525;
        }

        context.fill(x, y, x + width, y + 14, bgColor);

        if (module.active()) {
            context.fill(x, y, x + 2, y + 14, ClickGuiModule.getAccentColor());
        }

        Minecraft client = Minecraft.getInstance();
        String label = UiNameUtil.moduleName(module.getDisplayName());
        context.text(client.font, label, x + 5, y + 3, 0xFFFFFFFF, true);
        if (module.isFavorite()) {
            int starX = x + 5 + client.font.width(label);
            int maxStarX = x + width - client.font.width(" *") - 2;
            if (starX <= maxStarX) {
                context.text(client.font, " *", starX, y + 3, 0xFFFFD75A, true);
            }
        }
    }
}
