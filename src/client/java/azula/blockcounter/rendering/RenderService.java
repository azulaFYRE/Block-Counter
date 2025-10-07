package azula.blockcounter.rendering;

import me.x150.renderer.render.CustomRenderLayers;
import me.x150.renderer.render.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;

public interface RenderService {

    int BUFFER_SIZE = 1024;

    static void drawFilled(MatrixStack matrixStack, Vec3d start, Vec3d dimensions, Color color) {
        VertexConsumerProvider.Immediate vcp = VertexConsumerProvider.immediate(new BufferAllocator(BUFFER_SIZE));
        WorldRenderContext renderContext = new WorldRenderContext(MinecraftClient.getInstance(), vcp);

        if (matrixStack != null) {
            RenderLayer layer = CustomRenderLayers.getPositionColorQuads(true);
            renderContext.drawFilledCube(matrixStack,
                    layer, start,
                    (float) dimensions.x,
                    (float) dimensions.y,
                    (float) dimensions.z,
                    new me.x150.renderer.util.Color(color));
        }

        vcp.draw();
    }

    static void drawOutlined(MatrixStack matrixStack, Vec3d start, Vec3d dimensions, Color color) {

        VertexConsumerProvider.Immediate vcp = VertexConsumerProvider.immediate(new BufferAllocator(BUFFER_SIZE));
        WorldRenderContext renderContext = new WorldRenderContext(MinecraftClient.getInstance(), vcp);

        double w = dimensions.x;
        double h = dimensions.y;
        double d = dimensions.z;


        Vec3d p1 = start;
        Vec3d p2 = start.add(w, 0, 0);
        Vec3d p3 = start.add(w, 0, d);
        Vec3d p4 = start.add(0, 0, d);
        Vec3d p5 = start.add(0, h, 0);
        Vec3d p6 = start.add(w, h, 0);
        Vec3d p7 = start.add(w, h, d);
        Vec3d p8 = start.add(0, h, d);

        if (matrixStack != null) {
            RenderLayer layer = CustomRenderLayers.getLines(1.0f, true);

            me.x150.renderer.util.Color other = new me.x150.renderer.util.Color(color);

            renderContext.drawLine(matrixStack, layer, p1, p2, other);
            renderContext.drawLine(matrixStack, layer, p2, p3, other);
            renderContext.drawLine(matrixStack, layer, p3, p4, other);
            renderContext.drawLine(matrixStack, layer, p4, p1, other);
            renderContext.drawLine(matrixStack, layer, p1, p5, other);
            renderContext.drawLine(matrixStack, layer, p2, p6, other);
            renderContext.drawLine(matrixStack, layer, p3, p7, other);
            renderContext.drawLine(matrixStack, layer, p4, p8, other);
            renderContext.drawLine(matrixStack, layer, p5, p6, other);
            renderContext.drawLine(matrixStack, layer, p6, p7, other);
            renderContext.drawLine(matrixStack, layer, p7, p8, other);
            renderContext.drawLine(matrixStack, layer, p8, p5, other);
        }

        vcp.draw();
    }

    static void drawFillAndOutlined(MatrixStack matrixStack, Vec3d start, Vec3d dimensions, Color fillColor, Color edgeColor) {
        RenderService.drawFilled(matrixStack, start, dimensions, fillColor);
        RenderService.drawOutlined(matrixStack, start, dimensions, edgeColor);
    }

}
