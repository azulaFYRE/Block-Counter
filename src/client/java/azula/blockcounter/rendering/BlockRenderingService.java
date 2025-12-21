package azula.blockcounter.rendering;

import azula.blockcounter.rendering.world.BlockCounterRenderContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface BlockRenderingService {

    void renderStandingSelection(BlockCounterRenderContext context, Vec3d firstPos, BlockPos lockPos);

    void renderClickSelection(BlockCounterRenderContext context, Vec3d firstPos, BlockPos lockPos);

}