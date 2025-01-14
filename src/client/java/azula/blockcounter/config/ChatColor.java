package azula.blockcounter.config;

public enum ChatColor {
    BLACK("text.autoconfig.blockcounter.color.black"),
    DARK_BLUE("text.autoconfig.blockcounter.color.dark_blue"),
    DARK_GREEN("text.autoconfig.blockcounter.color.dark_green"),
    DARK_AQUA("text.autoconfig.blockcounter.color.dark_aqua"),
    DARK_RED("text.autoconfig.blockcounter.color.dark_red"),
    DARK_PURPLE("text.autoconfig.blockcounter.color.dark_purple"),
    DARK_GRAY("text.autoconfig.blockcounter.color.dark_gray"),
    GOLD("text.autoconfig.blockcounter.color.gold"),
    BLUE("text.autoconfig.blockcounter.color.blue"),
    GREEN("text.autoconfig.blockcounter.color.green"),
    AQUA("text.autoconfig.blockcounter.color.aqua"),
    RED("text.autoconfig.blockcounter.color.red"),
    PURPLE("text.autoconfig.blockcounter.color.purple"),
    GRAY("text.autoconfig.blockcounter.color.gray"),
    YELLOW("text.autoconfig.blockcounter.color.yellow"),
    WHITE("text.autoconfig.blockcounter.color.white");

    private final String name;

    ChatColor(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
