package azula.blockcounter.config.shape;

public enum Shape {
    LINE("Line"),
    QUAD("Quad"),
    CIRCLE("Circle"),
    SPHERE("Sphere");

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
            case 3 -> SPHERE;
            default -> null;
        };
    }
}