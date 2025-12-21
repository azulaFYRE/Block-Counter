package azula.blockcounter.config.shape;

public interface ShapeConfigService {

    Shape getSelectedShape();
    void setSelectedShape(Shape shape);

    boolean canPlaceLine();
    void setPlaceLine(boolean canPlace);

    boolean isAxisAligned();
    void setAxisAligned(boolean axisAligned);

    boolean isTwoAxis();
    void setTwoAxis(boolean twoAxis);

    int getXOffset();
    void setXOffset(int x);

    int getYOffset();
    void setYOffset(int y);

    int getZOffset();
    void setZOffset(int z);

    int[] getDimensions();
}
