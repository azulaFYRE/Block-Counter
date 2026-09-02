package azula.blockcounter.rendering;

import azula.blockcounter.BlockCounterClient;
import azula.blockcounter.config.BlockCounterModMenuConfig;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;


import java.awt.Color;
import java.util.Optional;

public class RenderingServiceImpl implements RenderingService {

    private Color renderColor;
    private Color edgeColor;

    private static final int BINDING_INDEX = 0;

    private final static RenderPipeline QUAD_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(BlockCounterClient.MOD_ID, "pipeline/quad"))
                    .withVertexBinding(BINDING_INDEX, DefaultVertexFormat.POSITION_COLOR)
                    .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                    .withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_ALL))
                    .build()
    );

    private final static RenderPipeline LINE_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(BlockCounterClient.MOD_ID, "pipeline/line"))
                    .withVertexBinding(BINDING_INDEX, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH)
                    .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                    .withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_ALL))
                    .build()
    );

    private static final StagedVertexBuffer quadStagedBuffer = new StagedVertexBuffer(() -> "Quad Buffer", RenderType.SMALL_BUFFER_SIZE);
    private static final StagedVertexBuffer lineStagedBuffer = new StagedVertexBuffer(() -> "Line Buffer", RenderType.SMALL_BUFFER_SIZE);

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();

    public RenderingServiceImpl() {
        boolean usingIris = FabricLoader.getInstance().isModLoaded("iris");
//        if (usingIris) {
//            IrisApi.getInstance().assignPipeline(LINE_PIPELINE, IrisProgram.LINES);
//            IrisApi.getInstance().assignPipeline(QUAD_PIPELINE, IrisProgram.BASIC);
//        }
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

    @Override
    public void renderQuadBuffer(LevelRenderContext context, BlockRenderingService.BlockCounterRenderState renderState) {
        VertexFormat quadFormat = QUAD_PIPELINE.getVertexFormatBinding(BINDING_INDEX);

        assert quadFormat != null;

        PrimitiveTopology primitive = QUAD_PIPELINE.getPrimitiveTopology();
        StagedVertexBuffer.Draw draw = quadStagedBuffer.appendDraw(quadFormat, primitive, RenderSystem.getProjectionType().vertexSorting());

        this.renderQuads(context, renderState, draw);

        quadStagedBuffer.upload();

        StagedVertexBuffer.ExecuteInfo execution = quadStagedBuffer.getExecuteInfo(draw);

        if (execution != null) {
            draw(Minecraft.getInstance(), execution, QUAD_PIPELINE);
        }

        quadStagedBuffer.endFrame();
    }

    @Override
    public void renderLineBuffer(LevelRenderContext context, BlockRenderingService.BlockCounterRenderState renderState) {
        VertexFormat lineFormat = LINE_PIPELINE.getVertexFormatBinding(BINDING_INDEX);

        assert lineFormat != null;

        PrimitiveTopology primitive = LINE_PIPELINE.getPrimitiveTopology();
        StagedVertexBuffer.Draw draw = lineStagedBuffer.appendDraw(lineFormat, primitive, null);

        this.renderLines(context, renderState, draw);

        lineStagedBuffer.upload();

        StagedVertexBuffer.ExecuteInfo execution = lineStagedBuffer.getExecuteInfo(draw);

        if (execution != null) {
            draw(Minecraft.getInstance(), execution, LINE_PIPELINE);
        }

        lineStagedBuffer.endFrame();
    }

    private void renderQuads(LevelRenderContext context, BlockRenderingService.BlockCounterRenderState renderState, StagedVertexBuffer.Draw draw) {
        Vec3 camera = context.levelState().cameraRenderState.pos;

        final VertexConsumer builder = quadStagedBuffer.getVertexBuilder(draw);

        for (Vec3 pos : renderState.positions()) {
            this.addSolidBlockToBuffer(builder, pos.subtract(camera).toVector3f());
        }
    }

    private void renderLines(LevelRenderContext context, BlockRenderingService.BlockCounterRenderState renderState, StagedVertexBuffer.Draw draw) {
        Vec3 camera = context.levelState().cameraRenderState.pos;

        final VertexConsumer builder = lineStagedBuffer.getVertexBuilder(draw);

        for (Vec3 pos : renderState.positions()) {
            this.addEdgedBlockToBuffer(builder, pos.subtract(camera).toVector3f());
        }
    }

    private void addSolidBlockToBuffer(VertexConsumer buffer, Vector3f pos) {
        Vector3f back_bl = new Vector3f(pos.x, pos.y, pos.z);
        Vector3f back_tl = new Vector3f(pos.x, pos.y + 1, pos.z);
        Vector3f back_tr = new Vector3f(pos.x + 1, pos.y + 1, pos.z);
        Vector3f back_br = new Vector3f(pos.x + 1, pos.y, pos.z);

        Vector3f front_bl = new Vector3f(pos.x, pos.y, pos.z + 1);
        Vector3f front_tl = new Vector3f(pos.x, pos.y + 1, pos.z + 1);
        Vector3f front_tr = new Vector3f(pos.x + 1, pos.y + 1, pos.z + 1);
        Vector3f front_br = new Vector3f(pos.x + 1, pos.y, pos.z + 1);

        // back face
        buffer.addVertex(back_br.x, back_br.y, back_br.z).setColor(this.renderColor.getRGB());
        buffer.addVertex(back_bl.x, back_bl.y, back_bl.z).setColor(this.renderColor.getRGB());
        buffer.addVertex(back_tl.x, back_tl.y, back_tl.z).setColor(this.renderColor.getRGB());
        buffer.addVertex(back_tr.x, back_tr.y, back_tr.z).setColor(this.renderColor.getRGB());

        // front face
        buffer.addVertex(front_bl.x, front_bl.y, front_bl.z).setColor(this.renderColor.getRGB());
        buffer.addVertex(front_br.x, front_br.y, front_br.z).setColor(this.renderColor.getRGB());
        buffer.addVertex(front_tr.x, front_tr.y, front_tr.z).setColor(this.renderColor.getRGB());
        buffer.addVertex(front_tl.x, front_tl.y, front_tl.z).setColor(this.renderColor.getRGB());

        // left face
        buffer.addVertex(back_bl.x, back_bl.y, back_bl.z).setColor(this.renderColor.getRGB());
        buffer.addVertex(front_bl.x, front_bl.y, front_bl.z).setColor(this.renderColor.getRGB());
        buffer.addVertex(front_tl.x, front_tl.y, front_tl.z).setColor(this.renderColor.getRGB());
        buffer.addVertex(back_tl.x, back_tl.y, back_tl.z).setColor(this.renderColor.getRGB());

        // right face
        buffer.addVertex(front_br.x, front_br.y, front_br.z).setColor(this.renderColor.getRGB());
        buffer.addVertex(back_br.x, back_br.y, back_br.z).setColor(this.renderColor.getRGB());
        buffer.addVertex(back_tr.x, back_tr.y, back_tr.z).setColor(this.renderColor.getRGB());
        buffer.addVertex(front_tr.x, front_tr.y, front_tr.z).setColor(this.renderColor.getRGB());

        // top face
        buffer.addVertex(front_tl.x, front_tl.y, front_tl.z).setColor(this.renderColor.getRGB());
        buffer.addVertex(front_tr.x, front_tr.y, front_tr.z).setColor(this.renderColor.getRGB());
        buffer.addVertex(back_tr.x, back_tr.y, back_tr.z).setColor(this.renderColor.getRGB());
        buffer.addVertex(back_tl.x, back_tl.y, back_tl.z).setColor(this.renderColor.getRGB());

        // bottom face
        buffer.addVertex(back_bl.x, back_bl.y, back_bl.z).setColor(this.renderColor.getRGB());
        buffer.addVertex(back_br.x, back_br.y, back_br.z).setColor(this.renderColor.getRGB());
        buffer.addVertex(front_br.x, front_br.y, front_br.z).setColor(this.renderColor.getRGB());
        buffer.addVertex(front_bl.x, front_bl.y, front_bl.z).setColor(this.renderColor.getRGB());
    }

    private void addEdgedBlockToBuffer(VertexConsumer buffer, Vector3f pos) {
        Vector3f back_bl = new Vector3f(pos.x, pos.y, pos.z);
        Vector3f back_tl = new Vector3f(pos.x, pos.y + 1, pos.z);
        Vector3f back_tr = new Vector3f(pos.x + 1, pos.y + 1, pos.z);
        Vector3f back_br = new Vector3f(pos.x + 1, pos.y, pos.z);

        Vector3f front_bl = new Vector3f(pos.x, pos.y, pos.z + 1);
        Vector3f front_tl = new Vector3f(pos.x, pos.y + 1, pos.z + 1);
        Vector3f front_tr = new Vector3f(pos.x + 1, pos.y + 1, pos.z + 1);
        Vector3f front_br = new Vector3f(pos.x + 1, pos.y, pos.z + 1);

        // back bottom
        buffer.addVertex(back_br.x, back_br.y, back_br.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        buffer.addVertex(back_bl.x, back_bl.y, back_bl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        // back right
        buffer.addVertex(back_bl.x, back_bl.y, back_bl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        buffer.addVertex(back_tl.x, back_tl.y, back_tl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        // back top
        buffer.addVertex(back_tl.x, back_tl.y, back_tl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        buffer.addVertex(back_tr.x, back_tr.y, back_tr.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        // back left
        buffer.addVertex(back_tr.x, back_tr.y, back_tr.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        buffer.addVertex(back_br.x, back_br.y, back_br.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);

        // front bottom
        buffer.addVertex(front_bl.x, front_bl.y, front_bl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        buffer.addVertex(front_br.x, front_br.y, front_br.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        // front right
        buffer.addVertex(front_br.x, front_br.y, front_br.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        buffer.addVertex(front_tr.x, front_tr.y, front_tr.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        // front top
        buffer.addVertex(front_tr.x, front_tr.y, front_tr.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        buffer.addVertex(front_tl.x, front_tl.y, front_tl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        // front left
        buffer.addVertex(front_tl.x, front_tl.y, front_tl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        buffer.addVertex(front_bl.x, front_bl.y, front_bl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);

        // left bottom
        buffer.addVertex(back_bl.x, back_bl.y, back_bl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        buffer.addVertex(front_bl.x, front_bl.y, front_bl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        // left top
        buffer.addVertex(back_tl.x, back_tl.y, back_tl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        buffer.addVertex(front_tl.x, front_tl.y, front_tl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);

        // right bottom
        buffer.addVertex(back_br.x, back_br.y, back_br.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        buffer.addVertex(front_br.x, front_br.y, front_br.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        // right top
        buffer.addVertex(back_tr.x, back_tr.y, back_tr.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        buffer.addVertex(front_tr.x, front_tr.y, front_tr.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
    }

    private static void draw(Minecraft client, StagedVertexBuffer.ExecuteInfo execution, RenderPipeline pipeline) {
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrixCopy(),
                COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

        RenderTarget mainTarget = client.gameRenderer.mainRenderTarget();
        GpuTextureView colorTexture = mainTarget.getColorTextureView();

        assert colorTexture != null;

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> BlockCounterClient.MOD_ID + " render pipeline rendering",
                        colorTexture, Optional.empty())) {
            renderPass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            renderPass.setVertexBuffer(0, execution.vertexBuffer().slice());
            renderPass.setIndexBuffer(execution.indexBuffer(), execution.indexType());

            renderPass.drawIndexed(execution.indexCount(), 1, execution.firstIndex(), execution.baseVertex(), 0);
        }
    }

    public static void close() {
        quadStagedBuffer.close();
        lineStagedBuffer.close();
    }
}