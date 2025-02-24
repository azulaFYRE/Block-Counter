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
}
