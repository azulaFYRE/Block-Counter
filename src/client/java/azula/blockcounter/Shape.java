package azula.blockcounter;

public enum Shape {
    LINE("Line"),
    QUAD("Quad"),
    CIRCLE("Circle");

    private final String name;

    Shape(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Shape parseInt(int toParse) {
        return switch (toParse) {
            case 0 -> LINE;
            case 1 -> QUAD;
            case 2 -> CIRCLE;
            default -> null;
        };
    }
}
