package azula.blockcounter.rendering;

import azula.blockcounter.config.RenderType;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public interface BlockRenderingService {

    void extractStandingSelection(LevelExtractionContext context, Vec3 firstPos, BlockPos lockPos);

    void extractClickSelection(LevelExtractionContext context, Vec3 firstPos, BlockPos lockPos);

    void renderSelection(RenderType renderType, LevelRenderContext context);

    record BlockCounterRenderState(List<Vec3> positions) {}
}