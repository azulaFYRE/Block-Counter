package azula.blockcounter.mixin.client;

import azula.blockcounter.rendering.ImGuiService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(MinecraftClient.class)
public class ClientMixin {

    @Shadow
    @Final
    private Window window;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void initImGui(RunArgs args, CallbackInfo callbackInfo) {
        try {
            ImGuiService.init(window.getHandle());
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Block-Counter ImGui: \n" + e.getMessage());
        }

    }

    @Inject(method = "close", at = @At("RETURN"))
    public void disposeImGui(CallbackInfo callbackInfo) {
        ImGuiService.dispose();
    }

}