package azula.blockcounter.mixin.client;

import azula.blockcounter.rendering.world.BlockCounterWorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class BlockCounterWorldRendererMixin {

    @Inject(method = "ren")

    @Inject(method = "renderBlockEntities", at = @At(value = "RETURN"))
    private void grabRenderBlockEntitiesMatrix(MatrixStack matrices, WorldRenderState renderStates, OrderedRenderCommandQueueImpl queue, CallbackInfo ci) {
        VertexConsumerProvider entityProvider = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();

        BlockCounterWorldRenderContext context = new BlockCounterWorldRenderContext(matrices, entityProvider, camera);
    }
}
