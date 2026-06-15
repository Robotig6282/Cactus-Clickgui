package xyz.robotig.cactusclickgui.ui;

import com.dwarslooper.cactus.client.feature.module.Category;
import com.dwarslooper.cactus.client.feature.module.Module;
import com.dwarslooper.cactus.client.feature.module.ModuleManager;
import com.dwarslooper.cactus.client.gui.screen.impl.ModuleOptionsScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import xyz.robotig.cactusclickgui.Config;
import xyz.robotig.cactusclickgui.ui.components.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ClickGuiScreen extends Screen {
    private static final String ALL_CATEGORY_ID = "all";
    private static final String FAVORITES_CATEGORY_ID = "favorites";

    private final List<Window> windows = new ArrayList<>();
    private EditBox searchWidget;
    private String searchQuery = "";

    public ClickGuiScreen() {
        super(Component.literal("Cactus-Clickgui"));
    }

    @Override
    protected void init() {
        super.init();
        int searchWidth = Math.min(220, Math.max(120, width - 20));
        searchWidget = new EditBox(font, 10, 10, searchWidth, 16, Component.literal("Search"));
        searchWidget.setMaxLength(64);
        searchWidget.setHint(Component.literal("Search modules..."));
        searchWidget.setValue(searchQuery);
        searchWidget.setResponder(value -> {
            searchQuery = value;
            rebuildWindows();
        });
        addRenderableWidget(searchWidget);
        setInitialFocus(searchWidget);
        rebuildWindows();
    }

    private void rebuildWindows() {
        windows.clear();
        ModuleManager manager = ModuleManager.get();
        if (manager == null) {
            return;
        }

        final int margin = 10;
        final int horizontalGap = 8;
        final int verticalGap = 8;
        int nextX = margin;
        int searchBottom = searchWidget == null ? margin : searchWidget.getY() + searchWidget.getHeight();
        int nextY = searchBottom + 8;
        int rowMaxHeight = 0;

        for (Category category : manager.getCategories()) {
            if (ALL_CATEGORY_ID.equals(category.getId())) {
                continue;
            }

            List<Module> modules = filterModules(getModules(category));
            if (!modules.isEmpty()) {
                Window window = new Window(category, nextX, nextY, modules);
                int windowWidth = window.getWidth();
                int windowHeight = window.getHeight();

                if (nextX > margin && nextX + windowWidth > width - margin) {
                    nextX = margin;
                    nextY += rowMaxHeight + verticalGap;
                    rowMaxHeight = 0;
                    window.setPosition(nextX, nextY);
                    windowHeight = window.getHeight();
                }

                int[] savedPos = Config.getWindowPos(category.getId());
                if (savedPos != null) {
                    window.setPosition(savedPos[0], savedPos[1]);
                }
                windows.add(window);
                nextX += windowWidth + horizontalGap;
                rowMaxHeight = Math.max(rowMaxHeight, windowHeight);
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        extractTransparentBackground(context);
        for (Window window : windows) {
            window.render(context, mouseX, mouseY);
        }
        if (searchWidget != null) {
            searchWidget.extractRenderState(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent keyInput) {
        if (searchWidget != null) {
            if ((keyInput.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0 && keyInput.key() == GLFW.GLFW_KEY_F) {
                searchWidget.setFocused(true);
                return true;
            }
            if (searchWidget.isFocused() && keyInput.key() == GLFW.GLFW_KEY_ESCAPE) {
                searchWidget.setFocused(false);
                return true;
            }
        }
        return super.keyPressed(keyInput);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean bool) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        for (int i = windows.size() - 1; i >= 0; i--) {
            Window window = windows.get(i);
            Module clicked = window.mouseClicked(mouseX, mouseY, button);
            if (clicked != null) {
                windows.remove(i);
                windows.add(window);
                if (button == 2) {
                    rebuildWindows();
                    return true;
                }
                if (button == 1) {
                    minecraft.setScreen(new ModuleOptionsScreen(clicked, this));
                }
                return true;
            }
            if (mouseX >= window.getX() && mouseX <= window.getX() + window.getWidth() && mouseY >= window.getY() && mouseY <= window.getY() + 15) {
                windows.remove(i);
                windows.add(window);
                return true;
            }
        }
        return super.mouseClicked(click, bool);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        for (Window window : windows) {
            window.mouseReleased(click.x(), click.y(), click.button());
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double dx, double dy) {
        for (Window window : windows) {
            window.mouseDragged((int) click.x(), (int) click.y());
        }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private List<Module> filterModules(List<Module> modules) {
        String query = searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            return modules;
        }
        return modules.stream()
                .filter(module -> UiNameUtil.moduleName(module.getDisplayName()).toLowerCase(Locale.ROOT).contains(query))
                .collect(Collectors.toList());
    }

    private List<Module> getModules(Category category) {
        ModuleManager manager = ModuleManager.get();
        if (manager == null) {
            return List.of();
        }

        boolean favorites = FAVORITES_CATEGORY_ID.equals(category.getId());
        return manager.getModules().values().stream()
                .filter(category::contains)
                .filter(module -> !favorites || module.isFavorite())
                .collect(Collectors.toList());
    }
}
