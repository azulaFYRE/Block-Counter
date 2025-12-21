package azula.blockcounter.rendering.world;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface BlockCounterRenderEvents {
    Event<AfterWorld> AFTER_WORLD = EventFactory.createArrayBacked(AfterWorld.class, listeners -> context -> {
        for (AfterWorld listener : listeners) {
            listener.onRender(context);
        }
    });

    @FunctionalInterface
    interface AfterWorld {
        void onRender(BlockCounterRenderContext context);
    }
}
