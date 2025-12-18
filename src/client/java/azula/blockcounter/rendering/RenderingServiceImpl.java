package azula.blockcounter.rendering;

import azula.blockcounter.BlockCounterClient;
import azula.blockcounter.config.BlockCounterModMenuConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.Color;

public class RenderingServiceImpl implements RenderingService {

    private BufferBuilder lineBuffer = null;
    private BufferBuilder quadBuffer = null;
    private Color renderColor;
    private Color edgeColor;

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

    public void startLineBuffer() {
        this.setRenderColors(BlockCounterClient.getInstance().getConfig());
        Tessellator tessellator = Tessellator.getInstance();
        this.lineBuffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
    }

    public void startQuadBuffer() {
        this.setRenderColors(BlockCounterClient.getInstance().getConfig());
        Tessellator tessellator = Tessellator.getInstance();
        this.quadBuffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
    }

    @Override
    public void addSolid(WorldRenderContext context, Vec3d pos) {

        if (context.matrixStack() != null) {

            Vec3d cameraPos = context.camera().getPos();
            Vector3f posInCam = pos.toVector3f().sub(cameraPos.toVector3f());

            Matrix4f tranMatrix = context.matrixStack().peek().getPositionMatrix();

            Vector3f transformPos = new Vector3f();

            tranMatrix.transformPosition(posInCam.x, posInCam.y, posInCam.z, transformPos);

            this.addSolidBlockToBuffer(tranMatrix, transformPos);
        }

    }

    @Override
    public void addEdged(WorldRenderContext context, Vec3d pos) {
        if (context.matrixStack() != null) {

            Vec3d cameraPos = context.camera().getPos();
            Vector3f posInCam = pos.toVector3f().sub(cameraPos.toVector3f());

            Matrix4f tranMatrix = context.matrixStack().peek().getPositionMatrix();

            Vector3f transformPos = new Vector3f();

            tranMatrix.transformPosition(posInCam.x, posInCam.y, posInCam.z, transformPos);

            this.addEdgedBlockToBuffer(tranMatrix, transformPos);
        }
    }

    private void addSolidBlockToBuffer(Matrix4f tranMatrix, Vector3f pos) {
        Vector3f back_bl = new Vector3f(pos.x, pos.y, pos.z);
        Vector3f back_tl = new Vector3f(pos.x, pos.y + 1, pos.z);
        Vector3f back_tr = new Vector3f(pos.x + 1, pos.y + 1, pos.z);
        Vector3f back_br = new Vector3f(pos.x + 1, pos.y, pos.z);

        Vector3f front_bl = new Vector3f(pos.x, pos.y, pos.z + 1);
        Vector3f front_tl = new Vector3f(pos.x, pos.y + 1, pos.z + 1);
        Vector3f front_tr = new Vector3f(pos.x + 1, pos.y + 1, pos.z + 1);
        Vector3f front_br = new Vector3f(pos.x + 1, pos.y, pos.z + 1);

        // back face
        this.quadBuffer.vertex(tranMatrix, back_br.x, back_br.y, back_br.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(tranMatrix, back_bl.x, back_bl.y, back_bl.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(tranMatrix, back_tl.x, back_tl.y, back_tl.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(tranMatrix, back_tr.x, back_tr.y, back_tr.z).color(this.renderColor.getRGB());

        // front face
        this.quadBuffer.vertex(tranMatrix, front_bl.x, front_bl.y, front_bl.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(tranMatrix, front_br.x, front_br.y, front_br.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(tranMatrix, front_tr.x, front_tr.y, front_tr.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(tranMatrix, front_tl.x, front_tl.y, front_tl.z).color(this.renderColor.getRGB());

        // left face
        this.quadBuffer.vertex(tranMatrix, back_bl.x, back_bl.y, back_bl.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(tranMatrix, front_bl.x, front_bl.y, front_bl.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(tranMatrix, front_tl.x, front_tl.y, front_tl.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(tranMatrix, back_tl.x, back_tl.y, back_tl.z).color(this.renderColor.getRGB());

        // right face
        this.quadBuffer.vertex(tranMatrix, front_br.x, front_br.y, front_br.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(tranMatrix, back_br.x, back_br.y, back_br.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(tranMatrix, back_tr.x, back_tr.y, back_tr.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(tranMatrix, front_tr.x, front_tr.y, front_tr.z).color(this.renderColor.getRGB());

        // top face
        this.quadBuffer.vertex(tranMatrix, front_tl.x, front_tl.y, front_tl.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(tranMatrix, front_tr.x, front_tr.y, front_tr.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(tranMatrix, back_tr.x, back_tr.y, back_tr.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(tranMatrix, back_tl.x, back_tl.y, back_tl.z).color(this.renderColor.getRGB());

        // bottom face
        this.quadBuffer.vertex(tranMatrix, back_bl.x, back_bl.y, back_bl.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(tranMatrix, back_br.x, back_br.y, back_br.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(tranMatrix, front_br.x, front_br.y, front_br.z).color(this.renderColor.getRGB());
        this.quadBuffer.vertex(tranMatrix, front_bl.x, front_bl.y, front_bl.z).color(this.renderColor.getRGB());
    }

    private void addEdgedBlockToBuffer(Matrix4f tranMatrix, Vector3f pos) {
        Vector3f back_bl = new Vector3f(pos.x, pos.y, pos.z);
        Vector3f back_tl = new Vector3f(pos.x, pos.y + 1, pos.z);
        Vector3f back_tr = new Vector3f(pos.x + 1, pos.y + 1, pos.z);
        Vector3f back_br = new Vector3f(pos.x + 1, pos.y, pos.z);

        Vector3f front_bl = new Vector3f(pos.x, pos.y, pos.z + 1);
        Vector3f front_tl = new Vector3f(pos.x, pos.y + 1, pos.z + 1);
        Vector3f front_tr = new Vector3f(pos.x + 1, pos.y + 1, pos.z + 1);
        Vector3f front_br = new Vector3f(pos.x + 1, pos.y, pos.z + 1);

        // back bottom
        this.lineBuffer.vertex(tranMatrix, back_br.x, back_br.y, back_br.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(tranMatrix, back_bl.x, back_bl.y, back_bl.z).color(this.edgeColor.getRGB());
        // back right
        this.lineBuffer.vertex(tranMatrix, back_bl.x, back_bl.y, back_bl.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(tranMatrix, back_tl.x, back_tl.y, back_tl.z).color(this.edgeColor.getRGB());
        // back top
        this.lineBuffer.vertex(tranMatrix, back_tl.x, back_tl.y, back_tl.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(tranMatrix, back_tr.x, back_tr.y, back_tr.z).color(this.edgeColor.getRGB());
        // back left
        this.lineBuffer.vertex(tranMatrix, back_tr.x, back_tr.y, back_tr.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(tranMatrix, back_br.x, back_br.y, back_br.z).color(this.edgeColor.getRGB());

        // front bottom
        this.lineBuffer.vertex(tranMatrix, front_bl.x, front_bl.y, front_bl.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(tranMatrix, front_br.x, front_br.y, front_br.z).color(this.edgeColor.getRGB());
        // front right
        this.lineBuffer.vertex(tranMatrix, front_br.x, front_br.y, front_br.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(tranMatrix, front_tr.x, front_tr.y, front_tr.z).color(this.edgeColor.getRGB());
        // front top
        this.lineBuffer.vertex(tranMatrix, front_tr.x, front_tr.y, front_tr.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(tranMatrix, front_tl.x, front_tl.y, front_tl.z).color(this.edgeColor.getRGB());
        // front left
        this.lineBuffer.vertex(tranMatrix, front_tl.x, front_tl.y, front_tl.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(tranMatrix, front_bl.x, front_bl.y, front_bl.z).color(this.edgeColor.getRGB());

        // left bottom
        this.lineBuffer.vertex(tranMatrix, back_bl.x, back_bl.y, back_bl.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(tranMatrix, front_bl.x, front_bl.y, front_bl.z).color(this.edgeColor.getRGB());
        // left top
        this.lineBuffer.vertex(tranMatrix, back_tl.x, back_tl.y, back_tl.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(tranMatrix, front_tl.x, front_tl.y, front_tl.z).color(this.edgeColor.getRGB());

        // right bottom
        this.lineBuffer.vertex(tranMatrix, back_br.x, back_br.y, back_br.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(tranMatrix, front_br.x, front_br.y, front_br.z).color(this.edgeColor.getRGB());
        // right top
        this.lineBuffer.vertex(tranMatrix, back_tr.x, back_tr.y, back_tr.z).color(this.edgeColor.getRGB());
        this.lineBuffer.vertex(tranMatrix, front_tr.x, front_tr.y, front_tr.z).color(this.edgeColor.getRGB());
    }

    @Override
    public void renderQuadBuffer(WorldRenderContext context) {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        RenderSystem.enableBlend();

        BufferRenderer.drawWithGlobalProgram(this.quadBuffer.end());

        RenderSystem.disableBlend();

        // cleanup
        this.quadBuffer = null;
    }

    @Override
    public void renderLineBuffer(WorldRenderContext context) {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        RenderSystem.enableBlend();

        BufferRenderer.drawWithGlobalProgram(this.lineBuffer.end());

        RenderSystem.disableBlend();

        // cleanup
        this.lineBuffer = null;
    }
}
