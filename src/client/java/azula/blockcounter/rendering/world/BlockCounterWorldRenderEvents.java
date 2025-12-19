package azula.blockcounter.rendering.world;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface BlockCounterWorldRenderEvents {
    Event<Last> LAST = EventFactory.createArrayBacked(Last.class, listeners -> context -> {
        for (Last listener : listeners) {
            listener.onRender(context);
        }
    });

    @FunctionalInterface
    interface Last {
        void onRender(BlockCounterWorldRenderContext context);
    }
}
