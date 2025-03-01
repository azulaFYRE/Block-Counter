package azula.blockcounter.rendering;

import azula.blockcounter.ActivationMethod;
import azula.blockcounter.config.BlockCounterModMenuConfig;
import azula.blockcounter.config.RenderType;
import azula.blockcounter.util.BlockCalculations;
import me.x150.renderer.render.Renderer3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;

import java.awt.Color;


public class BlockRenderingService {

    private Color renderColor;
    private Color edgeColor;
    private RenderType renderType;

    public void setRenderColors(BlockCounterModMenuConfig config) {
        int renderRGB = config.renderColor;
        int edgeRGB = config.edgeColor;
        int a = config.alpha;

        // Convert to rgba so we don't have to store alpha separately
        int renderRGBA = (a << 24) | (renderRGB & 0x00FFFFFF);
        int edgeRGBA = (a << 24) | (edgeRGB & 0x00FFFFFF);

        this.renderColor = new Color(renderRGBA, true);
        this.edgeColor = new Color(edgeRGBA, true);
        this.renderType = config.renderType;

        Renderer3d.renderThroughWalls();
    }

    public void renderStandingSelection(MatrixStack stack, Vec3d firstPos, BlockCounterModMenuConfig config) {

        if (firstPos != null) {
            assert MinecraftClient.getInstance().player != null;

            Vec3d playerPos = MinecraftClient.getInstance().player.getPos();
            BlockPos blockPosFirst = BlockPos.ofFloored(firstPos);
            BlockPos blockPosPlayer = BlockPos.ofFloored(playerPos);

            Vec3d fixedFirst = new Vec3d(blockPosFirst.getX(), blockPosFirst.getY(), blockPosFirst.getZ());
            Vec3d toRender = new Vec3d(blockPosPlayer.getX(), blockPosPlayer.getY() - 1, blockPosPlayer.getZ());

            this.renderLine(config, stack, fixedFirst, toRender, false);
        }
    }

    public void renderClickSelection(MatrixStack stack, Vec3d firstPos, BlockCounterModMenuConfig config) {

        if (firstPos != null) {
            Vec3d crosshairBlock = this.getCrosshairBlockPos();

            if (crosshairBlock != null) {
                this.renderLine(config, stack, firstPos, crosshairBlock, true);
            }
        }

    }

    public void renderQuad(MatrixStack stack, BlockPos lockPos, BlockCounterModMenuConfig config) {

        Vec3d offset = new Vec3d(ImGuiService.xOffset[0], ImGuiService.yOffset[0], ImGuiService.zOffset[0]);

        BlockPos renderBlockPos;
        if (lockPos == null) {
            // First pos is either from where player is standing or where they are looking
            if (config.activationMethod.equals(ActivationMethod.STANDING)) {
                renderBlockPos = BlockPos.ofFloored(
                        MinecraftClient.getInstance().player.getPos()
                                .subtract(new Vec3d(0, 1, 0)));
            } else {
                Vec3d crosshairPos = getCrosshairBlockPos();
                if (crosshairPos == null) return;
                renderBlockPos = BlockPos.ofFloored(crosshairPos);

            }
        } else {
            renderBlockPos = lockPos;
        }


        int length = ImGuiService.length[0];
        int width = ImGuiService.width[0];
        int height = ImGuiService.height[0];

        Vec3d dimensions = new Vec3d(length, height, width);

        this.setRenderColors(config);

        Vec3d renderPos = new Vec3d(renderBlockPos.getX(), renderBlockPos.getY(), renderBlockPos.getZ());
        renderPos = renderPos.add(offset);

        if (this.renderType.equals(RenderType.SOLID)) {
            Renderer3d.renderFilled(stack, this.renderColor, renderPos, dimensions);
        } else if (this.renderType.equals(RenderType.EDGE_ONLY)) {
            Renderer3d.renderOutline(stack, this.edgeColor, renderPos, dimensions);
        } else {
            Renderer3d.renderEdged(stack, this.renderColor, this.edgeColor, renderPos, dimensions);
        }
    }

    public Vec3d getCrosshairBlockPos() {

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        // Pretty sure this should technically never happen, just shutting up the IDE for next methods
        assert player != null;
        assert client.world != null;

        Vec3d playerPos = player.getCameraPosVec(1.0f);
        Vec3d viewDir = player.getRotationVec(1.0f);

        int MAX_DIST = 5;

        // Raycast to where the player is currently looking
        BlockHitResult rayCastResult = client.world.raycast(new RaycastContext(
                playerPos,
                playerPos.add(viewDir.x * MAX_DIST, viewDir.y * MAX_DIST, viewDir.z * MAX_DIST),
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                player
        ));

        if (rayCastResult != null) {
            return new Vec3d(
                    rayCastResult.getBlockPos().getX(),
                    rayCastResult.getBlockPos().getY(),
                    rayCastResult.getBlockPos().getZ()
            );
        }

        return null;
    }

    private Vec3d toIntVec(Vec3d vec) {
        return new Vec3d((int) vec.x, (int) vec.y, (int) vec.z);
    }

    private void renderLine(BlockCounterModMenuConfig config, MatrixStack stack, Vec3d firstPos, Vec3d secondPos, boolean isClick) {

        this.setRenderColors(config);

        Vec3d firstPosInt = this.toIntVec(firstPos);
        Vec3d renderPos = new Vec3d(firstPosInt.x, firstPosInt.y - (isClick ? 0 : 1), firstPosInt.z);

        Vec3d dimensions = this.findDimensions(renderPos, secondPos);

        int clickOffset = isClick ? 1 : 0;
        int standOffset = isClick ? 0 : 1;

        // Rendering library seems to have trouble with negative dimensions, so instead we will make the dimension
        // positive while translating the initial render position in order to render using positive dimensions
        if (dimensions.x < 0) {
            double absX = Math.abs(dimensions.x);
            int newFirstX = (int) (firstPosInt.x - absX);

            // Update the dimensions to be a positive offset while shifting the render position back
            dimensions = new Vec3d(absX + 1, dimensions.y, dimensions.z);
            renderPos = new Vec3d(newFirstX, renderPos.y, renderPos.z);
        } else if (dimensions.y < 0) {
            double absY = Math.abs(dimensions.y);
            int newFirstY = (int) (firstPosInt.y - (absY + standOffset));

            dimensions = new Vec3d(dimensions.x, absY + clickOffset + standOffset, dimensions.z);
            renderPos = new Vec3d(renderPos.x, newFirstY, renderPos.z);
        } else if (dimensions.z < 0) {
            double absZ = Math.abs(dimensions.z);
            int newFirstZ = (int) (firstPosInt.z - absZ);

            dimensions = new Vec3d(dimensions.x, dimensions.y, absZ + 1);
            renderPos = new Vec3d(renderPos.x, renderPos.y, newFirstZ);
        }

        if (dimensions.y > 1 && !isClick) {
            dimensions = new Vec3d(dimensions.x, dimensions.y - 1, dimensions.z);
            renderPos = new Vec3d(renderPos.x, renderPos.y + 1, renderPos.z);
        }

        if (this.renderType.equals(RenderType.SOLID)) {
            Renderer3d.renderFilled(stack, this.renderColor, renderPos, dimensions);
        } else if (this.renderType.equals(RenderType.EDGE_ONLY)) {
            Renderer3d.renderOutline(stack, this.edgeColor, renderPos, dimensions);
        } else {
            Renderer3d.renderEdged(stack, this.renderColor, this.edgeColor, renderPos, dimensions);
        }

    }

    private Vec3d findDimensions(Vec3d firstPos, Vec3d secondPos) {

        Vec3d firstPosInt = new Vec3d((int) firstPos.x, (int) firstPos.y, (int) firstPos.z);
        Vec3d secondPosInt = new Vec3d((int) secondPos.x, (int) secondPos.y, (int) secondPos.z);

        int diffX = (int) (secondPosInt.x - firstPosInt.x);
        int diffY = (int) (secondPosInt.y - firstPosInt.y);
        int diffZ = (int) (secondPosInt.z - firstPosInt.z);

        Direction.Axis maxDiff = BlockCalculations.findLargestAxisDiff(firstPos, secondPos);

        double x = 1;
        double y = 1;
        double z = 1;

        if (maxDiff == Direction.Axis.X) {
            x = diffX == 0 ? x : (diffX > 0 ? diffX + 1 : diffX);
        } else if (maxDiff == Direction.Axis.Y) {
            y = diffY == 0 ? y : (diffY > 0 ? diffY + 1 : diffY);
        } else {
            z = diffZ == 0 ? z : (diffZ > 0 ? diffZ + 1 : diffZ);
        }

        return new Vec3d(x, y, z);
    }

}
