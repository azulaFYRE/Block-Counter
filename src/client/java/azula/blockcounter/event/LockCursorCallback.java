package azula.blockcounter.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

public interface LockCursorCallback {

    Event<LockCursorCallback> EVENT = EventFactory.createArrayBacked(LockCursorCallback.class,
            (listeners) -> () -> {
                for (LockCursorCallback listener : listeners) {
                    ActionResult result = listener.lock();

                    if(result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    ActionResult lock();
}
