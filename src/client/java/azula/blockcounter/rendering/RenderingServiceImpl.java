package azula.blockcounter.rendering;

import azula.blockcounter.BlockCounterClient;
import azula.blockcounter.config.BlockCounterModMenuConfig;
import azula.blockcounter.config.RenderType;
import azula.blockcounter.config.shape.Shape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.gl.GlUsage;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.awt.Color;
import java.util.List;

public class RenderingServiceImpl implements RenderingService {

    private BufferBuilder lineBuffer = null;
    private BufferBuilder quadBuffer = null;

    private Color renderColor;
    private Color edgeColor;

    private VertexBuffer quadVertBuffer;
    private VertexBuffer lineVertBuffer;

    @Override
    public void setRenderColors(BlockCounterModMenuConfig config) {
        int renderRGB = config.renderColor;
        int edgeRGB = config.edgeColor;
        int a = config.alpha;

        // Convert to rgba so we don't have to store alpha separately
        int renderRGBA = (a << 24) | (renderRGB & 0x00FFFFFF);
        int edgeRGBA = (a << 24) | (edgeRGB & 0x00FFFFFF);

        this.renderColor = new Color(renderRGBA, true);
        this.edgeColor = new Color(edgeRGBA, true);
    }

    @Override
    public void rebuildBuffer(List<Vec3d> pos, RenderType renderType, boolean builderMode, WorldRenderContext context) {

        switch (renderType) {
            case SOLID -> this.rebuildQuadBuffer(pos, context, builderMode);
            case EDGE_ONLY -> this.rebuildLineBuffer(pos, context, builderMode);
            case SOLID_EDGE -> {
                this.rebuildQuadBuffer(pos, context, builderMode);
                this.rebuildLineBuffer(pos, context, builderMode);
            }
        }
    }

    private void rebuildQuadBuffer(List<Vec3d> pos, WorldRenderContext context, boolean builderMode) {
        this.startQuadBuffer();

        pos.forEach(p -> this.addSolid(context, p, builderMode));

        this.quadVertBuffer.bind();
        this.quadVertBuffer.upload(this.quadBuffer.end());
    }

    private void rebuildLineBuffer(List<Vec3d> pos, WorldRenderContext context, boolean builderMode) {
        this.startLineBuffer();

        pos.forEach(p -> this.addEdged(context, p, builderMode));

        this.lineVertBuffer.bind();
        this.lineVertBuffer.upload(this.lineBuffer.end());
    }

    private void startLineBuffer() {
        this.setRenderColors(BlockCounterClient.getInstance().getConfig());
        Tessellator tessellator = Tessellator.getInstance();
        this.lineBuffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        this.lineVertBuffer = new VertexBuffer(GlUsage.STATIC_WRITE);
    }

    private void startQuadBuffer() {
        this.setRenderColors(BlockCounterClient.getInstance().getConfig());
        Tessellator tessellator = Tessellator.getInstance();
        this.quadBuffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        this.quadVertBuffer = new VertexBuffer(GlUsage.STATIC_WRITE);
    }

    private void addSolid(WorldRenderContext context, Vec3d pos, boolean builderMode) {
        if (context.matrixStack() != null) {
            this.addSolidBlockToBuffer(pos.toVector3f(), builderMode);
        }
    }

    private void addEdged(WorldRenderContext context, Vec3d pos, boolean builderMode) {
        if (context.matrixStack() != null) {
            this.addEdgedBlockToBuffer(pos.toVector3f(), builderMode);
        }
    }

    private void addSolidBlockToBuffer(Vector3f pos, boolean builderMode) {
        Vector3f back_bl;
        Vector3f back_tl;
        Vector3f back_tr;
        Vector3f back_br;

        Vector3f front_bl;
        Vector3f front_tl;
        Vector3f front_tr;
        Vector3f front_br;

        if (!builderMode) {
            back_bl = new Vector3f(pos.x, pos.y, pos.z);
            back_tl = new Vector3f(pos.x, pos.y + 1, pos.z);
            back_tr = new Vector3f(pos.x + 1, pos.y + 1, pos.z);
            back_br = new Vector3f(pos.x + 1, pos.y, pos.z);

            front_bl = new Vector3f(pos.x, pos.y, pos.z + 1);
            front_tl = new Vector3f(pos.x, pos.y + 1, pos.z + 1);
            front_tr = new Vector3f(pos.x + 1, pos.y + 1, pos.z + 1);
            front_br = new Vector3f(pos.x + 1, pos.y, pos.z + 1);
        } else {
            back_bl = new Vector3f(pos.x + 0.33f, pos.y + 0.33f, pos.z + 0.33f);
            back_tl = new Vector3f(pos.x + 0.33f, pos.y + 0.66f, pos.z + 0.33f);
            back_tr = new Vector3f(pos.x + 0.66f, pos.y + 0.66f, pos.z + 0.33f);
            back_br = new Vector3f(pos.x + 0.66f, pos.y + 0.33f, pos.z + 0.33f);

            front_bl = new Vector3f(pos.x + 0.33f, pos.y + 0.33f, pos.z + 0.66f);
            front_tl = new Vector3f(pos.x + 0.33f, pos.y + 0.66f, pos.z + 0.66f);
            front_tr = new Vector3f(pos.x + 0.66f, pos.y + 0.66f, pos.z + 0.66f);
            front_br = new Vector3f(pos.x + 0.66f, pos.y + 0.33f, pos.z + 0.66f);
        }

        // back face
        this.quadBuffer.vertex(back_br.x, back_br.y, back_br.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(back_bl.x, back_bl.y, back_bl.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(back_tl.x, back_tl.y, back_tl.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(back_tr.x, back_tr.y, back_tr.z).color(this.renderColor.getRGB());

        // front face
        this.quadBuffer.vertex(front_bl.x, front_bl.y, front_bl.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(front_br.x, front_br.y, front_br.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(front_tr.x, front_tr.y, front_tr.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(front_tl.x, front_tl.y, front_tl.z).color(this.renderColor.getRGB());

        // left face
        this.quadBuffer.vertex(back_bl.x, back_bl.y, back_bl.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(front_bl.x, front_bl.y, front_bl.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(front_tl.x, front_tl.y, front_tl.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(back_tl.x, back_tl.y, back_tl.z).color(this.renderColor.getRGB());

        // right face
        this.quadBuffer.vertex(front_br.x, front_br.y, front_br.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(back_br.x, back_br.y, back_br.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(back_tr.x, back_tr.y, back_tr.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(front_tr.x, front_tr.y, front_tr.z).color(this.renderColor.getRGB());

        // top face
        this.quadBuffer.vertex(front_tl.x, front_tl.y, front_tl.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(front_tr.x, front_tr.y, front_tr.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(back_tr.x, back_tr.y, back_tr.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(back_tl.x, back_tl.y, back_tl.z).color(this.renderColor.getRGB());

        // bottom face
        this.quadBuffer.vertex(back_bl.x, back_bl.y, back_bl.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(back_br.x, back_br.y, back_br.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(front_br.x, front_br.y, front_br.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(front_bl.x, front_bl.y, front_bl.z).color(this.renderColor.getRGB());
    }

    private void addEdgedBlockToBuffer(Vector3f pos, boolean builderMode) {
        Vector3f back_bl;
        Vector3f back_tl;
        Vector3f back_tr;
        Vector3f back_br;

        Vector3f front_bl;
        Vector3f front_tl;
        Vector3f front_tr;
        Vector3f front_br;

        if (!builderMode) {
            back_bl = new Vector3f(pos.x, pos.y, pos.z);
            back_tl = new Vector3f(pos.x, pos.y + 1, pos.z);
            back_tr = new Vector3f(pos.x + 1, pos.y + 1, pos.z);
            back_br = new Vector3f(pos.x + 1, pos.y, pos.z);

            front_bl = new Vector3f(pos.x, pos.y, pos.z + 1);
            front_tl = new Vector3f(pos.x, pos.y + 1, pos.z + 1);
            front_tr = new Vector3f(pos.x + 1, pos.y + 1, pos.z + 1);
            front_br = new Vector3f(pos.x + 1, pos.y, pos.z + 1);
        } else {
            back_bl = new Vector3f(pos.x + 0.33f, pos.y + 0.33f, pos.z + 0.33f);
            back_tl = new Vector3f(pos.x + 0.33f, pos.y + 0.66f, pos.z + 0.33f);
            back_tr = new Vector3f(pos.x + 0.66f, pos.y + 0.66f, pos.z + 0.33f);
            back_br = new Vector3f(pos.x + 0.66f, pos.y + 0.33f, pos.z + 0.33f);

            front_bl = new Vector3f(pos.x + 0.33f, pos.y + 0.33f, pos.z + 0.66f);
            front_tl = new Vector3f(pos.x + 0.33f, pos.y + 0.66f, pos.z + 0.66f);
            front_tr = new Vector3f(pos.x + 0.66f, pos.y + 0.66f, pos.z + 0.66f);
            front_br = new Vector3f(pos.x + 0.66f, pos.y + 0.33f, pos.z + 0.66f);
        }

        // back bottom
        this.lineBuffer.vertex(back_br.x, back_br.y, back_br.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(back_bl.x, back_bl.y, back_bl.z).color(this.edgeColor.getRGB());
        // back right
        this.lineBuffer.vertex(back_bl.x, back_bl.y, back_bl.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(back_tl.x, back_tl.y, back_tl.z).color(this.edgeColor.getRGB());
        // back top
        this.lineBuffer.vertex(back_tl.x, back_tl.y, back_tl.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(back_tr.x, back_tr.y, back_tr.z).color(this.edgeColor.getRGB());
        // back left
        this.lineBuffer.vertex(back_tr.x, back_tr.y, back_tr.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(back_br.x, back_br.y, back_br.z).color(this.edgeColor.getRGB());

        // front bottom
        this.lineBuffer.vertex(front_bl.x, front_bl.y, front_bl.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(front_br.x, front_br.y, front_br.z).color(this.edgeColor.getRGB());
        // front right
        this.lineBuffer.vertex(front_br.x, front_br.y, front_br.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(front_tr.x, front_tr.y, front_tr.z).color(this.edgeColor.getRGB());
        // front top
        this.lineBuffer.vertex(front_tr.x, front_tr.y, front_tr.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(front_tl.x, front_tl.y, front_tl.z).color(this.edgeColor.getRGB());
        // front left
        this.lineBuffer.vertex(front_tl.x, front_tl.y, front_tl.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(front_bl.x, front_bl.y, front_bl.z).color(this.edgeColor.getRGB());

        // left bottom
        this.lineBuffer.vertex(back_bl.x, back_bl.y, back_bl.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(front_bl.x, front_bl.y, front_bl.z).color(this.edgeColor.getRGB());
        // left top
        this.lineBuffer.vertex(back_tl.x, back_tl.y, back_tl.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(front_tl.x, front_tl.y, front_tl.z).color(this.edgeColor.getRGB());

        // right bottom
        this.lineBuffer.vertex(back_br.x, back_br.y, back_br.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(front_br.x, front_br.y, front_br.z).color(this.edgeColor.getRGB());
        // right top
        this.lineBuffer.vertex(back_tr.x, back_tr.y, back_tr.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(front_tr.x, front_tr.y, front_tr.z).color(this.edgeColor.getRGB());
    }

    @Override
    public void render(WorldRenderContext context, RenderType type) {
        switch (type) {
            case SOLID -> this.renderQuadBuffer(context);
            case EDGE_ONLY -> this.renderLineBuffer(context);
            case SOLID_EDGE -> {
                this.renderQuadBuffer(context);
                this.renderLineBuffer(context);
            }
        }
    }

    private void renderQuadBuffer(WorldRenderContext context) {
        ShaderProgram shader = RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        RenderSystem.enableBlend();
        if (!BlockCounterClient.getInstance().getShapeConfigService().getSelectedShape().equals(Shape.LINE)) {
            RenderSystem.enableDepthTest();
        }

        Vector3f camPos = context.camera().getPos().toVector3f();

        MatrixStack stack = new MatrixStack();

        stack.push();

        stack.multiplyPositionMatrix(RenderSystem.getModelViewMatrix());
        stack.translate(-camPos.x, -camPos.y, -camPos.z);

        this.quadVertBuffer.bind();
        this.quadVertBuffer.draw(stack.peek().getPositionMatrix(), RenderSystem.getProjectionMatrix(), shader);

        stack.pop();

        RenderSystem.disableBlend();
        if (!BlockCounterClient.getInstance().getShapeConfigService().getSelectedShape().equals(Shape.LINE)) {
            RenderSystem.disableDepthTest();
        }
    }

    private void renderLineBuffer(WorldRenderContext context) {
        ShaderProgram shader = RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        RenderSystem.enableBlend();
        if (!BlockCounterClient.getInstance().getShapeConfigService().getSelectedShape().equals(Shape.LINE)) {
            RenderSystem.enableDepthTest();
        }

        Vector3f camPos = context.camera().getPos().toVector3f();

        MatrixStack stack = new MatrixStack();

        stack.push();

        stack.multiplyPositionMatrix(RenderSystem.getModelViewMatrix());
        stack.translate(-camPos.x, -camPos.y, -camPos.z);

        this.lineVertBuffer.bind();
        this.lineVertBuffer.draw(stack.peek().getPositionMatrix(), RenderSystem.getProjectionMatrix(), shader);

        stack.pop();

        RenderSystem.disableBlend();
        if (!BlockCounterClient.getInstance().getShapeConfigService().getSelectedShape().equals(Shape.LINE)) {
            RenderSystem.disableDepthTest();
        }
    }
}
