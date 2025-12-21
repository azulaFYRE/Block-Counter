package azula.blockcounter.mixin.client;

import azula.blockcounter.rendering.world.BlockCounterRenderContext;
import azula.blockcounter.rendering.world.BlockCounterRenderEvents;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public abstract class BlockCounterGameRendererMixin {

    // this is pretty much directly from the old library I would use for rendering
    // I couldn't figure out the proper mixin so just used theirs
    // https://github.com/0x3C50/Renderer/commit/072d3b2ebaef2c8107a6f9a6aa5e81028751cfb4#diff-48860bd3ff82c23ad32fd98d1294816930558cc6bb4a253ed8209c7a2fa85847
    @WrapOperation(method = "renderWorld", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/ObjectAllocator;" +
                    "Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;" +
                    "Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V"))
    void afterWorldRender(WorldRenderer instance, ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline,
                          Camera camera, Matrix4f positionMatrix, Matrix4f matrix4f, Matrix4f projectionMatrix, GpuBufferSlice fogBuffer,
                          Vector4f fogColor, boolean renderSky, Operation<Void> original)
    {
        original.call(instance, allocator, tickCounter, renderBlockOutline, camera, positionMatrix, matrix4f, projectionMatrix, fogBuffer, fogColor, renderSky);

        GlStateManager._depthMask(false);
        GlStateManager._enableBlend();

        Profiler profiler = Profilers.get();
        profiler.swap("bcGameWorldRender");

        MatrixStack stack = new MatrixStack();
        stack.multiplyPositionMatrix(positionMatrix);

        BlockCounterRenderContext context = new BlockCounterRenderContext(stack);

        BlockCounterRenderEvents.AFTER_WORLD.invoker().onRender(context);

        // restore
        GlStateManager._depthMask(true);
        GlStateManager._disableBlend();
    }
}
