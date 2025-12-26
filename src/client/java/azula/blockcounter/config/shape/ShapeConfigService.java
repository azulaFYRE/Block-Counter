package azula.blockcounter.config.shape;

import net.minecraft.util.math.Vec3d;

public interface ShapeConfigService {

    Shape getSelectedShape();
    void setSelectedShape(Shape shape);

    boolean canPlaceLine();
    void setPlaceLine(boolean canPlace);

    boolean isAxisAligned();
    void setAxisAligned(boolean axisAligned);

    boolean isTwoAxis();
    void setTwoAxis(boolean twoAxis);

    Vec3d getOffsets();

    int getXOffset();
    void setXOffset(int x);

    int getYOffset();
    void setYOffset(int y);

    int getZOffset();
    void setZOffset(int z);

    int[] getDimensions();

    void setQuadWidth(int width);
    void setQuadLength(int length);
    void setQuadHeight(int height);

    void setCircleRadius(int radius);
    void setCircleHeight(int height);

    void setSphereRadius(int radius);

    void cycleShape();
}