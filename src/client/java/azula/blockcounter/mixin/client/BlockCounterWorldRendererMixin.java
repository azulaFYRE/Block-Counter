package azula.blockcounter.mixin.client;

import azula.blockcounter.rendering.world.BlockCounterWorldRenderContext;
import azula.blockcounter.rendering.world.BlockCounterWorldRenderEvents;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class BlockCounterWorldRendererMixin {

    @Inject(method = "render", at = @At(value = "TAIL"))
    private void renderLast(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline,
                            Camera camera, Matrix4f positionMatrix, Matrix4f matrix4f, Matrix4f projectionMatrix,
                            GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
        VertexConsumerProvider vertexConsumerProvider = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        BlockCounterWorldRenderContext context = new BlockCounterWorldRenderContext(vertexConsumerProvider, positionMatrix);

        BlockCounterWorldRenderEvents.LAST.invoker().onRender(context);
    }
}
