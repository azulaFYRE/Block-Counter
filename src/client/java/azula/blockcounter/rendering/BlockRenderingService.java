package azula.blockcounter.rendering;

import azula.blockcounter.config.BlockCounterModMenuConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface BlockRenderingService {

    void setRenderColors(BlockCounterModMenuConfig config);

    void renderStandingSelection(WorldRenderContext context, Vec3d firstPos, BlockPos lockPos, BlockCounterModMenuConfig config);

    void renderClickSelection(WorldRenderContext context, Vec3d firstPos, BlockPos lockPos, BlockCounterModMenuConfig config);

}