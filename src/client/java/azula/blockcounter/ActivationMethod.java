package azula.blockcounter;

public enum ActivationMethod {
    CLICK("text.autoconfig.blockcounter.click"),
    STANDING("text.autoconfig.blockcounter.standing");

    private final String name;

    ActivationMethod(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
