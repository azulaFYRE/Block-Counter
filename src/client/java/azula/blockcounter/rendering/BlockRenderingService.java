package azula.blockcounter.rendering;

import azula.blockcounter.config.BlockCounterModMenuConfig;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface BlockRenderingService {

    void setRenderColors(BlockCounterModMenuConfig config);

    void renderStandingSelection(MatrixStack stack, Vec3d firstPos, BlockPos lockPos, BlockCounterModMenuConfig config);

    void renderClickSelection(MatrixStack stack, Vec3d firstPos, BlockPos lockPos, BlockCounterModMenuConfig config);

}