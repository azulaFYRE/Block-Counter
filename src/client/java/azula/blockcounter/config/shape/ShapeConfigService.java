package azula.blockcounter.config.shape;

import azula.blockcounter.BlockCounterClient;
import azula.blockcounter.Shape;
import azula.blockcounter.util.BlockCalculations;

public class ShapeConfigService {

    private Shape selectedShape;

    private boolean placeLine;
    private boolean axisAligned;
    private boolean twoAxis;
    private boolean isSphere;
    private boolean calcUsingRendered;

    private int qLength;
    private int qWidth;
    private int qHeight;

    private int cRadius;
    private int cHeight;

    private int xOffset;
    private int yOffset;
    private int zOffset;

    private int totalBlocks;

    public ShapeConfigService() {
        this.selectedShape = Shape.LINE;

        this.placeLine = false;
        this.axisAligned = true;
        this.twoAxis = false;

        this.isSphere = false;

        this.calcUsingRendered = false;

        this.qLength = 1;
        this.qWidth = 1;
        this.qHeight = 1;

        this.cRadius = 1;
        this.cHeight = 1;

        this.xOffset = 0;
        this.yOffset = 0;
        this.zOffset = 0;

        this.totalBlocks = 0;

        this.calculateTotalBlocks();
    }

    private void calculateTotalBlocks() {
        Shape selected = this.selectedShape;

        if (selected.equals(Shape.QUAD)) {
            this.totalBlocks = BlockCalculations.calculateBlocksQuad();
        } else if (selected.equals(Shape.CIRCLE)) {
            this.totalBlocks = this.isSphere() ? BlockCalculations.calculateBlocksSphere()
                    : BlockCalculations.calculateBlocksCircle();
        } else {
            this.totalBlocks = 0;
        }
    }

    public Shape getSelectedShape() {
        return selectedShape;
    }

    public void setSelectedShape(Shape selectedShape) {
        this.selectedShape = selectedShape;
        BlockCounterClient.getInstance().shapeChanged();
        this.calculateTotalBlocks();
    }

    public boolean canPlaceLine() {
        return placeLine;
    }

    public void setPlaceLine(boolean placeLine) {
        this.placeLine = placeLine;
    }

    public boolean isAxisAligned() {
        return axisAligned;
    }

    public void setAxisAligned(boolean axisAligned) {
        this.axisAligned = axisAligned;
    }

    public boolean isTwoAxis() {
        return twoAxis;
    }

    public void setTwoAxis(boolean twoAxis) {
        this.twoAxis = twoAxis;
    }

    public boolean isSphere() {
        return isSphere;
    }

    public void setSphere(boolean sphere) {
        isSphere = sphere;
        this.calculateTotalBlocks();
    }

    public boolean isCalcUsingRendered() {
        return calcUsingRendered;
    }

    public void setCalcUsingRendered(boolean calcUsingRendered) {
        this.calcUsingRendered = calcUsingRendered;
        this.calculateTotalBlocks();
    }

    public int getQLength() {
        return qLength;
    }

    public void setQLength(int qLength) {
        this.qLength = qLength;
        this.calculateTotalBlocks();
    }

    public int getQWidth() {
        return qWidth;
    }

    public void setQWidth(int qWidth) {
        this.qWidth = qWidth;
        this.calculateTotalBlocks();
    }

    public int getQHeight() {
        return qHeight;
    }

    public void setQHeight(int qHeight) {
        this.qHeight = qHeight;
        this.calculateTotalBlocks();
    }

    public int getCRadius() {
        return cRadius;
    }

    public void setCRadius(int cRadius) {
        this.cRadius = cRadius;
        this.calculateTotalBlocks();
    }

    public int getCHeight() {
        return cHeight;
    }

    public void setCHeight(int cHeight) {
        this.cHeight = cHeight;
        this.calculateTotalBlocks();
    }

    public int getXOffset() {
        return xOffset;
    }

    public void setXOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public void setYOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    public int getZOffset() {
        return zOffset;
    }

    public void setZOffset(int zOffset) {
        this.zOffset = zOffset;
    }

    public int getTotalBlocks() {
        return totalBlocks;
    }

}
