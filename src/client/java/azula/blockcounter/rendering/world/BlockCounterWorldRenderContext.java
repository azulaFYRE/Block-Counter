package azula.blockcounter.rendering.world;

import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Matrix4f;

public class BlockCounterWorldRenderContext {

    private VertexConsumerProvider vertexConsumer;
    private Matrix4f positionMatrix;

    public BlockCounterWorldRenderContext(VertexConsumerProvider vertexConsumer, Matrix4f positionMatrix) {
        this.vertexConsumer = vertexConsumer;
        this.positionMatrix = positionMatrix;
    }

    public VertexConsumerProvider getVertexConsumer() {
        return vertexConsumer;
    }

    public void setVertexConsumer(VertexConsumerProvider vertexConsumer) {
        this.vertexConsumer = vertexConsumer;
    }

    public Matrix4f getPositionMatrix() {
        return positionMatrix;
    }

    public void setPositionMatrix(Matrix4f positionMatrix) {
        this.positionMatrix = positionMatrix;
    }
}
