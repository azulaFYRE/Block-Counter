package azula.blockcounter.config.shape;

public class ShapeConfigServiceImpl implements ShapeConfigService {

    private Shape selectedShape;

    private boolean canPlaceLine;
    private boolean isAxisAligned;
    private boolean isTwoAxis;

    private int xOffset;
    private int yOffset;
    private int zOffset;

    private int[] quadDims = {3, 3, 3}; // width, length, height
    private int[] circleDims = {1, 1, 0}; // radius, height, for now...
    private int[] sphereDims = {1, 0, 0}; // just radius, for now...
    private int[] EMPTY_DIMS = {0, 0, 0};

    public ShapeConfigServiceImpl() {
        this.selectedShape = Shape.QUAD;
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
            default -> EMPTY_DIMS;
        };
    }
}
