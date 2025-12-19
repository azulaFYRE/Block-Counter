package azula.blockcounter.rendering;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface BlockRenderingService {

    void renderStandingSelection(WorldRenderContext context, Vec3d firstPos, BlockPos lockPos);

    void renderClickSelection(WorldRenderContext context, Vec3d firstPos, BlockPos lockPos);

}