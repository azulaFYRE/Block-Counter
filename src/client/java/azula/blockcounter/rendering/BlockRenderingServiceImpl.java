package azula.blockcounter.rendering;

import azula.blockcounter.BlockCounterClient;
import azula.blockcounter.config.RenderType;
import azula.blockcounter.config.shape.LineConfigService;
import azula.blockcounter.util.BlockCalculations;
import azula.blockcounter.util.Random;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;


public class BlockRenderingServiceImpl implements BlockRenderingService {

    private final RenderingService renderingService;

    private final int MAX_RAY_DIST = 5;

    public BlockRenderingServiceImpl() {
        this.renderingService = new RenderingServiceImpl();
    }

    @Override
    public void extractStandingSelection(LevelExtractionContext context, Vec3 firstPos, BlockPos lockPos) {
        Minecraft client = Minecraft.getInstance();
        assert client.player != null;

        BlockPos blockPosFirst = new BlockPos(Random.toIntVec(firstPos));
        BlockPos playerPos = client.player.getOnPos();
        Vec3 toRender = new Vec3(playerPos);

        if (lockPos != null) {
            playerPos = lockPos;
            toRender = new Vec3(playerPos);
        }

        Vec3 fixedFirst = new Vec3(blockPosFirst);

        this.extractLine(context, fixedFirst, toRender, false);
    }

    @Override
    public void extractClickSelection(LevelExtractionContext context, Vec3 firstPos, BlockPos lockPos) {

        if (firstPos != null) {
            Vec3 secondPos = getCrosshairBlockPos();

            if (lockPos != null) {
                secondPos = new Vec3(lockPos);
            }

            if (secondPos != null) {
                this.extractLine(context, firstPos, secondPos, true);
            }
        }

    }

    @Override
    public void renderSelection(RenderType renderType) {
        switch (renderType) {
            case SOLID -> this.renderingService.renderQuadBuffer();
            case EDGE_ONLY -> this.renderingService.renderLineBuffer();
            case SOLID_EDGE -> {
                this.renderingService.renderQuadBuffer();
                this.renderingService.renderLineBuffer();
            }
        }
    }

    private Vec3 getCrosshairBlockPos() {

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;

        assert player != null;
        assert client.getCameraEntity() != null;

        // Raycast to where the player is currently looking
        BlockHitResult rayCastResult = (BlockHitResult) player.raycastHitResult(0, client.getCameraEntity());

        if (rayCastResult.getBlockPos().getCenter().distanceTo(player.getEyePosition()) - 0.5 <= MAX_RAY_DIST) {
            return new Vec3(
                    rayCastResult.getBlockPos().getX(),
                    rayCastResult.getBlockPos().getY(),
                    rayCastResult.getBlockPos().getZ()
            );
        }

        return null;
    }

    private void extractLine(LevelExtractionContext context, Vec3 firstPos, Vec3 secondPos, boolean isClick) {

        LineConfigService shapeService = BlockCounterClient.getInstance().getLineConfigService();

        if (shapeService.isAxisAligned()) {

            if (!shapeService.isTwoAxis()) {
                this.extractSingleLine(context, firstPos, secondPos, isClick);
            } else {
                this.extractDoubleLine(context, firstPos, secondPos, isClick);
            }

        } else {
            this.extractFreeLine(context, firstPos, secondPos, isClick);
        }

    }

    private void extractSingleLine(LevelExtractionContext context, Vec3 firstPos, Vec3 secondPos, boolean isClick) {

        LineConfigService service = BlockCounterClient.getInstance().getLineConfigService();
        Vec3i offset = new Vec3i(service.getXOffset(), service.getYOffset(), service.getZOffset());

        Vec3i firstPosInt = Random.toIntVec(firstPos).offset(offset);
        Vec3i secondPosInt = Random.toIntVec(secondPos).offset(offset);

        Vec3 alteredSecond = new Vec3(secondPosInt.getX(), secondPosInt.getY(), secondPosInt.getZ());
        Vec3 renderPos = new Vec3(firstPosInt.getX(), firstPosInt.getY(), firstPosInt.getZ());

        Vec3 dimensions = this.findDimensions(renderPos, alteredSecond);

        // Rendering library seems to have trouble with negative dimensions, so instead we will make the dimension
        // positive while translating the initial render position in order to render using positive dimensions
        if (dimensions.x < 0) {
            double absX = Math.abs(dimensions.x);
            int newFirstX = (int) (firstPosInt.getX() - absX);

            // Update the dimensions to be a positive offset while shifting the render position back
            dimensions = new Vec3(absX + 1, dimensions.y, dimensions.z);
            renderPos = new Vec3(newFirstX, renderPos.y, renderPos.z);
        } else if (dimensions.y < 0) {
            double absY = Math.abs(dimensions.y);
            int newFirstY = (int) (firstPosInt.getY() - absY);

            dimensions = new Vec3(dimensions.x, absY + 1, dimensions.z);
            renderPos = new Vec3(renderPos.x, newFirstY, renderPos.z);
        } else if (dimensions.z < 0) {
            double absZ = Math.abs(dimensions.z);
            int newFirstZ = (int) (firstPosInt.getZ() - absZ);

            dimensions = new Vec3(dimensions.x, dimensions.y, absZ + 1);
            renderPos = new Vec3(renderPos.x, renderPos.y, newFirstZ);
        }

        Direction.Axis dir = BlockCalculations.findLargestAxisDiff(dimensions);

        int stopIndex = (int) (dir.equals(Direction.Axis.X) ? dimensions.x :
                (dir.equals(Direction.Axis.Y) ? dimensions.y : dimensions.z));
        Vec3 toAdd = (dir.equals(Direction.Axis.X) ? new Vec3(1, 0, 0) :
                (dir.equals(Direction.Axis.Y) ? new Vec3(0, 1, 0) : new Vec3(0, 0, 1)));

        List<Vec3> renders = new ArrayList<>();

        for (int b = 0; b < stopIndex; b++) {
            renders.add(renderPos);
            renderPos = renderPos.add(toAdd);
        }

        switch (BlockCounterClient.getInstance().getConfig().renderType) {
            case SOLID -> this.renderingService.fillQuadBuffer(context, renders);
            case EDGE_ONLY -> this.renderingService.fillLineBuffer(context, renders);
            case SOLID_EDGE -> {
                this.renderingService.fillQuadBuffer(context, renders);
                this.renderingService.fillLineBuffer(context, renders);
            }
        }
    }

    private void extractDoubleLine(LevelExtractionContext context, Vec3 firstPos, Vec3 secondPos, boolean isClick) {

        Vec3i firstPosInt = Random.toIntVec(firstPos);
        Vec3 firstStart = new Vec3(firstPosInt.getX(), firstPosInt.getY(), firstPosInt.getZ());

        Vec3i secondPosInt = Random.toIntVec(secondPos);
        Vec3 secondStart = new Vec3(secondPosInt.getX(), secondPosInt.getY(), secondPosInt.getZ());

        List<Direction.Axis> largestDiffs = BlockCalculations.findTwoLargestAxisDiff(firstStart, secondStart);

        Direction.Axis first = largestDiffs.getFirst();
        Direction.Axis second = largestDiffs.get(1);

        Vec3 firstEnd;
        if (first.equals(Direction.Axis.X)) {
            firstEnd = new Vec3(secondStart.x, firstStart.y, firstStart.z);
        } else if (first.equals(Direction.Axis.Y)) {
            firstEnd = new Vec3(firstStart.x, secondStart.y, firstStart.z);
        } else {
            firstEnd = new Vec3(firstStart.x, firstStart.y, secondStart.z);
        }

        this.extractSingleLine(context, firstStart, firstEnd, isClick);

        Vec3 secondEnd;
        if (second.equals(Direction.Axis.X)) {
            secondEnd = new Vec3(secondStart.x, firstEnd.y, firstEnd.z);
        } else if (second.equals(Direction.Axis.Y)) {
            secondEnd = new Vec3(firstEnd.x, secondStart.y, firstEnd.z);
        } else {
            secondEnd = new Vec3(firstEnd.x, firstEnd.y, secondStart.z);
        }

        this.extractSingleLine(context, firstEnd, secondEnd, isClick);

    }

    // 3D Line Drawing algorithm with slight tweaks
    // https://www.geeksforgeeks.org/bresenhams-algorithm-for-3-d-line-drawing/
    private void extractFreeLine(LevelExtractionContext context, Vec3 firstPos, Vec3 secondPos, boolean isClick) {
        LineConfigService service = BlockCounterClient.getInstance().getLineConfigService();
        Vec3i offset = new Vec3i(service.getXOffset(), service.getYOffset(), service.getZOffset());

        Vec3i firstPosInt = Random.toIntVec(firstPos).offset(offset);
        Vec3i secondPosInt = Random.toIntVec(secondPos).offset(offset);

        Vec3 startPos = new Vec3(firstPosInt.getX(), firstPosInt.getY(), firstPosInt.getZ());
        Vec3 endPos = new Vec3(secondPosInt.getX(), secondPosInt.getY(), secondPosInt.getZ());

        int x = (int) startPos.x;
        int y = (int) startPos.y;
        int z = (int) startPos.z;

        int dy = Math.abs(((int) endPos.y) - y);
        int dx = Math.abs(((int) endPos.x) - x);
        int dz = Math.abs(((int) endPos.z) - z);

        List<Vec3> renderPoints = new ArrayList<>();
        renderPoints.add(startPos);

        Vec3 xs, ys, zs;

        xs = new Vec3(endPos.x > startPos.x ? 1 : -1, 0, 0);
        ys = new Vec3(0, endPos.y > startPos.y ? 1 : -1, 0);
        zs = new Vec3(0, 0, endPos.z > startPos.z ? 1 : -1);

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
                renderPoints.add(new Vec3(startPos.x, startPos.y, startPos.z));
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
                renderPoints.add(new Vec3(startPos.x, startPos.y, startPos.z));
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
                renderPoints.add(new Vec3(startPos.x, startPos.y, startPos.z));
            }
        }

        switch (BlockCounterClient.getInstance().getConfig().renderType) {
            case SOLID -> this.renderingService.fillQuadBuffer(context, renderPoints);
            case EDGE_ONLY -> this.renderingService.fillLineBuffer(context, renderPoints);
            case SOLID_EDGE -> {
                this.renderingService.fillQuadBuffer(context, renderPoints);
                this.renderingService.fillLineBuffer(context,renderPoints);
            }
        }
    }

    private Vec3 findDimensions(Vec3 firstPos, Vec3 secondPos) {

        Vec3i firstPosInt = Random.toIntVec(firstPos);
        Vec3i secondPosInt = Random.toIntVec(secondPos);

        int diffX = secondPosInt.getX() - firstPosInt.getX();
        int diffY = secondPosInt.getY() - firstPosInt.getY();
        int diffZ = secondPosInt.getZ() - firstPosInt.getZ();

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

        return new Vec3(x, y, z);
    }

}

