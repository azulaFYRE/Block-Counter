package azula.blockcounter.rendering;

import azula.blockcounter.config.BlockCounterModMenuConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.Vec3d;

public interface RenderingService {

    void setRenderColors(BlockCounterModMenuConfig config);
    void startLineBuffer();
    void startQuadBuffer();
    void addSolid(WorldRenderContext context, Vec3d pos);
    void addEdged(WorldRenderContext context, Vec3d pos);
    void renderQuadBuffer(WorldRenderContext context);
    void renderLineBuffer(WorldRenderContext context);
}
