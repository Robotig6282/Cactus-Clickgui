package xyz.robotig.cactusclickgui.ui;

import net.minecraft.client.resources.language.I18n;

public final class UiNameUtil {
    private static final String MODULE_PREFIX = "modules.";
    private static final String MODULE_SUFFIX = ".name";

    private UiNameUtil() {
    }

    public static String moduleName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return "";
        }

        String stripped = stripFormatting(rawName);
        String translated = translate(stripped);
        if (!translated.isBlank()) {
            return translated;
        }

        String fromModuleKey = extractModuleKeyInner(stripped);
        if (!fromModuleKey.isBlank()) {
            return normalizeDisplayText(fromModuleKey);
        }

        String moduleId = extractModuleId(stripped);
        if (!moduleId.isBlank()) {
            String translatedById = translate(MODULE_PREFIX + moduleId + MODULE_SUFFIX);
            if (!translatedById.isBlank()) {
                return translatedById;
            }
            return humanize(moduleId);
        }

        return normalizeDisplayText(stripped);
    }

    private static String translate(String key) {
        String translated = I18n.get(key);
        return translated.equals(key) ? "" : translated;
    }

    private static String extractModuleId(String rawName) {
        String candidate = rawName;

        int namespaceSeparator = candidate.indexOf(':');
        if (namespaceSeparator >= 0 && namespaceSeparator < candidate.length() - 1) {
            candidate = candidate.substring(namespaceSeparator + 1);
        }

        if (!looksLikeMachineId(candidate)) {
            return "";
        }

        return candidate;
    }

    private static boolean looksLikeMachineId(String text) {
        if (text.isBlank()) {
            return false;
        }
        if (text.indexOf(' ') >= 0) {
            return false;
        }
        if (text.matches("[a-z0-9_\\-.]+")) {
            return true;
        }
        return text.matches(".*[a-z].*") && text.matches(".*[A-Z].*");
    }

    private static String extractModuleKeyInner(String text) {
        if (!text.startsWith(MODULE_PREFIX) || !text.endsWith(MODULE_SUFFIX)) {
            return "";
        }
        if (text.length() <= MODULE_PREFIX.length() + MODULE_SUFFIX.length()) {
            return "";
        }
        return text.substring(MODULE_PREFIX.length(), text.length() - MODULE_SUFFIX.length()).trim();
    }

    private static String normalizeDisplayText(String text) {
        if (text.isBlank()) {
            return text;
        }
        String normalized = text.replace('_', ' ').replace('-', ' ').replace('.', ' ');
        normalized = normalized.replaceAll("([a-z])([A-Z])", "$1 $2");
        normalized = normalized.replaceAll("([A-Z])([A-Z][a-z])", "$1 $2");
        normalized = normalized.trim().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            return text;
        }
        if (normalized.equals(normalized.toLowerCase())) {
            return toTitleCase(normalized);
        }
        return normalized;
    }

    private static String stripFormatting(String text) {
        return text.replaceAll("(?i)§[0-9A-FK-OR]", "");
    }

    private static String humanize(String id) {
        String normalized = id.replace('_', ' ').replace('-', ' ').replace('.', ' ');
        normalized = normalized.replaceAll("([a-z])([A-Z])", "$1 $2");
        normalized = normalized.replaceAll("([A-Z])([A-Z][a-z])", "$1 $2");
        String[] parts = normalized.trim().split("\\s+");

        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (!out.isEmpty()) {
                out.append(' ');
            }
            if (part.length() == 1) {
                out.append(part.toUpperCase());
            } else {
                out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return out.toString();
    }

    private static String toTitleCase(String text) {
        String[] parts = text.split("\\s+");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (!out.isEmpty()) {
                out.append(' ');
            }
            if (part.length() == 1) {
                out.append(part.toUpperCase());
            } else {
                out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return out.toString();
    }
}
