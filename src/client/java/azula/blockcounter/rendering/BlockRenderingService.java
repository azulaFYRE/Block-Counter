package azula.blockcounter.rendering;

import azula.blockcounter.rendering.world.BlockCounterWorldRenderContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface BlockRenderingService {

    void renderStandingSelection(BlockCounterWorldRenderContext context, Vec3d firstPos, BlockPos lockPos);

    void renderClickSelection(BlockCounterWorldRenderContext context, Vec3d firstPos, BlockPos lockPos);

}