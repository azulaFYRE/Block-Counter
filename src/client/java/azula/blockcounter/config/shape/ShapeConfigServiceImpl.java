package azula.blockcounter.config.shape;

import azula.blockcounter.BlockCounterClient;
import net.minecraft.util.math.Vec3d;

public class ShapeConfigServiceImpl implements ShapeConfigService {

    private Shape selectedShape;

    private boolean canPlaceLine;
    private boolean isAxisAligned;
    private boolean isTwoAxis;

    private int xOffset;
    private int yOffset;
    private int zOffset;

    private int[] quadDims = {1, 1, 1}; // width, length, height
    private int[] circleDims = {1, 1, 0}; // radius, height, for now...
    private int[] sphereDims = {1, 0, 0}; // just radius, for now...
    private int[] EMPTY_DIMS = {0, 0, 0};

    public ShapeConfigServiceImpl() {
        this.selectedShape = Shape.LINE;

        this.canPlaceLine = false;
        this.isAxisAligned = true;
        this.isTwoAxis = false;

        this.xOffset = 0;
        this.yOffset = 0;
        this.zOffset = 0;
    }

    @Override
    public Shape getSelectedShape() {
        return this.selectedShape;
    }

    @Override
    public void setSelectedShape(Shape shape) {
        this.selectedShape = shape;
    }

    @Override
    public boolean canPlaceLine() {
        return this.canPlaceLine;
    }

    @Override
    public void setPlaceLine(boolean canPlace) {
        this.canPlaceLine = canPlace;
    }

    @Override
    public boolean isAxisAligned() {
        return this.isAxisAligned;
    }

    @Override
    public void setAxisAligned(boolean axisAligned) {
        this.isAxisAligned = axisAligned;
    }

    @Override
    public boolean isTwoAxis() {
        return this.isTwoAxis;
    }

    @Override
    public void setTwoAxis(boolean twoAxis) {
        this.isTwoAxis = twoAxis;
    }

    @Override
    public Vec3d getOffsets() {
        return new Vec3d(this.xOffset, this.yOffset, this.zOffset);
    }

    @Override
    public int getXOffset() {
        return this.xOffset;
    }

    @Override
    public void setXOffset(int x) {
        this.xOffset = x;
    }

    @Override
    public int getYOffset() {
        return this.yOffset;
    }

    @Override
    public void setYOffset(int y) {
        this.yOffset = y;
    }

    @Override
    public int getZOffset() {
        return this.zOffset;
    }

    @Override
    public void setZOffset(int z) {
        this.zOffset = z;
    }

    @Override
    public int[] getDimensions() {
        return switch (this.selectedShape) {
            case QUAD -> this.quadDims;
            case CIRCLE -> this.circleDims;
            case SPHERE -> this.sphereDims;
            default -> EMPTY_DIMS; // for line etc
        };
    }

    @Override
    public void setQuadWidth(int width) {
        this.quadDims[0] = width;
    }

    @Override
    public void setQuadLength(int length) {
        this.quadDims[1] = length;
    }

    @Override
    public void setQuadHeight(int height) {
        this.quadDims[2] = height;
    }

    @Override
    public void setCircleRadius(int radius) {
        this.circleDims[0] = radius;
    }

    @Override
    public void setCircleHeight(int height) {
        this.circleDims[1] = height;
    }

    @Override
    public void setSphereRadius(int radius) {
        this.sphereDims[0] = radius;
    }

    @Override
    public void cycleShape() {
        switch (this.selectedShape) {
            case LINE -> {
                this.selectedShape = Shape.QUAD;

                this.isAxisAligned = true;
                this.isTwoAxis = false;
                this.canPlaceLine = false;
            }
            case QUAD -> {
                this.selectedShape = Shape.CIRCLE;

                this.quadDims[0] = 1; // w
                this.quadDims[1] = 1; // l
                this.quadDims[2] = 1; // h
            }
            case CIRCLE -> {
                this.selectedShape = Shape.SPHERE;

                this.circleDims[0] = 1; // r
                this.circleDims[1] = 1; // h
            }
            case SPHERE -> {
                this.selectedShape = Shape.LINE;

                this.sphereDims[0] = 1; // r
            }
        }

        this.xOffset = 0;
        this.yOffset = 0;
        this.zOffset = 0;

        BlockCounterClient.getInstance().shapeChanged();
    }
}