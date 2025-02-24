package azula.blockcounter.util;

import azula.blockcounter.config.ChatColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public class Random {

    public static String formatVec3d(Vec3d vector, String format) {
        return String.format(format, vector.x) + ", "
                + String.format(format, vector.y) + ", "
                + String.format(format, vector.z);
    }

    public static Formatting chatColorToFormat(ChatColor color) {

        return switch (color) {
            case BLACK -> Formatting.BLACK;
            case DARK_BLUE -> Formatting.DARK_BLUE;
            case DARK_GREEN -> Formatting.DARK_GREEN;
            case DARK_AQUA -> Formatting.DARK_AQUA;
            case DARK_RED -> Formatting.DARK_RED;
            case DARK_PURPLE -> Formatting.DARK_PURPLE;
            case DARK_GRAY -> Formatting.DARK_GRAY;
            case GOLD -> Formatting.GOLD;
            case BLUE -> Formatting.BLUE;
            case GREEN -> Formatting.GREEN;
            case AQUA -> Formatting.AQUA;
            case RED -> Formatting.RED;
            case PURPLE -> Formatting.LIGHT_PURPLE;
            case GRAY -> Formatting.GRAY;
            case YELLOW -> Formatting.YELLOW;
            case WHITE -> Formatting.WHITE;
        };

    }

    // This is a terrible way of doing this I'm pretty sure
    public static void lockMovement() {
        MinecraftClient client = MinecraftClient.getInstance();

        client.options.forwardKey.setPressed(false);
        client.options.backKey.setPressed(false);
        client.options.leftKey.setPressed(false);
        client.options.rightKey.setPressed(false);
        client.options.jumpKey.setPressed(false);
        client.options.sprintKey.setPressed(false);
        client.options.sneakKey.setPressed(false);

    }
}
