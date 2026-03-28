package azula.blockcounter.util;

import azula.blockcounter.config.ChatColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class Random {

    public static String formatVec3(Vec3 vector, String format) {
        return String.format(format, vector.x) + ", "
                + String.format(format, vector.y) + ", "
                + String.format(format, vector.z);
    }

    public static ChatFormatting chatColorToFormat(ChatColor color) {

        return switch (color) {
            case BLACK -> ChatFormatting.BLACK;
            case DARK_BLUE -> ChatFormatting.DARK_BLUE;
            case DARK_GREEN -> ChatFormatting.DARK_GREEN;
            case DARK_AQUA -> ChatFormatting.DARK_AQUA;
            case DARK_RED -> ChatFormatting.DARK_RED;
            case DARK_PURPLE -> ChatFormatting.DARK_PURPLE;
            case DARK_GRAY -> ChatFormatting.DARK_GRAY;
            case GOLD -> ChatFormatting.GOLD;
            case BLUE -> ChatFormatting.BLUE;
            case GREEN -> ChatFormatting.GREEN;
            case AQUA -> ChatFormatting.AQUA;
            case RED -> ChatFormatting.RED;
            case PURPLE -> ChatFormatting.LIGHT_PURPLE;
            case GRAY -> ChatFormatting.GRAY;
            case YELLOW -> ChatFormatting.YELLOW;
            case WHITE -> ChatFormatting.WHITE;
        };

    }

    public static Vec3i toIntVec(Vec3 vec) {
        return new Vec3i((int) Math.floor(vec.x), (int) Math.floor(vec.y), (int) Math.floor(vec.z));
    }
}
