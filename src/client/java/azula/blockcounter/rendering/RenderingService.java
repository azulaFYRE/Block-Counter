package azula.blockcounter.rendering;

import azula.blockcounter.config.BlockCounterModMenuConfig;
import azula.blockcounter.rendering.world.BlockCounterWorldRenderContext;
import net.minecraft.util.math.Vec3d;

public interface RenderingService {

    void setRenderColors(BlockCounterModMenuConfig config);
    void startLineBuffer(BlockCounterWorldRenderContext context);
    void startQuadBuffer(BlockCounterWorldRenderContext context);
    void addSolid(BlockCounterWorldRenderContext context, Vec3d pos);
    void addEdged(BlockCounterWorldRenderContext context, Vec3d pos);
}