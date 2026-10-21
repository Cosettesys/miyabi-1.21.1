package net.cosette.miyabi.util;

import net.minecraft.ChatFormatting;
import net.cosette.miyabi.progression.MagicRank;
import net.cosette.miyabi.progression.SwordStyle;
import net.cosette.miyabi.skills.magic.MagicSchool;

import java.util.List;

/**
 * Aides pour colorer quelques mots importants dans les messages du chat (codes de formatage Minecraft).
 * Exemple : "Je sens mon corps " + Chat.aura("plus fort") + " maintenant."
 */
public final class Chat {
    private Chat() {}
    public static String c(ChatFormatting color, Object text) {
        return color + String.valueOf(text) + ChatFormatting.RESET;
    }
    // --- Sujets ---
    public static String title(Object text) { return ChatFormatting.GOLD.toString() + ChatFormatting.BOLD + text + ChatFormatting.RESET; }
    public static String label(Object text) { return c(ChatFormatting.YELLOW, text); }
    public static String mana(Object text) { return c(ChatFormatting.AQUA, text); }
    public static String aura(Object text) { return c(ChatFormatting.GOLD, text); }
    public static String good(Object text) { return c(ChatFormatting.GREEN, text); }
    public static String bad(Object text) { return c(ChatFormatting.RED, text); }
    public static String dim(Object text) { return c(ChatFormatting.GRAY, text); }
    public static String bool(boolean value) { return value ? good(true) : bad(false); }
    // --- Écoles, styles, rangs ---
    public static String school(MagicSchool school) {
        ChatFormatting color = switch (school) {
            case FIRE -> ChatFormatting.RED;
            case WATER -> ChatFormatting.AQUA;
            case EARTH -> ChatFormatting.DARK_GREEN;
            case WIND -> ChatFormatting.WHITE;
            case GRAVITY -> ChatFormatting.DARK_PURPLE;
            case SUMMONING -> ChatFormatting.LIGHT_PURPLE;
            case BARRIER -> ChatFormatting.BLUE;
            case HEALING -> ChatFormatting.GREEN;
        };
        return c(color, school.name());
    }
    /** Une école, ou deux séparées par + pour un sort hybride. */
    public static String schools(List<MagicSchool> schools) {
        StringBuilder out = new StringBuilder();
        for (MagicSchool school : schools) {
            if (out.length() > 0) out.append(dim(" + "));
            out.append(school(school));
        }
        return out.toString();
    }
    public static String style(SwordStyle style) {
        ChatFormatting color = switch (style) {
            case SWORD_GOD -> ChatFormatting.RED;
            case NORTH_GOD -> ChatFormatting.DARK_AQUA;
            case WATER_GOD -> ChatFormatting.BLUE;
        };
        return c(color, style.name());
    }
    public static String rank(MagicRank rank) {
        ChatFormatting color = switch (rank) {
            case BEGINNER -> ChatFormatting.GRAY;
            case INTERMEDIATE -> ChatFormatting.GREEN;
            case ADVANCED -> ChatFormatting.AQUA;
            case SAINT -> ChatFormatting.GOLD;
            case KING -> ChatFormatting.LIGHT_PURPLE;
            case EMPEROR -> ChatFormatting.RED;
            case GOD -> ChatFormatting.YELLOW;
        };
        return rank == MagicRank.GOD ? color.toString() + ChatFormatting.BOLD + rank.name() + ChatFormatting.RESET : c(color, rank.name());
    }
}