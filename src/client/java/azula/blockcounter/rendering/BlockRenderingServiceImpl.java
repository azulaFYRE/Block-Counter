package azula.blockcounter.rendering;

import azula.blockcounter.BlockCounterClient;
import azula.blockcounter.config.shape.LineConfigService;
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
import java.util.List;


public class BlockRenderingServiceImpl implements BlockRenderingService {

    private final RenderingService renderingService;

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

    private Vec3d getCrosshairBlockPos() {

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

    private void renderLine(WorldRenderContext context, Vec3d firstPos, Vec3d secondPos, boolean isClick) {

        LineConfigService shapeService = BlockCounterClient.getInstance().getLineConfigService();

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

        LineConfigService service = BlockCounterClient.getInstance().getLineConfigService();
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

        // start the necessary vertex buffer
        switch (BlockCounterClient.getInstance().getConfig().renderType) {
            case SOLID -> {
                this.renderingService.startQuadBuffer();

                for (int b = 0; b < stopIndex; b++) {
                    this.renderingService.addSolid(context, renderPos);
                    renderPos = renderPos.add(toAdd);
                }

                this.renderingService.renderQuadBuffer(context);
            }
            case EDGE_ONLY -> {
                this.renderingService.startLineBuffer();

                for (int b = 0; b < stopIndex; b++) {
                    this.renderingService.addEdged(context, renderPos);
                    renderPos = renderPos.add(toAdd);
                }

                this.renderingService.renderLineBuffer(context);
            }
            case SOLID_EDGE -> {
                this.renderingService.startQuadBuffer();

                Vec3d start = new Vec3d(renderPos.x, renderPos.y, renderPos.z);

                for (int b = 0; b < stopIndex; b++) {
                    this.renderingService.addSolid(context, renderPos);
                    renderPos = renderPos.add(toAdd);
                }

                this.renderingService.renderQuadBuffer(context);

                renderPos = new Vec3d(start.x, start.y, start.z);

                this.renderingService.startLineBuffer();

                for (int b = 0; b < stopIndex; b++) {
                    this.renderingService.addEdged(context, renderPos);
                    renderPos = renderPos.add(toAdd);
                }

                this.renderingService.renderLineBuffer(context);
            }
        }
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
        LineConfigService service = BlockCounterClient.getInstance().getLineConfigService();
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

        // create the necessary vertex buffer
        switch (BlockCounterClient.getInstance().getConfig().renderType) {
            case SOLID -> {
                this.renderingService.startQuadBuffer();

                renderPoints.forEach(p -> this.renderingService.addSolid(context, p));

                this.renderingService.renderQuadBuffer(context);
            }
            case EDGE_ONLY -> {
                this.renderingService.startLineBuffer();

                renderPoints.forEach(p -> this.renderingService.addEdged(context, p));

                this.renderingService.renderLineBuffer(context);
            }
            case SOLID_EDGE -> {
                this.renderingService.startQuadBuffer();

                renderPoints.forEach(p -> this.renderingService.addSolid(context, p));

                this.renderingService.renderQuadBuffer(context);

                this.renderingService.startLineBuffer();

                renderPoints.forEach(p -> this.renderingService.addEdged(context, p));

                this.renderingService.renderLineBuffer(context);
            }
        }
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

