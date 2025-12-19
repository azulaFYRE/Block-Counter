package azula.blockcounter.rendering;

import azula.blockcounter.BlockCounterClient;
import azula.blockcounter.config.BlockCounterModMenuConfig;
import azula.blockcounter.rendering.world.BlockCounterWorldRenderContext;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.Color;

public class RenderingServiceImpl implements RenderingService {

    private VertexConsumer lineBuffer = null;
    private VertexConsumer quadBuffer = null;

    private Color renderColor;
    private Color edgeColor;

    private static final RenderPipeline QUAD_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation(Identifier.of(BlockCounterClient.MOD_ID, "pipeline/block_counter_quad_pipeline"))
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withBlend(BlendFunction.TRANSLUCENT)
            .build()
    );

    private static final RenderPipeline LINE_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
                    .withLocation(Identifier.of(BlockCounterClient.MOD_ID, "pipeline/block_counter_line_pipeline"))
                    .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.DEBUG_LINES)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .build()
    );

    public static final RenderLayer COLORED_QUADS_RENDER_LAYER = RenderLayer.of("block_counter_quad_layer", 256,
            QUAD_PIPELINE, RenderLayer.MultiPhaseParameters.builder().build(false));

    public static final RenderLayer LINES_RENDER_LAYER = RenderLayer.of("block_counter_line_layer", 256,
            LINE_PIPELINE, RenderLayer.MultiPhaseParameters.builder().build(false));

    Identifier whiteTexture = Identifier.of(BlockCounterClient.MOD_ID, "textures/random/white.png");

    private final RenderLayer quadLayer;
    private final RenderLayer lineLayer;

    // I would imagine there are better ways of doing this,
    // but this is the simplest I could find lol.
    public RenderingServiceImpl() {
        if (FabricLoader.getInstance().isModLoaded("iris")) {
            quadLayer = RenderLayer.getEntityTranslucent(whiteTexture);
            lineLayer = RenderLayer.getLines();
        } else {
            quadLayer = COLORED_QUADS_RENDER_LAYER;
            lineLayer = LINES_RENDER_LAYER;
        }
    }

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

    public void startLineBuffer(BlockCounterWorldRenderContext context) {
        this.setRenderColors(BlockCounterClient.getInstance().getConfig());
        this.lineBuffer = context.getVertexConsumerProvider().getBuffer(this.lineLayer);
    }

    public void startQuadBuffer(BlockCounterWorldRenderContext context) {
        this.setRenderColors(BlockCounterClient.getInstance().getConfig());
        this.quadBuffer = context.getVertexConsumerProvider().getBuffer(this.quadLayer);
    }

    @Override
    public void addSolid(BlockCounterWorldRenderContext context, Vec3d pos) {

        if (context.getMatrixStack() != null) {

            Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
            Vector3f posInCam = pos.toVector3f().sub(cameraPos.toVector3f());

            Matrix4f tranMatrix = context.getMatrixStack().peek().getPositionMatrix();

            Vector3f transformPos = new Vector3f();

            tranMatrix.transformPosition(posInCam.x, posInCam.y, posInCam.z, transformPos);

            this.addSolidBlockToBuffer(tranMatrix, transformPos);
        }

    }

    @Override
    public void addEdged(BlockCounterWorldRenderContext context, Vec3d pos) {
        if (context.getMatrixStack() != null) {

            Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
            Vector3f posInCam = pos.toVector3f().sub(cameraPos.toVector3f());

            Matrix4f tranMatrix = context.getMatrixStack().peek().getPositionMatrix();

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
        this.quadBuffer.vertex(tranMatrix, back_br.x, back_br.y, back_br.z).color(this.renderColor.getRGB())
                .normal(0, 0, -1).texture(0, 1).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);
        this.quadBuffer.vertex(tranMatrix, back_bl.x, back_bl.y, back_bl.z).color(this.renderColor.getRGB())
                .normal(0, 0, -1).texture(1, 1).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);
        this.quadBuffer.vertex(tranMatrix, back_tl.x, back_tl.y, back_tl.z).color(this.renderColor.getRGB())
                .normal(0, 0, -1).texture(1, 0).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);
        this.quadBuffer.vertex(tranMatrix, back_tr.x, back_tr.y, back_tr.z).color(this.renderColor.getRGB())
                .normal(0, 0, -1).texture(0, 0).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);

        // front face
        this.quadBuffer.vertex(tranMatrix, front_bl.x, front_bl.y, front_bl.z).color(this.renderColor.getRGB())
                .normal(0, 0, 1).texture(0, 1).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);
        this.quadBuffer.vertex(tranMatrix, front_br.x, front_br.y, front_br.z).color(this.renderColor.getRGB())
                .normal(0, 0, 1).texture(1, 1).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);
        this.quadBuffer.vertex(tranMatrix, front_tr.x, front_tr.y, front_tr.z).color(this.renderColor.getRGB())
                .normal(0, 0, 1).texture(1, 0).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);
        this.quadBuffer.vertex(tranMatrix, front_tl.x, front_tl.y, front_tl.z).color(this.renderColor.getRGB())
                .normal(0, 0, 1).texture(0, 0).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);

        // left face
        this.quadBuffer.vertex(tranMatrix, back_bl.x, back_bl.y, back_bl.z).color(this.renderColor.getRGB())
                .normal(-1, 0, 0).texture(0, 1).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);
        this.quadBuffer.vertex(tranMatrix, front_bl.x, front_bl.y, front_bl.z).color(this.renderColor.getRGB())
                .normal(-1, 0, 0).texture(1, 1).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);
        this.quadBuffer.vertex(tranMatrix, front_tl.x, front_tl.y, front_tl.z).color(this.renderColor.getRGB())
                .normal(-1, 0, 0).texture(1, 0).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);
        this.quadBuffer.vertex(tranMatrix, back_tl.x, back_tl.y, back_tl.z).color(this.renderColor.getRGB())
                .normal(-1, 0, 0).texture(0, 0).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);

        // right face
        this.quadBuffer.vertex(tranMatrix, front_br.x, front_br.y, front_br.z).color(this.renderColor.getRGB())
                .normal(1, 0, 0).texture(0, 1).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);
        this.quadBuffer.vertex(tranMatrix, back_br.x, back_br.y, back_br.z).color(this.renderColor.getRGB())
                .normal(1, 0, 0).texture(1, 1).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);
        this.quadBuffer.vertex(tranMatrix, back_tr.x, back_tr.y, back_tr.z).color(this.renderColor.getRGB())
                .normal(1, 0, 0).texture(1, 0).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);
        this.quadBuffer.vertex(tranMatrix, front_tr.x, front_tr.y, front_tr.z).color(this.renderColor.getRGB())
                .normal(1, 0, 0).texture(0, 0).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);

        // top face
        this.quadBuffer.vertex(tranMatrix, front_tl.x, front_tl.y, front_tl.z).color(this.renderColor.getRGB())
                .normal(0, 1, 0).texture(0, 1).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);
        this.quadBuffer.vertex(tranMatrix, front_tr.x, front_tr.y, front_tr.z).color(this.renderColor.getRGB())
                .normal(0, 1, 0).texture(1, 1).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);
        this.quadBuffer.vertex(tranMatrix, back_tr.x, back_tr.y, back_tr.z).color(this.renderColor.getRGB())
                .normal(0, 1, 0).texture(1, 0).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);
        this.quadBuffer.vertex(tranMatrix, back_tl.x, back_tl.y, back_tl.z).color(this.renderColor.getRGB())
                .normal(0, 1, 0).texture(0, 0).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);

        // bottom face
        this.quadBuffer.vertex(tranMatrix, back_bl.x, back_bl.y, back_bl.z).color(this.renderColor.getRGB())
                .normal(0, -1, 0).texture(0, 1).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);
        this.quadBuffer.vertex(tranMatrix, back_br.x, back_br.y, back_br.z).color(this.renderColor.getRGB())
                .normal(0, -1, 0).texture(1, 1).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);
        this.quadBuffer.vertex(tranMatrix, front_br.x, front_br.y, front_br.z).color(this.renderColor.getRGB())
                .normal(0, -1, 0).texture(1, 0).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);
        this.quadBuffer.vertex(tranMatrix, front_bl.x, front_bl.y, front_bl.z).color(this.renderColor.getRGB())
                .normal(0, -1, 0).texture(0, 0).light(0xF000F0).overlay(OverlayTexture.DEFAULT_UV);
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
        this.lineBuffer.vertex(tranMatrix, back_br.x, back_br.y, back_br.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);
        this.lineBuffer.vertex(tranMatrix, back_bl.x, back_bl.y, back_bl.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);
        // back right
        this.lineBuffer.vertex(tranMatrix, back_bl.x, back_bl.y, back_bl.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);
        this.lineBuffer.vertex(tranMatrix, back_tl.x, back_tl.y, back_tl.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);
        // back top
        this.lineBuffer.vertex(tranMatrix, back_tl.x, back_tl.y, back_tl.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);
        this.lineBuffer.vertex(tranMatrix, back_tr.x, back_tr.y, back_tr.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);
        // back left
        this.lineBuffer.vertex(tranMatrix, back_tr.x, back_tr.y, back_tr.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);
        this.lineBuffer.vertex(tranMatrix, back_br.x, back_br.y, back_br.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);

        // front bottom
        this.lineBuffer.vertex(tranMatrix, front_bl.x, front_bl.y, front_bl.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);
        this.lineBuffer.vertex(tranMatrix, front_br.x, front_br.y, front_br.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);
        // front right
        this.lineBuffer.vertex(tranMatrix, front_br.x, front_br.y, front_br.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);
        this.lineBuffer.vertex(tranMatrix, front_tr.x, front_tr.y, front_tr.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);
        // front top
        this.lineBuffer.vertex(tranMatrix, front_tr.x, front_tr.y, front_tr.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);
        this.lineBuffer.vertex(tranMatrix, front_tl.x, front_tl.y, front_tl.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);
        // front left
        this.lineBuffer.vertex(tranMatrix, front_tl.x, front_tl.y, front_tl.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);
        this.lineBuffer.vertex(tranMatrix, front_bl.x, front_bl.y, front_bl.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);

        // left bottom
        this.lineBuffer.vertex(tranMatrix, back_bl.x, back_bl.y, back_bl.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);
        this.lineBuffer.vertex(tranMatrix, front_bl.x, front_bl.y, front_bl.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);
        // left top
        this.lineBuffer.vertex(tranMatrix, back_tl.x, back_tl.y, back_tl.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);
        this.lineBuffer.vertex(tranMatrix, front_tl.x, front_tl.y, front_tl.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);

        // right bottom
        this.lineBuffer.vertex(tranMatrix, back_br.x, back_br.y, back_br.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);
        this.lineBuffer.vertex(tranMatrix, front_br.x, front_br.y, front_br.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);
        // right top
        this.lineBuffer.vertex(tranMatrix, back_tr.x, back_tr.y, back_tr.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);
        this.lineBuffer.vertex(tranMatrix, front_tr.x, front_tr.y, front_tr.z).color(this.edgeColor.getRGB()).normal(1, 1, 1);
    }
}