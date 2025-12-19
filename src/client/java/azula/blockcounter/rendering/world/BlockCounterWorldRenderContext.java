package azula.blockcounter.rendering.world;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public class BlockCounterWorldRenderContext {
    private MatrixStack matrixStack;
    private VertexConsumerProvider vertexConsumerProvider;
    private Camera camera;

    public BlockCounterWorldRenderContext(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, Camera camera) {
        this.matrixStack = matrixStack;
        this.vertexConsumerProvider = vertexConsumerProvider;
        this.camera = camera;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public void setMatrixStack(MatrixStack matrixStack) {
        this.matrixStack = matrixStack;
    }

    public VertexConsumerProvider getVertexConsumerProvider() {
        return vertexConsumerProvider;
    }

    public void setVertexConsumerProvider(VertexConsumerProvider vertexConsumerProvider) {
        this.vertexConsumerProvider = vertexConsumerProvider;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }
}
