package azula.blockcounter.rendering;

import azula.blockcounter.config.BlockCounterModMenuConfig;
import azula.blockcounter.config.RenderType;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public interface RenderingService {

    void setRenderColors(BlockCounterModMenuConfig config);
    void rebuildBuffer(List<Vec3d> pos, RenderType renderType, WorldRenderContext context);
    void render(WorldRenderContext context, RenderType type);
}
