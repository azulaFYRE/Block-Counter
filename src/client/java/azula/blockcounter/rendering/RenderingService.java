package azula.blockcounter.rendering;

import azula.blockcounter.config.BlockCounterModMenuConfig;
import azula.blockcounter.rendering.world.BlockCounterRenderContext;
import net.minecraft.util.math.Vec3d;

public interface RenderingService {

    void setRenderColors(BlockCounterModMenuConfig config);
    void startLineBuffer(BlockCounterRenderContext context);
    void startQuadBuffer(BlockCounterRenderContext context);
    void addSolid(BlockCounterRenderContext context, Vec3d pos);
    void addEdged(BlockCounterRenderContext context, Vec3d pos);
    void renderLines();
    void renderQuads();
}