package xyz.robotig.cactusclickgui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path clickGuiPath;
    private static JsonObject clickGuiData = new JsonObject();

    public static void init() {
        Path dir = Minecraft.getInstance().gameDirectory.toPath().resolve("cactus-clickgui");
        clickGuiPath = dir.resolve("clickgui.json");
        loadClickGui();
    }

    private static void loadClickGui() {
        try {
            if (Files.exists(clickGuiPath)) {
                clickGuiData = GSON.fromJson(Files.readString(clickGuiPath), JsonObject.class);
            }
        } catch (Exception e) {
            clickGuiData = new JsonObject();
        }
    }

    private static void saveClickGui() {
        try {
            Files.createDirectories(clickGuiPath.getParent());
            Files.writeString(clickGuiPath, GSON.toJson(clickGuiData));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int[] getWindowPos(String category) {
        try {
            if (clickGuiData.has(category)) {
                JsonObject pos = clickGuiData.getAsJsonObject(category);
                return new int[]{pos.get("x").getAsInt(), pos.get("y").getAsInt()};
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static void setWindowPos(String category, int x, int y) {
        JsonObject obj = clickGuiData.has(category) ? clickGuiData.getAsJsonObject(category) : new JsonObject();
        obj.addProperty("x", x);
        obj.addProperty("y", y);
        clickGuiData.add(category, obj);
        saveClickGui();
    }

    public static boolean isWindowCollapsed(String category) {
        try {
            if (clickGuiData.has(category)) {
                JsonObject obj = clickGuiData.getAsJsonObject(category);
                if (obj.has("collapsed")) {
                    return obj.get("collapsed").getAsBoolean();
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static void setWindowCollapsed(String category, boolean collapsed) {
        JsonObject obj = clickGuiData.has(category) ? clickGuiData.getAsJsonObject(category) : new JsonObject();
        obj.addProperty("collapsed", collapsed);
        clickGuiData.add(category, obj);
        saveClickGui();
    }
}
