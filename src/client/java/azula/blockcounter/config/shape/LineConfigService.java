package azula.blockcounter.config.shape;

public interface LineConfigService {

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

    LineConfigService getInstance();
}
