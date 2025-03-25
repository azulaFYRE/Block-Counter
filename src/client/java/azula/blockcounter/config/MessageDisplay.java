package azula.blockcounter.config;

public enum MessageDisplay {
    CHAT("text.autoconfig.blockcounter.messageDisplay.chat"),
    ABOVE_HOTBAR("text.autoconfig.blockcounter.messageDisplay.aboveHotbar");

    private final String name;

    MessageDisplay(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}