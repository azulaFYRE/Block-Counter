package azula.blockcounter.rendering;

import azula.blockcounter.ActivationMethod;
import azula.blockcounter.BlockCounterClient;
import azula.blockcounter.config.BlockCounterModMenuConfig;
import azula.blockcounter.config.shape.Shape;
import azula.blockcounter.config.shape.ShapeConfigService;
import azula.blockcounter.util.BlockCalculations;
import azula.blockcounter.util.Random;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BlockRenderingServiceImpl implements BlockRenderingService {

    private final RenderingService renderingService;

    private final Map<String, ArrayList<Vec3d>> quadPosCache = new HashMap<>();
    private final Map<String, Integer> quadBlockCount = new HashMap<>(); // total blocks
    private final Map<String, Integer> quadRenderCount = new HashMap<>(); // total blocks if only counting rendered

    private final Map<String, ArrayList<Vec3d>> circlePosCache = new HashMap<>();
    private final Map<String, Integer> circleBlockCount = new HashMap<>();
    private final Map<String, Integer> circleRenderCount = new HashMap<>();

    private final Map<String, ArrayList<Vec3d>> spherePosCache = new HashMap<>();
    private final Map<String, Integer> sphereBlockCount = new HashMap<>();
    private final Map<String, Integer> sphereRenderCount = new HashMap<>();

    private Vec3d lastRenderPos = null;

    private Shape lastShape = null;
    private Vec3d lastDims = null;
    private Vec3d lastOffsets = null;
    private Direction.Axis lastLookAxis = null;

    public BlockRenderingServiceImpl() {
        this.renderingService = new RenderingServiceImpl();
    }

    public void renderStandingSelection(WorldRenderContext context, Vec3d firstPos, BlockPos lockPos) {

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

            this.renderLine(context, fixedFirst, toRender, false);
        }
    }

    public void renderClickSelection(WorldRenderContext context, Vec3d firstPos, BlockPos lockPos) {
        if (firstPos != null) {
            Vec3d secondPos = getCrosshairBlockPos();

            if (lockPos != null) {
                secondPos = Vec3d.of(lockPos);
            }

            if (secondPos != null) {
                this.renderLine(context, firstPos, secondPos, true);
            }
        }
    }

    @Override
    public void renderQuad(WorldRenderContext context, BlockPos lockPos) {
        ShapeConfigService shapeService = BlockCounterClient.getInstance().getShapeConfigService();
        BlockCounterModMenuConfig config = BlockCounterClient.getInstance().getConfig();

        Vec3d offset = new Vec3d(shapeService.getXOffset(), shapeService.getYOffset(), shapeService.getZOffset());

        BlockPos renderBlockPos;

        if (lockPos == null) {
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

        int[] dimensions = shapeService.getDimensions(); // w, l, h for quad

        Vec3d dimensionVec = new Vec3d(dimensions[0], dimensions[1], dimensions[2]);

        Vec3d renderPos = Vec3d.of(renderBlockPos).add(offset);

        if (!renderPos.equals(this.lastRenderPos)
                || !Shape.QUAD.equals(this.lastShape)
                || !dimensionVec.equals(this.lastDims)
                || !shapeService.getOffsets().equals(this.lastOffsets)) {
            List<Vec3d> quadPos = this.getQuadPositions(dimensions);
            List<Vec3d> renderQuadPos = quadPos.stream().map(p -> p.add(renderPos)).toList();

            this.renderingService.rebuildBuffer(renderQuadPos, config.renderType, context);
        }

        this.renderingService.render(context, config.renderType);

        this.lastRenderPos = renderPos;
        this.lastShape = Shape.QUAD;
        this.lastDims = dimensionVec;
        this.lastOffsets = shapeService.getOffsets();
        this.lastLookAxis = null;
    }

    private String quadCacheKey(int[] dimensions) {
        return dimensions[0] + ";" + dimensions[1] + ";" + dimensions[2];
    }

    private ArrayList<Vec3d> getQuadPositions(int[] dimensions) {

        String quadKey = this.quadCacheKey(dimensions);

        if (this.quadPosCache.containsKey(quadKey)) {
            return this.quadPosCache.get(quadKey);
        }

        Vec3d origin = Vec3d.ZERO;

        Vec3d toAddX = new Vec3d(1, 0, 0);
        Vec3d toAddY = new Vec3d(0, 1, 0);
        Vec3d toAddZ = new Vec3d(0, 0, 1);

        Vec3d bottomY = new Vec3d(origin.x, origin.y, origin.z);
        Vec3d topY = new Vec3d(origin.x, origin.y + dimensions[2] - 1, origin.z);

        Vec3d frontX = new Vec3d(origin.x, origin.y + 1, origin.z);
        Vec3d backX = new Vec3d(origin.x + dimensions[1] - 1, origin.y + 1, origin.z);

        Vec3d leftZ = new Vec3d(origin.x + 1, origin.y + 1, origin.z);
        Vec3d rightZ = new Vec3d(origin.x + 1, origin.y + 1, origin.z + dimensions[0] - 1);

        ArrayList<Vec3d> toRender = new ArrayList<>();

        // top and bottom
        for (int z = 0; z < dimensions[0]; z++) {
            for (int x = 0; x < dimensions[1]; x++) {

                toRender.addAll(this.quadBlockAdd(dimensions[2], bottomY, topY));

                bottomY = bottomY.add(toAddX);
                topY = topY.add(toAddX);
            }
            bottomY = new Vec3d(origin.x, bottomY.y, bottomY.z);
            bottomY = bottomY.add(toAddZ);
            topY = new Vec3d(origin.x, topY.y, topY.z);
            topY = topY.add(toAddZ);
        }

        // front and back
        for (int y = 0; y < dimensions[2] - 2; y++) {
            for (int z = 0; z < dimensions[0]; z++) {

                toRender.addAll(this.quadBlockAdd(dimensions[1], frontX, backX));

                frontX = frontX.add(toAddZ);
                backX = backX.add(toAddZ);
            }

            frontX = new Vec3d(frontX.x, frontX.y, origin.z);
            frontX = frontX.add(toAddY);
            backX = new Vec3d(backX.x, backX.y, origin.z);
            backX = backX.add(toAddY);
        }

        // left and right
        for (int y = 0; y < dimensions[2] - 2; y++) {
            for (int x = 0; x < dimensions[1] - 2; x++) {

                toRender.addAll(this.quadBlockAdd(dimensions[0], leftZ, rightZ));

                leftZ = leftZ.add(toAddX);
                rightZ = rightZ.add(toAddX);
            }

            leftZ = new Vec3d(origin.x + 1, leftZ.y, leftZ.z);
            leftZ = leftZ.add(toAddY);
            rightZ = new Vec3d(origin.x + 1, rightZ.y, rightZ.z);
            rightZ = rightZ.add(toAddY);
        }

        this.quadPosCache.put(quadKey, toRender);

        return toRender;
    }

    private ArrayList<Vec3d> quadBlockAdd(int limiter, Vec3d defaultAdd, Vec3d pastLimitAdd) {
        ArrayList<Vec3d> result = new ArrayList<>();

        result.add(defaultAdd);
        if (limiter > 1) result.add(pastLimitAdd);

        return result;
    }

    @Override
    public void renderCircle(WorldRenderContext context, BlockPos lockPos, Direction.Axis lookAxis) {
        ShapeConfigService shapeService = BlockCounterClient.getInstance().getShapeConfigService();
        BlockCounterModMenuConfig config = BlockCounterClient.getInstance().getConfig();

        Vec3d offset = new Vec3d(shapeService.getXOffset(), shapeService.getYOffset(), shapeService.getZOffset());

        BlockPos renderBlockPos;

        if (lockPos == null) {
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

        int[] dimensions = shapeService.getDimensions(); // w, l, h for quad

        Vec3d dimensionVec = new Vec3d(dimensions[0], dimensions[1], dimensions[2]);

        Vec3d renderPos = Vec3d.of(renderBlockPos).add(offset);

        if (!renderPos.equals(this.lastRenderPos)
                || !Shape.CIRCLE.equals(this.lastShape)
                || !dimensionVec.equals(this.lastDims)
                || !shapeService.getOffsets().equals(this.lastOffsets)
                || (lookAxis != null && !lookAxis.equals(this.lastLookAxis))
                || (this.lastLookAxis != null && !this.lastLookAxis.equals(lookAxis))) {
            List<Vec3d> circlePos = this.getCirclePositions(dimensions, lookAxis);
            List<Vec3d> renderCirclePos = circlePos.stream().map(p -> p.add(renderPos)).toList();

            this.renderingService.rebuildBuffer(renderCirclePos, config.renderType, context);
        }

        this.renderingService.render(context, config.renderType);

        this.lastRenderPos = renderPos;
        this.lastShape = Shape.CIRCLE;
        this.lastDims = dimensionVec;
        this.lastOffsets = shapeService.getOffsets();
        this.lastLookAxis = lookAxis;
    }

    private String circleCacheKey(int[] dimensions, Direction.Axis lockAxis) {
        String axis = lockAxis != null ? lockAxis.getName() : "none";
        return axis + ";" + dimensions[0] + ";" + dimensions[1] + ";" + dimensions[2];
    }

    private List<Vec3d> getCirclePositions(int[] dimensions, Direction.Axis lockAxis) {
        String circleKey = this.circleCacheKey(dimensions, lockAxis);

        if (this.circlePosCache.containsKey(circleKey)) {
            return this.circlePosCache.get(circleKey);
        }

        int radius = dimensions[0];
        int height = dimensions[1];

        int rr = radius * radius;

        // Random inefficient algorithm from stack overflow
        // https://stackoverflow.com/questions/1201200/fast-algorithm-for-drawing-filled-circles

        ArrayList<Vec3d> renderPoints = new ArrayList<>();

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

        this.circlePosCache.put(circleKey, renderPoints);

        return renderPoints;
    }

    @Override
    public void renderSphere(WorldRenderContext context, BlockPos lockPos) {
        ShapeConfigService shapeService = BlockCounterClient.getInstance().getShapeConfigService();
        BlockCounterModMenuConfig config = BlockCounterClient.getInstance().getConfig();

        Vec3d offset = new Vec3d(shapeService.getXOffset(), shapeService.getYOffset(), shapeService.getZOffset());

        BlockPos renderBlockPos;

        if (lockPos == null) {
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

        int[] dimensions = shapeService.getDimensions(); // w, l, h for quad

        Vec3d dimensionVec = new Vec3d(dimensions[0], dimensions[1], dimensions[2]);

        Vec3d renderPos = Vec3d.of(renderBlockPos).add(offset);

        if (!renderPos.equals(this.lastRenderPos)
                || !Shape.SPHERE.equals(this.lastShape)
                || !dimensionVec.equals(this.lastDims)
                || !shapeService.getOffsets().equals(this.lastOffsets)) {
            List<Vec3d> spherePos = this.getSpherePositions(dimensions);
            List<Vec3d> renderSpherePos = spherePos.stream().map(p -> p.add(renderPos)).toList();

            this.renderingService.rebuildBuffer(renderSpherePos, config.renderType, context);
        }

        this.renderingService.render(context, config.renderType);

        this.lastRenderPos = renderPos;
        this.lastShape = Shape.SPHERE;
        this.lastDims = dimensionVec;
        this.lastOffsets = shapeService.getOffsets();
        this.lastLookAxis = null;
    }

    private String sphereCacheKey(int[] dimensions) {
        return dimensions[0] + ";";
    }

    private List<Vec3d> getSpherePositions(int[] dimensions) {

        String sphereKey = this.sphereCacheKey(dimensions);

        if (this.spherePosCache.containsKey(sphereKey)) {
            return this.spherePosCache.get(sphereKey);
        }

        int radius = dimensions[0];

        ArrayList<Vec3d> renderPoints = new ArrayList<>();

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

        this.spherePosCache.put(sphereKey, renderPoints);

        return renderPoints;
    }

    @Override
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

    @Override
    public Integer getTotalQuadCount(int[] dimensions) {
        String quadKey = this.quadCacheKey(dimensions);

        if (this.quadBlockCount.containsKey(quadKey)) {
            return this.quadBlockCount.get(quadKey);
        }

        Integer count = BlockCalculations.calculateBlocksQuad(dimensions[0], dimensions[1], dimensions[2], false);
        this.quadBlockCount.put(quadKey, count);

        return count;
    }

    @Override
    public Integer getRenderQuadCount(int[] dimensions) {
        String quadKey = this.quadCacheKey(dimensions);

        if (this.quadRenderCount.containsKey(quadKey)) {
            return this.quadRenderCount.get(quadKey);
        }

        Integer count = BlockCalculations.calculateBlocksQuad(dimensions[0], dimensions[1], dimensions[2], true);
        this.quadRenderCount.put(quadKey, count);

        return count;
    }

    private void renderLine(WorldRenderContext context, Vec3d firstPos, Vec3d secondPos, boolean isClick) {

        ShapeConfigService shapeService = BlockCounterClient.getInstance().getShapeConfigService();

        if (shapeService.isAxisAligned()) {

            if (!shapeService.isTwoAxis()) {
                this.renderSingleLine(context, firstPos, secondPos, isClick);
            } else {
                this.renderDoubleLine(context, firstPos, secondPos, isClick);
            }

        } else {
            this.renderFreeLine(context, firstPos, secondPos, isClick);
        }

    }

    private void renderSingleLine(WorldRenderContext context, Vec3d firstPos, Vec3d secondPos, boolean isClick) {

        ShapeConfigService service = BlockCounterClient.getInstance().getShapeConfigService();
        Vec3d offset = new Vec3d(service.getXOffset(), service.getYOffset(), service.getZOffset());

        Vec3d firstPosInt = Random.toIntVec(firstPos).add(offset);
        Vec3d secondPosInt = Random.toIntVec(secondPos).add(offset);

        Vec3d alteredSecond = new Vec3d(secondPosInt.x, secondPosInt.y - (isClick ? 0 : 1), secondPosInt.z);
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

        int stopIndex = (int) (dir.equals(Direction.Axis.X) ? dimensions.x :
                (dir.equals(Direction.Axis.Y) ? dimensions.y : dimensions.z));
        Vec3d toAdd = (dir.equals(Direction.Axis.X) ? new Vec3d(1, 0, 0) :
                (dir.equals(Direction.Axis.Y) ? new Vec3d(0, 1, 0) : new Vec3d(0, 0, 1)));

        List<Vec3d> line = new ArrayList<>();

        for (int b = 0; b < stopIndex; b++) {
            line.add(renderPos);
            renderPos = renderPos.add(toAdd);
        }

        if (!secondPos.equals(this.lastRenderPos)
                || !Shape.LINE.equals(this.lastShape)
                || !BlockCounterClient.getInstance().getShapeConfigService().getOffsets().equals(this.lastOffsets)) {
            this.renderingService.rebuildBuffer(line, BlockCounterClient.getInstance().getConfig().renderType, context);
        }

        this.renderingService.render(context, BlockCounterClient.getInstance().getConfig().renderType);

        this.lastRenderPos = secondPos;
        this.lastShape = Shape.LINE;
        this.lastDims = null;
        this.lastOffsets = BlockCounterClient.getInstance().getShapeConfigService().getOffsets();
    }

    private void renderDoubleLine(WorldRenderContext context, Vec3d firstPos, Vec3d secondPos, boolean isClick) {

        Vec3d firstPosInt = Random.toIntVec(firstPos);
        Vec3d firstStart = new Vec3d(firstPosInt.x, firstPosInt.y, firstPosInt.z);

        Vec3d secondPosInt = Random.toIntVec(secondPos);
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

        this.renderSingleLine(context, firstStart, firstEnd, isClick);

        Vec3d secondEnd;
        if (second.equals(Direction.Axis.X)) {
            secondEnd = new Vec3d(secondStart.x, firstEnd.y, firstEnd.z);
        } else if (second.equals(Direction.Axis.Y)) {
            secondEnd = new Vec3d(firstEnd.x, secondStart.y, firstEnd.z);
        } else {
            secondEnd = new Vec3d(firstEnd.x, firstEnd.y, secondStart.z);
        }

        this.renderSingleLine(context, firstEnd, secondEnd, isClick);

    }

    // 3D Line Drawing algorithm with slight tweaks
    // https://www.geeksforgeeks.org/bresenhams-algorithm-for-3-d-line-drawing/
    private void renderFreeLine(WorldRenderContext context, Vec3d firstPos, Vec3d secondPos, boolean isClick) {
        ShapeConfigService service = BlockCounterClient.getInstance().getShapeConfigService();
        Vec3d offset = new Vec3d(service.getXOffset(), service.getYOffset(), service.getZOffset());

        Vec3d firstPosInt = Random.toIntVec(firstPos).add(offset);
        Vec3d secondPosInt = Random.toIntVec(secondPos).add(offset);

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

        if (!secondPos.equals(this.lastRenderPos)
                || !Shape.LINE.equals(this.lastShape)
                || !BlockCounterClient.getInstance().getShapeConfigService().getOffsets().equals(this.lastOffsets)) {
            this.renderingService.rebuildBuffer(renderPoints, BlockCounterClient.getInstance().getConfig().renderType, context);
        }

        this.renderingService.render(context, BlockCounterClient.getInstance().getConfig().renderType);

        this.lastRenderPos = secondPos;
        this.lastShape = Shape.LINE;
        this.lastDims = null;
        this.lastOffsets = BlockCounterClient.getInstance().getShapeConfigService().getOffsets();
    }

    private Vec3d findDimensions(Vec3d firstPos, Vec3d secondPos) {

        Vec3d firstPosInt = Random.toIntVec(firstPos);
        Vec3d secondPosInt = Random.toIntVec(secondPos);

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

