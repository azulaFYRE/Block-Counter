package azula.blockcounter.rendering;

import azula.blockcounter.config.BlockCounterModMenuConfig;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;

public interface RenderingService {

    void setRenderColors(BlockCounterModMenuConfig config);

    void renderLineBuffer(LevelRenderContext context, BlockRenderingService.BlockCounterRenderState renderState);
    void renderQuadBuffer(LevelRenderContext context, BlockRenderingService.BlockCounterRenderState renderState);
}