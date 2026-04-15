package azula.blockcounter.rendering;

import azula.blockcounter.BlockCounterClient;
import azula.blockcounter.config.BlockCounterModMenuConfig;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.loader.api.FabricLoader;
//import net.irisshaders.iris.api.v0.IrisApi;
//import net.irisshaders.iris.api.v0.IrisProgram;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class RenderingServiceImpl implements RenderingService {

    private Color renderColor;
    private Color edgeColor;

    private static RenderPipeline QUAD_PIPELINE = null;
    private static RenderPipeline LINE_PIPELINE = null;

    private static final ByteBufferBuilder quadAllocator = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final ByteBufferBuilder lineAllocator = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);

    private BufferBuilder quadBuffer;
    private BufferBuilder lineBuffer;

    private MappableRingBuffer quadVertexBuffer;
    private MappableRingBuffer lineVertexBuffer;

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();

    private final boolean usingIris;

    private enum BufferType {
        QUAD,
        LINE
    }

    public RenderingServiceImpl() {
        this.usingIris = FabricLoader.getInstance().isModLoaded("iris");
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
    public void fillLineBuffer(LevelExtractionContext context, List<Vec3> pos) {
        this.setRenderColors(BlockCounterClient.getInstance().getConfig());

        if (LINE_PIPELINE == null) {
            LINE_PIPELINE = RenderPipelines.register(
                    RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                            .withLocation(Identifier.fromNamespaceAndPath(BlockCounterClient.MOD_ID, "pipeline/line"))
                            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                            .build()
            );

//            if (this.usingIris) {
//                IrisApi.getInstance().assignPipeline(LINE_PIPELINE, IrisProgram.LINES);
//            }
        }

        if (this.lineBuffer == null) {
            this.lineBuffer = new BufferBuilder(lineAllocator, LINE_PIPELINE.getVertexFormatMode(), LINE_PIPELINE.getVertexFormat());
        }

        Vec3 camPos = context.camera().position();

        for (Vec3 p : pos) {
            Vector3f inCamPos = p.subtract(camPos).toVector3f();
            this.addEdgedBlockToBuffer(inCamPos);
        }
    }

    @Override
    public void fillQuadBuffer(LevelExtractionContext context, List<Vec3> pos) {
        this.setRenderColors(BlockCounterClient.getInstance().getConfig());

        if (QUAD_PIPELINE == null) {
            QUAD_PIPELINE = RenderPipelines.register(
                    RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                            .withLocation(Identifier.fromNamespaceAndPath(BlockCounterClient.MOD_ID, "pipeline/quad"))
                            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
                            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                            .build()
            );

//            if (this.usingIris) {
//                IrisApi.getInstance().assignPipeline(QUAD_PIPELINE, IrisProgram.BASIC);
//            }
        }

        if (this.quadBuffer == null) {
            this.quadBuffer = new BufferBuilder(quadAllocator, QUAD_PIPELINE.getVertexFormatMode(), QUAD_PIPELINE.getVertexFormat());
        }

        Vec3 camPos = context.camera().position();

        for (Vec3 p : pos) {
            Vector3f inCamPos = p.subtract(camPos).toVector3f();
            this.addSolidBlockToBuffer(inCamPos);
        }
    }

    private void addSolidBlockToBuffer(Vector3f pos) {
        Vector3f back_bl = new Vector3f(pos.x, pos.y, pos.z);
        Vector3f back_tl = new Vector3f(pos.x, pos.y + 1, pos.z);
        Vector3f back_tr = new Vector3f(pos.x + 1, pos.y + 1, pos.z);
        Vector3f back_br = new Vector3f(pos.x + 1, pos.y, pos.z);

        Vector3f front_bl = new Vector3f(pos.x, pos.y, pos.z + 1);
        Vector3f front_tl = new Vector3f(pos.x, pos.y + 1, pos.z + 1);
        Vector3f front_tr = new Vector3f(pos.x + 1, pos.y + 1, pos.z + 1);
        Vector3f front_br = new Vector3f(pos.x + 1, pos.y, pos.z + 1);

        // back face
        this.quadBuffer.addVertex(back_br.x, back_br.y, back_br.z).setColor(this.renderColor.getRGB());
        this.quadBuffer.addVertex(back_bl.x, back_bl.y, back_bl.z).setColor(this.renderColor.getRGB());
        this.quadBuffer.addVertex(back_tl.x, back_tl.y, back_tl.z).setColor(this.renderColor.getRGB());
        this.quadBuffer.addVertex(back_tr.x, back_tr.y, back_tr.z).setColor(this.renderColor.getRGB());

        // front face
        this.quadBuffer.addVertex(front_bl.x, front_bl.y, front_bl.z).setColor(this.renderColor.getRGB());
        this.quadBuffer.addVertex(front_br.x, front_br.y, front_br.z).setColor(this.renderColor.getRGB());
        this.quadBuffer.addVertex(front_tr.x, front_tr.y, front_tr.z).setColor(this.renderColor.getRGB());
        this.quadBuffer.addVertex(front_tl.x, front_tl.y, front_tl.z).setColor(this.renderColor.getRGB());

        // left face
        this.quadBuffer.addVertex(back_bl.x, back_bl.y, back_bl.z).setColor(this.renderColor.getRGB());
        this.quadBuffer.addVertex(front_bl.x, front_bl.y, front_bl.z).setColor(this.renderColor.getRGB());
        this.quadBuffer.addVertex(front_tl.x, front_tl.y, front_tl.z).setColor(this.renderColor.getRGB());
        this.quadBuffer.addVertex(back_tl.x, back_tl.y, back_tl.z).setColor(this.renderColor.getRGB());

        // right face
        this.quadBuffer.addVertex(front_br.x, front_br.y, front_br.z).setColor(this.renderColor.getRGB());
        this.quadBuffer.addVertex(back_br.x, back_br.y, back_br.z).setColor(this.renderColor.getRGB());
        this.quadBuffer.addVertex(back_tr.x, back_tr.y, back_tr.z).setColor(this.renderColor.getRGB());
        this.quadBuffer.addVertex(front_tr.x, front_tr.y, front_tr.z).setColor(this.renderColor.getRGB());

        // top face
        this.quadBuffer.addVertex(front_tl.x, front_tl.y, front_tl.z).setColor(this.renderColor.getRGB());
        this.quadBuffer.addVertex(front_tr.x, front_tr.y, front_tr.z).setColor(this.renderColor.getRGB());
        this.quadBuffer.addVertex(back_tr.x, back_tr.y, back_tr.z).setColor(this.renderColor.getRGB());
        this.quadBuffer.addVertex(back_tl.x, back_tl.y, back_tl.z).setColor(this.renderColor.getRGB());

        // bottom face
        this.quadBuffer.addVertex(back_bl.x, back_bl.y, back_bl.z).setColor(this.renderColor.getRGB());
        this.quadBuffer.addVertex(back_br.x, back_br.y, back_br.z).setColor(this.renderColor.getRGB());
        this.quadBuffer.addVertex(front_br.x, front_br.y, front_br.z).setColor(this.renderColor.getRGB());
        this.quadBuffer.addVertex(front_bl.x, front_bl.y, front_bl.z).setColor(this.renderColor.getRGB());
    }

    private void addEdgedBlockToBuffer(Vector3f pos) {
        Vector3f back_bl = new Vector3f(pos.x, pos.y, pos.z);
        Vector3f back_tl = new Vector3f(pos.x, pos.y + 1, pos.z);
        Vector3f back_tr = new Vector3f(pos.x + 1, pos.y + 1, pos.z);
        Vector3f back_br = new Vector3f(pos.x + 1, pos.y, pos.z);

        Vector3f front_bl = new Vector3f(pos.x, pos.y, pos.z + 1);
        Vector3f front_tl = new Vector3f(pos.x, pos.y + 1, pos.z + 1);
        Vector3f front_tr = new Vector3f(pos.x + 1, pos.y + 1, pos.z + 1);
        Vector3f front_br = new Vector3f(pos.x + 1, pos.y, pos.z + 1);

        // back bottom
        this.lineBuffer.addVertex(back_br.x, back_br.y, back_br.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        this.lineBuffer.addVertex(back_bl.x, back_bl.y, back_bl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        // back right
        this.lineBuffer.addVertex(back_bl.x, back_bl.y, back_bl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        this.lineBuffer.addVertex(back_tl.x, back_tl.y, back_tl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        // back top
        this.lineBuffer.addVertex(back_tl.x, back_tl.y, back_tl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        this.lineBuffer.addVertex(back_tr.x, back_tr.y, back_tr.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        // back left
        this.lineBuffer.addVertex(back_tr.x, back_tr.y, back_tr.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        this.lineBuffer.addVertex(back_br.x, back_br.y, back_br.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);

        // front bottom
        this.lineBuffer.addVertex(front_bl.x, front_bl.y, front_bl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        this.lineBuffer.addVertex(front_br.x, front_br.y, front_br.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        // front right
        this.lineBuffer.addVertex(front_br.x, front_br.y, front_br.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        this.lineBuffer.addVertex(front_tr.x, front_tr.y, front_tr.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        // front top
        this.lineBuffer.addVertex(front_tr.x, front_tr.y, front_tr.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        this.lineBuffer.addVertex(front_tl.x, front_tl.y, front_tl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        // front left
        this.lineBuffer.addVertex(front_tl.x, front_tl.y, front_tl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        this.lineBuffer.addVertex(front_bl.x, front_bl.y, front_bl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);

        // left bottom
        this.lineBuffer.addVertex(back_bl.x, back_bl.y, back_bl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        this.lineBuffer.addVertex(front_bl.x, front_bl.y, front_bl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        // left top
        this.lineBuffer.addVertex(back_tl.x, back_tl.y, back_tl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        this.lineBuffer.addVertex(front_tl.x, front_tl.y, front_tl.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);

        // right bottom
        this.lineBuffer.addVertex(back_br.x, back_br.y, back_br.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        this.lineBuffer.addVertex(front_br.x, front_br.y, front_br.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        // right top
        this.lineBuffer.addVertex(back_tr.x, back_tr.y, back_tr.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
        this.lineBuffer.addVertex(front_tr.x, front_tr.y, front_tr.z).setColor(this.edgeColor.getRGB()).setNormal(1, 1, 1).setLineWidth(1.0f);
    }

    @Override
    public void renderQuadBuffer() {
        if (this.quadBuffer == null) return;

        try (MeshData builtQuadBuffer = this.quadBuffer.buildOrThrow()) {
            MeshData.DrawState quadDrawState = builtQuadBuffer.drawState();
            VertexFormat quadFormat = quadDrawState.format();

            GpuBuffer vertices = this.upload(quadDrawState, quadFormat, builtQuadBuffer, BufferType.QUAD);

            draw(Minecraft.getInstance(), QUAD_PIPELINE, builtQuadBuffer, quadDrawState, vertices, quadFormat, BufferType.QUAD);

            this.quadVertexBuffer.rotate();
            this.quadBuffer = null;

        } catch (Exception e) {
            System.err.println("[Block-Counter] Failed to build quad buffer: " + e.getMessage());
        }
    }

    @Override
    public void renderLineBuffer() {
        if (this.lineBuffer == null) return;

        try (MeshData builtLineBuffer = this.lineBuffer.buildOrThrow()) {
            MeshData.DrawState lineDrawState = builtLineBuffer.drawState();
            VertexFormat lineFormat = lineDrawState.format();

            GpuBuffer vertices = this.upload(lineDrawState, lineFormat, builtLineBuffer, BufferType.LINE);

            draw(Minecraft.getInstance(), LINE_PIPELINE, builtLineBuffer, lineDrawState, vertices, lineFormat, BufferType.LINE);

            this.lineVertexBuffer.rotate();
            this.lineBuffer = null;

        } catch (Exception e) {
            System.err.println("[Block-Counter] Failed to build line buffer: " + e.getMessage());
        }
    }

    private GpuBuffer upload(MeshData.DrawState drawState, VertexFormat format, MeshData builtBuffer, BufferType type) {
        int vertexBufferSize = drawState.vertexCount() * format.getVertexSize();

        if (type == BufferType.LINE) {
            // Initialize or resize the vertex buffer as needed
            if (this.lineVertexBuffer == null || this.lineVertexBuffer.size() < vertexBufferSize) {
                if (this.lineVertexBuffer != null) {
                    this.lineVertexBuffer.close();
                }

                this.lineVertexBuffer = new MappableRingBuffer(() -> BlockCounterClient.MOD_ID + " line render pipeline", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
            }
        } else {
            if (this.quadVertexBuffer == null || this.quadVertexBuffer.size() < vertexBufferSize) {
                if (this.quadVertexBuffer != null) {
                    this.quadVertexBuffer.close();
                }

                this.quadVertexBuffer = new MappableRingBuffer(() -> BlockCounterClient.MOD_ID + " quad render pipeline", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
            }
        }

        MappableRingBuffer vertexBuffer = type == BufferType.LINE ? this.lineVertexBuffer : this.quadVertexBuffer;

        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(vertexBuffer.currentBuffer().slice(0, builtBuffer.vertexBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
        }

        return vertexBuffer.currentBuffer();
    }

    private static void draw(Minecraft client, RenderPipeline pipeline, MeshData builtBuffer, MeshData.DrawState drawParameters, GpuBuffer vertices, VertexFormat format, BufferType type) {
        GpuBuffer indices;
        VertexFormat.IndexType indexType;

        ByteBufferBuilder allocator = type == BufferType.LINE ? lineAllocator : quadAllocator;

        if (pipeline.getVertexFormatMode() == VertexFormat.Mode.QUADS) {
            // Sort the quads if there is translucency
            builtBuffer.sortQuads(allocator, RenderSystem.getProjectionType().vertexSorting());
            // Upload the index buffer
            ByteBuffer rawIndexBuffer = Objects.requireNonNull(builtBuffer.indexBuffer());
            indices = RenderSystem.getDevice().createBuffer(() -> "Block Counter Immediate Index Buffer",
                    GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                    rawIndexBuffer.remaining());
            indexType = builtBuffer.drawState().indexType();

            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(indices.slice(), rawIndexBuffer);
        } else {
            // Use the general shape index buffer for non-quad draw modes
            RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
            indices = shapeIndexBuffer.getBuffer(drawParameters.indexCount());
            indexType = shapeIndexBuffer.type();
        }

        // Actually execute the draw
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrixCopy(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> BlockCounterClient.MOD_ID + (type == BufferType.LINE ? " line" : " quad") + " render pipeline drawing",
                        Objects.requireNonNull(client.gameRenderer.mainRenderTarget().getColorTextureView()),
                        OptionalInt.empty(),
                        client.gameRenderer.mainRenderTarget().getDepthTextureView(),
                        OptionalDouble.empty())) {
            renderPass.setPipeline(pipeline);

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            // Bind texture if applicable:
            // Sampler0 is used for texture inputs in vertices
            // renderPass.bindTexture("Sampler0", textureSetup.texure0(), textureSetup.sampler0());

            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, indexType);

            // The base vertex is the starting index when we copied the data into the vertex buffer divided by vertex size
            //noinspection ConstantValue
            renderPass.drawIndexed(0 / format.getVertexSize(), 0, drawParameters.indexCount(), 1);
        }

        builtBuffer.close();
    }
}