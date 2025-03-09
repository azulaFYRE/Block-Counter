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
import net.minecraft.world.RaycastContext;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BlockRenderingService {

    private Color renderColor;
    private Color edgeColor;
    private RenderType renderType;

    private Map<String, List<Vec3d>> circlePositions = new HashMap<>();
    private Map<Integer, List<Vec3d>> spherePositions = new HashMap<>();

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
    }

    public void renderStandingSelection(MatrixStack stack, Vec3d firstPos, BlockPos lockPos, BlockCounterModMenuConfig config) {

        if (firstPos != null) {
            assert MinecraftClient.getInstance().player != null;

            Vec3d playerPos = MinecraftClient.getInstance().player.getPos();
            BlockPos blockPosFirst = BlockPos.ofFloored(firstPos);
            BlockPos blockPosPlayer = BlockPos.ofFloored(playerPos);
            Vec3d toRender = Vec3d.of(blockPosPlayer);

            if (lockPos != null) {
                blockPosPlayer = lockPos;
                toRender = Vec3d.of(blockPosPlayer);
            }

            Vec3d fixedFirst = Vec3d.of(blockPosFirst);

            this.renderLine(config, stack, fixedFirst, toRender, false);
        }
    }

    public void renderClickSelection(MatrixStack stack, Vec3d firstPos, BlockPos lockPos, BlockCounterModMenuConfig config) {

        if (firstPos != null) {
            Vec3d secondPos = getCrosshairBlockPos();

            if (lockPos != null) {
                secondPos = Vec3d.of(lockPos);
            }

            if (secondPos != null) {
                this.renderLine(config, stack, firstPos, secondPos, true);
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

        Vec3d renderPos = Vec3d.of(renderBlockPos);
        renderPos = renderPos.add(offset);

        Vec3d blockDimension = new Vec3d(1, 1, 1);

        Vec3d toAddX = new Vec3d(1, 0, 0);
        Vec3d toAddY = new Vec3d(0, 1, 0);
        Vec3d toAddZ = new Vec3d(0, 0, 1);

        Vec3d bottomY = new Vec3d(renderPos.x, renderPos.y, renderPos.z);
        Vec3d topY = new Vec3d(renderPos.x, renderPos.y + dimensions.y - 1, renderPos.z);

        Vec3d frontX = new Vec3d(renderPos.x, renderPos.y + 1, renderPos.z);
        Vec3d backX = new Vec3d(renderPos.x + dimensions.x - 1, renderPos.y + 1, renderPos.z);

        Vec3d leftZ = new Vec3d(renderPos.x + 1, renderPos.y + 1, renderPos.z);
        Vec3d rightZ = new Vec3d(renderPos.x + 1, renderPos.y + 1, renderPos.z + dimensions.z - 1);

        Renderer3d.stopRenderThroughWalls();

        if (this.renderType.equals(RenderType.SOLID)) {

            // top and bottom
            Vec3d topBottomDims = new Vec3d(dimensions.x, 1, dimensions.z);
            Renderer3d.renderFilled(stack, this.renderColor, bottomY, topBottomDims);
            if (height > 1) Renderer3d.renderFilled(stack, this.renderColor, topY, topBottomDims);

            // front and back
            Vec3d frontBackDims = new Vec3d(1, dimensions.y - 2, dimensions.z);
            Renderer3d.renderFilled(stack, this.renderColor, frontX, frontBackDims);
            if (height > 1) Renderer3d.renderFilled(stack, this.renderColor, backX, frontBackDims);

            // left and right
            Vec3d leftRightDims = new Vec3d(dimensions.x - 2, dimensions.y - 2, 1);
            Renderer3d.renderFilled(stack, this.renderColor, leftZ, leftRightDims);
            if (height > 1) Renderer3d.renderFilled(stack, this.renderColor, rightZ, leftRightDims);
        } else {

            // top and bottom
            for (int z = 0; z < width; z++) {
                for (int x = 0; x < length; x++) {
                    if (this.renderType.equals(RenderType.EDGE_ONLY)) {
                        Renderer3d.renderOutline(stack, this.edgeColor, bottomY, blockDimension);
                        if (height > 1) Renderer3d.renderOutline(stack, this.edgeColor, topY, blockDimension);
                    } else {
                        Renderer3d.renderEdged(stack, this.renderColor, this.edgeColor, bottomY, blockDimension);
                        if (height > 1) Renderer3d.renderEdged(stack, this.renderColor, this.edgeColor, topY, blockDimension);
                    }

                    bottomY = bottomY.add(toAddX);
                    topY = topY.add(toAddX);
                }
                bottomY = new Vec3d(renderPos.x, bottomY.y, bottomY.z);
                bottomY = bottomY.add(toAddZ);
                topY = new Vec3d(renderPos.x, topY.y, topY.z);
                topY = topY.add(toAddZ);
            }

            // front and back
            for (int y = 0; y < height - 2; y++) {
                for (int z = 0; z < width; z++) {
                    if (this.renderType.equals(RenderType.EDGE_ONLY)) {
                        Renderer3d.renderOutline(stack, this.edgeColor, frontX, blockDimension);
                        if (length > 1) Renderer3d.renderOutline(stack, this.edgeColor, backX, blockDimension);
                    } else {
                        Renderer3d.renderEdged(stack, this.renderColor, this.edgeColor, frontX, blockDimension);
                        if (length > 1) Renderer3d.renderEdged(stack, this.renderColor, this.edgeColor, backX, blockDimension);
                    }

                    frontX = frontX.add(toAddZ);
                    backX = backX.add(toAddZ);
                }

                frontX = new Vec3d(frontX.x, frontX.y, renderPos.z);
                frontX = frontX.add(toAddY);
                backX = new Vec3d(backX.x, backX.y, renderPos.z);
                backX = backX.add(toAddY);
            }

            // left and right
            for (int y = 0; y < height - 2; y++) {
                for (int x = 0; x < length - 2; x++) {
                    if (this.renderType.equals(RenderType.EDGE_ONLY)) {
                        Renderer3d.renderOutline(stack, this.edgeColor, leftZ, blockDimension);
                        if (width > 1) Renderer3d.renderOutline(stack, this.edgeColor, rightZ, blockDimension);
                    } else {
                        Renderer3d.renderEdged(stack, this.renderColor, this.edgeColor, leftZ, blockDimension);
                        if (width > 1) Renderer3d.renderEdged(stack, this.renderColor, this.edgeColor, rightZ, blockDimension);
                    }

                    leftZ = leftZ.add(toAddX);
                    rightZ = rightZ.add(toAddX);
                }
                leftZ = new Vec3d(renderPos.x + 1, leftZ.y, leftZ.z);
                leftZ = leftZ.add(toAddY);
                rightZ = new Vec3d(renderPos.x + 1, rightZ.y, rightZ.z);
                rightZ = rightZ.add(toAddY);
            }

        }

        Renderer3d.renderThroughWalls();
    }

    public void renderCircle(MatrixStack stack, BlockPos lockPos, Direction.Axis lockAxis, BlockCounterModMenuConfig config) {
        if (!ImGuiService.isSphere.get()) {
            this.render2DCircle(stack, lockPos, lockAxis, config);
        } else {
            this.renderSphere(stack, lockPos, config);
        }
    }

    private void render2DCircle(MatrixStack stack, BlockPos lockPos, Direction.Axis lockAxis, BlockCounterModMenuConfig config) {
        Vec3d offset = new Vec3d(ImGuiService.xOffset[0], ImGuiService.yOffset[0], ImGuiService.zOffset[0]);

        BlockPos centerBlockPos;

        if (lockPos == null) {
            // First pos is either from where player is standing or where they are looking
            if (config.activationMethod.equals(ActivationMethod.STANDING)) {
                centerBlockPos = BlockPos.ofFloored(
                        MinecraftClient.getInstance().player.getPos()
                                .subtract(new Vec3d(0, 1, 0)));
            } else {
                Vec3d crosshairPos = getCrosshairBlockPos();
                if (crosshairPos == null) return;
                centerBlockPos = BlockPos.ofFloored(crosshairPos);
            }
        } else {
            centerBlockPos = lockPos;
        }

        Vec3d centerPos = Vec3d.of(centerBlockPos);
        centerPos = centerPos.add(offset);

        int radius = ImGuiService.radius[0];
        int height = ImGuiService.circleHeight[0];

        List<Vec3d> renderPoints = new ArrayList<>();


        if (!circlePositions.containsKey(radius + ":" + height)) {
            int rr = radius * radius;

            // Random inefficient algorithm from stack overflow
            // https://stackoverflow.com/questions/1201200/fast-algorithm-for-drawing-filled-circles

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    int xx = x * x;
                    int zz = z * z;

                    if (xx + zz < rr + radius) {
                        Vec3d toAddBottom = new Vec3d(x, 0, z);
                        if (lockAxis != null) {
                            if (lockAxis.equals(Direction.Axis.X)) {
                                toAddBottom = new Vec3d(0, z, x);
                            } else if (lockAxis.equals(Direction.Axis.Z)){
                                toAddBottom = new Vec3d(x, z, 0);
                            }
                        }

                        renderPoints.add(toAddBottom);

                        if (height > 1) {
                            Vec3d toAddTop = new Vec3d(x, height - 1, z);
                            if (lockAxis != null) {
                                if (lockAxis.equals(Direction.Axis.X)) {
                                    toAddTop = new Vec3d(height - 1, z, x);
                                } else if (lockAxis.equals(Direction.Axis.Z)){
                                    toAddTop = new Vec3d(x, z, height - 1);
                                }
                            }

                            renderPoints.add(toAddTop);

                            if (xx + zz > rr - radius) {
                                for (int y = 1; y < height - 1; y++) {
                                    Vec3d toAddSides = new Vec3d(x, y, z);
                                    if (lockAxis != null) {
                                        if (lockAxis.equals(Direction.Axis.X)) {
                                            toAddSides = new Vec3d(y, z, x);
                                        } else if (lockAxis.equals(Direction.Axis.Z)){
                                            toAddSides = new Vec3d(x, z, y);
                                        }
                                    }
                                    renderPoints.add(toAddSides);
                                }
                            }
                        }

                    }
                }
            }

            circlePositions.put(radius + ":" + height, renderPoints);
        } else {
            renderPoints = circlePositions.get(radius + ":" + height);
        }


        this.setRenderColors(config);
        Vec3d blockDimension = new Vec3d(1, 1, 1);

        Renderer3d.stopRenderThroughWalls();

        for (Vec3d point : renderPoints) {
            Vec3d toRender = centerPos.add(point);
            if (this.renderType.equals(RenderType.SOLID)) {
                Renderer3d.renderFilled(stack, this.renderColor, toRender, blockDimension);
            } else if (this.renderType.equals(RenderType.EDGE_ONLY)) {
                Renderer3d.renderOutline(stack, this.edgeColor, toRender, blockDimension);
            } else {
                Renderer3d.renderEdged(stack, this.renderColor, this.edgeColor, toRender, blockDimension);
            }
        }

        Renderer3d.renderThroughWalls();
    }

    private void renderSphere(MatrixStack stack, BlockPos lockPos, BlockCounterModMenuConfig config) {
        Vec3d offset = new Vec3d(ImGuiService.xOffset[0], ImGuiService.yOffset[0], ImGuiService.zOffset[0]);

        BlockPos centerBlockPos;

        if (lockPos == null) {
            // First pos is either from where player is standing or where they are looking
            if (config.activationMethod.equals(ActivationMethod.STANDING)) {
                centerBlockPos = BlockPos.ofFloored(
                        MinecraftClient.getInstance().player.getPos()
                                .subtract(new Vec3d(0, 1, 0)));
            } else {
                Vec3d crosshairPos = getCrosshairBlockPos();
                if (crosshairPos == null) return;
                centerBlockPos = BlockPos.ofFloored(crosshairPos);
            }
        } else {
            centerBlockPos = lockPos;
        }

        Vec3d centerPos = Vec3d.of(centerBlockPos);
        centerPos = centerPos.add(offset);

        int radius = ImGuiService.radius[0];

        List<Vec3d> renderPoints = new ArrayList<>();

        if (!spherePositions.containsKey(radius)) {
            int rr = radius * radius;

            // Random inefficient algorithm from stack overflow
            // https://stackoverflow.com/questions/1201200/fast-algorithm-for-drawing-filled-circles

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    for (int y = -radius; y <= radius; y++) {
                        int xx = x * x;
                        int yy = y * y;
                        int zz = z * z;

                        if ((xx + yy + zz < rr + radius) && (xx + yy + zz > rr - radius)) {
                            renderPoints.add(new Vec3d(x, y, z));
                        }
                    }

                }
            }

            spherePositions.put(radius, renderPoints);
        } else {
            renderPoints = spherePositions.get(radius);
        }

        this.setRenderColors(config);
        Vec3d blockDimension = new Vec3d(1, 1, 1);

        Renderer3d.stopRenderThroughWalls();

        for (Vec3d point : renderPoints) {
            Vec3d toRender = centerPos.add(point);
            if (this.renderType.equals(RenderType.SOLID)) {
                Renderer3d.renderFilled(stack, this.renderColor, toRender, blockDimension);
            } else if (this.renderType.equals(RenderType.EDGE_ONLY)) {
                Renderer3d.renderOutline(stack, this.edgeColor, toRender, blockDimension);
            } else {
                Renderer3d.renderEdged(stack, this.renderColor, this.edgeColor, toRender, blockDimension);
            }
        }

        Renderer3d.renderThroughWalls();
    }

    public static Vec3d getCrosshairBlockPos() {

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

    public static Vec3d toIntVec(Vec3d vec) {
        return new Vec3d((int) vec.x, (int) vec.y, (int) vec.z);
    }

    private void renderLine(BlockCounterModMenuConfig config, MatrixStack stack, Vec3d firstPos, Vec3d secondPos, boolean isClick) {

        if (ImGuiService.axisAligned.get()) {

            if (!ImGuiService.twoAxis.get()) {
                this.renderSingleLine(config, stack, firstPos, secondPos, isClick);
            } else {
                this.renderDoubleLine(config, stack, firstPos, secondPos, isClick);
            }

        } else {
            this.renderFreeLine(config, stack, firstPos, secondPos, isClick);
        }

    }

    private void renderSingleLine(BlockCounterModMenuConfig config, MatrixStack stack, Vec3d firstPos, Vec3d secondPos, boolean isClick) {

        this.setRenderColors(config);

        Vec3d firstPosInt = toIntVec(firstPos);
        Vec3d secondPosInt = toIntVec(secondPos);
        Vec3d alteredSecond = new Vec3d(secondPosInt.x, secondPosInt.y - (isClick ? 0 : 1),secondPosInt.z);
        Vec3d renderPos = new Vec3d(firstPosInt.x, firstPosInt.y - (isClick ? 0 : 1), firstPosInt.z);

        Vec3d dimensions = this.findDimensions(renderPos, alteredSecond);

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

        Direction.Axis dir = BlockCalculations.findLargestAxisDiff(dimensions);

        Vec3d blockDimension = new Vec3d(1, 1,1);
        int stopIndex = (int) (dir.equals(Direction.Axis.X) ? dimensions.x :
                (dir.equals(Direction.Axis.Y) ? dimensions.y : dimensions.z));
        Vec3d toAdd = (dir.equals(Direction.Axis.X) ? new Vec3d(1, 0, 0) :
                (dir.equals(Direction.Axis.Y) ? new Vec3d(0, 1, 0) : new Vec3d(0, 0, 1)));

        Renderer3d.renderThroughWalls();

        if (this.renderType.equals(RenderType.SOLID)) {
            Renderer3d.renderFilled(stack, this.renderColor, renderPos, dimensions);
        } else if (this.renderType.equals(RenderType.EDGE_ONLY)) {
            // render individual blocks
            for (int b = 0; b < stopIndex; b++) {
                Renderer3d.renderOutline(stack, this.edgeColor, renderPos, blockDimension);
                renderPos = renderPos.add(toAdd);
            }
        } else {
            for (int b = 0; b < stopIndex; b++) {
                Renderer3d.renderEdged(stack, this.renderColor, this.edgeColor, renderPos, blockDimension);
                renderPos = renderPos.add(toAdd);
            }
        }

        Renderer3d.stopRenderThroughWalls();

    }

    private void renderDoubleLine(BlockCounterModMenuConfig config, MatrixStack stack, Vec3d firstPos, Vec3d secondPos, boolean isClick) {

        Vec3d firstPosInt = toIntVec(firstPos);
        Vec3d firstStart = new Vec3d(firstPosInt.x, firstPosInt.y, firstPosInt.z);

        Vec3d secondPosInt = toIntVec(secondPos);
        Vec3d secondStart = new Vec3d(secondPosInt.x, secondPosInt.y, secondPosInt.z);

        List<Direction.Axis> largestDiffs = BlockCalculations.findTwoLargestAxisDiff(firstStart, secondStart);

        Direction.Axis first = largestDiffs.getFirst();
        Direction.Axis second = largestDiffs.get(1);

        Vec3d firstEnd;
        if (first.equals(Direction.Axis.X)) {
            firstEnd = new Vec3d(secondStart.x, firstStart.y, firstStart.z);
        } else if (first.equals(Direction.Axis.Y)) {
            firstEnd = new Vec3d(firstStart.x, secondStart.y, firstStart.z);
        } else {
            firstEnd = new Vec3d(firstStart.x, firstStart.y, secondStart.z);
        }

        this.renderSingleLine(config, stack, firstStart, firstEnd, isClick);

        Vec3d secondEnd;
        if (second.equals(Direction.Axis.X)) {
            secondEnd = new Vec3d(secondStart.x, firstEnd.y, firstEnd.z);
        } else if (second.equals(Direction.Axis.Y)) {
            secondEnd = new Vec3d(firstEnd.x, secondStart.y, firstEnd.z);
        } else {
            secondEnd = new Vec3d(firstEnd.x, firstEnd.y, secondStart.z);
        }

        this.renderSingleLine(config, stack, firstEnd, secondEnd, isClick);

    }

    // 3D Line Drawing algorithm with slight tweaks
    // https://www.geeksforgeeks.org/bresenhams-algorithm-for-3-d-line-drawing/
    private void renderFreeLine(BlockCounterModMenuConfig config, MatrixStack stack, Vec3d firstPos, Vec3d secondPos, boolean isClick) {
        Vec3d firstPosInt = toIntVec(firstPos);
        Vec3d secondPosInt = toIntVec(secondPos);

        Vec3d startPos = new Vec3d(firstPosInt.x, firstPosInt.y - (isClick ? 0 : 1), firstPosInt.z);
        Vec3d endPos = new Vec3d(secondPosInt.x, secondPosInt.y - (isClick ? 0 : 1), secondPosInt.z);

        int x = (int) startPos.x;
        int y = (int) startPos.y;
        int z = (int) startPos.z;

        int dy = Math.abs(((int) endPos.y) - y);
        int dx = Math.abs(((int) endPos.x) - x);
        int dz = Math.abs(((int) endPos.z) - z);

        List<Vec3d> renderPoints = new ArrayList<>();
        renderPoints.add(startPos);

        Vec3d xs, ys, zs;

        xs = new Vec3d(endPos.x > startPos.x ? 1 : -1, 0, 0);
        ys = new Vec3d(0, endPos.y > startPos.y ? 1 : -1, 0);
        zs = new Vec3d(0, 0, endPos.z > startPos.z ? 1 : -1);

        Direction.Axis largestDiff = BlockCalculations.findLargestAxisDiff(startPos, endPos);

        if (largestDiff.equals(Direction.Axis.X)) {
            int p1 = 2 * dy - dx;
            int p2 = 2 * dz - dx;

            while (startPos.x != endPos.x) {
                startPos = startPos.add(xs);
                if (p1 >= 0) {
                    startPos = startPos.add(ys);
                    p1 -= 2 * dx;
                }
                if (p2 >= 0) {
                    startPos = startPos.add(zs);
                    p2 -= 2 * dx;
                }
                p1 += 2 * dy;
                p2 += 2 * dz;
                renderPoints.add(new Vec3d(startPos.x, startPos.y, startPos.z));
            }
        } else if (largestDiff.equals(Direction.Axis.Y)) {
            int p1 = 2 * dx - dy;
            int p2 = 2 * dz - dy;

            while (startPos.y != endPos.y) {
                startPos = startPos.add(ys);
                if (p1 >= 0) {
                    startPos = startPos.add(xs);
                    p1 -= 2 * dy;
                }
                if (p2 >= 0) {
                    startPos = startPos.add(zs);
                    p2 -= 2 * dy;
                }
                p1 += 2 * dx;
                p2 += 2 * dz;
                renderPoints.add(new Vec3d(startPos.x, startPos.y, startPos.z));
            }
        } else {
            int p1 = 2 * dy - dz;
            int p2 = 2 * dx - dz;

            while (startPos.z != endPos.z) {
                startPos = startPos.add(zs);
                if (p1 >= 0) {
                    startPos = startPos.add(ys);
                    p1 -= 2 * dz;
                }
                if (p2 >= 0) {
                    startPos = startPos.add(xs);
                    p2 -= 2 * dz;
                }
                p1 += 2 * dy;
                p2 += 2 * dx;
                renderPoints.add(new Vec3d(startPos.x, startPos.y, startPos.z));
            }
        }

        this.setRenderColors(config);

        Vec3d blockDimension = new Vec3d(1, 1, 1);

        Renderer3d.renderThroughWalls();

        for (Vec3d point : renderPoints) {

            if (this.renderType.equals(RenderType.SOLID)) {
                Renderer3d.renderFilled(stack, this.renderColor, point, blockDimension);
            } else if (this.renderType.equals(RenderType.EDGE_ONLY)) {
                Renderer3d.renderOutline(stack, this.edgeColor, point, blockDimension);
            } else {
                Renderer3d.renderEdged(stack, this.renderColor, this.edgeColor, point, blockDimension);
            }

        }

        Renderer3d.stopRenderThroughWalls();
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
