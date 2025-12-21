package azula.blockcounter.rendering.world;

import net.minecraft.client.util.math.MatrixStack;

public class BlockCounterRenderContext {

    private MatrixStack matrixStack;

    public BlockCounterRenderContext(MatrixStack matrixStack) {
        this.matrixStack = matrixStack;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public void setMatrixStack(MatrixStack matrixStack) {
        this.matrixStack = matrixStack;
    }
}
