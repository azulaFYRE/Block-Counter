package azula.blockcounter.rendering;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public interface BlockRenderingService {

    void renderStandingSelection(WorldRenderContext context, Vec3d firstPos, BlockPos lockPos);

    void renderClickSelection(WorldRenderContext context, Vec3d firstPos, BlockPos lockPos);

    void renderQuad(WorldRenderContext context, BlockPos lockPos);

    void renderCircle(WorldRenderContext context, BlockPos lockPos, Direction.Axis lockAxis);

    void renderSphere(WorldRenderContext context, BlockPos lockPos);

    Vec3d getCrosshairBlockPos();

}