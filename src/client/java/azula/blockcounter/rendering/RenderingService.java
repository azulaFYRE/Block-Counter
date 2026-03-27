package azula.blockcounter.rendering;

import azula.blockcounter.config.BlockCounterModMenuConfig;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public interface RenderingService {

    void setRenderColors(BlockCounterModMenuConfig config);

    void fillLineBuffer(LevelExtractionContext context, List<Vec3> pos);
    void fillQuadBuffer(LevelExtractionContext context, List<Vec3> pos);

    void renderLineBuffer();
    void renderQuadBuffer();
}