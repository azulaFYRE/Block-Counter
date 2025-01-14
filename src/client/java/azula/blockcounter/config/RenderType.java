package azula.blockcounter.config;

public enum RenderType {

    SOLID("text.autoconfig.blockcounter.renderType.solid"),
    EDGE_ONLY("text.autoconfig.blockcounter.renderType.edgesOnly"),
    SOLID_EDGE("text.autoconfig.blockcounter.renderType.solidEdges");

    private final String name;

    RenderType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
