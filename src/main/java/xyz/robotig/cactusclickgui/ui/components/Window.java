package xyz.robotig.cactusclickgui.ui.components;

import com.dwarslooper.cactus.client.feature.module.Category;
import com.dwarslooper.cactus.client.feature.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import xyz.robotig.cactusclickgui.Config;
import xyz.robotig.cactusclickgui.module.impl.ClickGuiModule;
import xyz.robotig.cactusclickgui.ui.UiNameUtil;

import java.util.List;

public class Window {
    private static final int MIN_WIDTH = 90;
    private static final int DRAG_THRESHOLD = 2;
    private final Category category;
    private int x, y;
    private int width = MIN_WIDTH;
    private boolean dragging;
    private int dragX, dragY;
    private boolean headerClicked;
    private boolean collapsed;
    private final List<Module> modules;

    public Window(Category category, int x, int y, List<Module> modules) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.modules = modules;
        this.width = calculateWidth();
        this.collapsed = Config.isWindowCollapsed(category.getId());
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        clampToScreen();
    }

    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        Minecraft client = Minecraft.getInstance();
        width = calculateWidth();
        clampToScreen();
        int height = getHeight();

        context.fill(x, y, x + width, y + 15, 0xCC303030);
        String indicator = collapsed ? "[+]" : "[-]";
        context.text(client.font, indicator, x + width - client.font.width(indicator) - 4, y + 3, 0xFF888888, true);
        context.text(client.font, categoryName(), x + 4, y + 3, 0xFFFFFFFF, true);

        if (collapsed) return;

        context.fill(x, y + 15, x + width, y + height, 0xCC101010);

        int buttonY = y + 16;
        for (Module module : modules) {
            ModuleButton button = new ModuleButton(module);
            button.render(context, x, buttonY, width, mouseX, mouseY);
            buttonY += 14;
        }
    }

    public Module mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= x && mouseX <= x + width) {
            if (mouseY >= y && mouseY <= y + 15) {
                if (button == 0) {
                    headerClicked = true;
                    dragX = (int) mouseX - x;
                    dragY = (int) mouseY - y;
                }
                return null;
            }

            if (collapsed) return null;

            int buttonY = y + 16;
            for (Module module : modules) {
                if (mouseY >= buttonY && mouseY <= buttonY + 14) {
                    if (button == 0) {
                        module.toggle();
                    } else if (button == 2) {
                        module.setFavorite(!module.isFavorite());
                    }
                    return module;
                }
                buttonY += 14;
            }
        }
        return null;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (headerClicked && !dragging) {
                collapsed = !collapsed;
                Config.setWindowCollapsed(category.getId(), collapsed);
            }
            dragging = false;
            headerClicked = false;
        }
    }

    public void mouseDragged(int mouseX, int mouseY) {
        if (headerClicked && !dragging) {
            int dx = mouseX - (x + dragX);
            int dy = mouseY - (y + dragY);
            if (Math.abs(dx) > DRAG_THRESHOLD || Math.abs(dy) > DRAG_THRESHOLD) {
                dragging = true;
            }
        }
        if (dragging) {
            x = mouseX - dragX;
            y = mouseY - dragY;
            clampToScreen();
            Config.setWindowPos(category.getId(), x, y);
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }

    public int getHeight() {
        if (collapsed) return 15;
        return 16 + modules.size() * 14 + 1;
    }

    private int calculateWidth() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.font == null) {
            return MIN_WIDTH;
        }

        int maxTextWidth = client.font.width(categoryName());
        for (Module module : modules) {
            String moduleName = UiNameUtil.moduleName(module.getDisplayName());
            maxTextWidth = Math.max(maxTextWidth, client.font.width(moduleName));
        }

        int computed = Math.max(MIN_WIDTH, maxTextWidth + 10);
        int screenWidth = client.getWindow().getGuiScaledWidth();
        if (screenWidth > 0) {
            computed = Math.min(computed, Math.max(MIN_WIDTH, screenWidth - 4));
        }
        return computed;
    }

    private void clampToScreen() {
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return;
        }
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int maxX = Math.max(0, screenWidth - width);
        int maxY = Math.max(0, screenHeight - getHeight());
        x = Math.max(0, Math.min(x, maxX));
        y = Math.max(0, Math.min(y, maxY));
    }

    private String categoryName() {
        String name = category.getName();
        if ("favorites".equals(category.getId()) && name.equals(category.getId())) {
            return "Favorites";
        }
        return UiNameUtil.moduleName(name);
    }
}
