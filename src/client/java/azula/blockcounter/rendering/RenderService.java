package azula.blockcounter.rendering;

import me.x150.renderer.render.CustomRenderLayers;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;

public interface RenderService {

    static void drawFilled(WorldRenderContext context, Vec3d start, Vec3d dimensions, Color color) {
        me.x150.renderer.render.WorldRenderContext renderContext = new me.x150.renderer.render.WorldRenderContext(MinecraftClient.getInstance(), context.consumers());

        if (context.matrixStack() != null) {
            RenderLayer layer = CustomRenderLayers.getPositionColorQuads(true);
            renderContext.drawFilledCube(context.matrixStack(),
                    layer, start,
                    (float) dimensions.x,
                    (float) dimensions.y,
                    (float) dimensions.z,
                    new me.x150.renderer.util.Color(color));
        }
    }

    static void drawOutlined(WorldRenderContext context, Vec3d start, Vec3d dimensions, Color color) {
        me.x150.renderer.render.WorldRenderContext renderContext = new me.x150.renderer.render.WorldRenderContext(MinecraftClient.getInstance(), context.consumers());

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

        if (context.matrixStack() != null) {
            // CHANGE THIS LATER TO BE TRUE ONCE IT GETS FIXED
            RenderLayer layer = CustomRenderLayers.getLines(1.0f, false);

            me.x150.renderer.util.Color other = new me.x150.renderer.util.Color(color);

            renderContext.drawLine(context.matrixStack(), layer, p1, p2, other);
            renderContext.drawLine(context.matrixStack(), layer, p2, p3, other);
            renderContext.drawLine(context.matrixStack(), layer, p3, p4, other);
            renderContext.drawLine(context.matrixStack(), layer, p4, p1, other);
            renderContext.drawLine(context.matrixStack(), layer, p1, p5, other);
            renderContext.drawLine(context.matrixStack(), layer, p2, p6, other);
            renderContext.drawLine(context.matrixStack(), layer, p3, p7, other);
            renderContext.drawLine(context.matrixStack(), layer, p4, p8, other);
            renderContext.drawLine(context.matrixStack(), layer, p5, p6, other);
            renderContext.drawLine(context.matrixStack(), layer, p6, p7, other);
            renderContext.drawLine(context.matrixStack(), layer, p7, p8, other);
            renderContext.drawLine(context.matrixStack(), layer, p8, p5, other);
        }
    }

    static void drawFillAndOutlined(WorldRenderContext context, Vec3d start, Vec3d dimensions, Color fillColor, Color edgeColor) {
        RenderService.drawFilled(context, start, dimensions, fillColor);
        RenderService.drawOutlined(context, start, dimensions, edgeColor);
    }

}
