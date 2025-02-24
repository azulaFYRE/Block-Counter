package azula.blockcounter.mixin.client;

import azula.blockcounter.event.LockCursorCallback;
import net.minecraft.client.Mouse;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(method="lockCursor", at=@At("HEAD"), cancellable = true)
    public void lockCursor(CallbackInfo ci) {
        ActionResult result = LockCursorCallback.EVENT.invoker().lock();

        if (result == ActionResult.FAIL) ci.cancel();
    }
}
