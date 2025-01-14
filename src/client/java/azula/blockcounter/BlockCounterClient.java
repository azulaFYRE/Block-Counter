package azula.blockcounter;

import azula.blockcounter.config.BlockCounterModMenuConfig;
import azula.blockcounter.rendering.BlockRenderingService;
import azula.blockcounter.util.BlockCalculations;
import azula.blockcounter.util.Random;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public class BlockCounterClient implements ClientModInitializer {
    public static final String MOD_ID = "block-counter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private final BlockRenderingService blockRenderingService = new BlockRenderingService();

    public static KeyBinding activationKey;

    private final AtomicReference<ActivationStep> standStep = new AtomicReference<>();
    private final AtomicReference<ActivationStep> clickStep = new AtomicReference<>();

    private BlockCounterModMenuConfig config;

    private Vec3d firstPosition;
    private Vec3d secondPosition;

    @Override
    public void onInitializeClient() {
        ConfigHolder<BlockCounterModMenuConfig> configHolder = AutoConfig
                .register(BlockCounterModMenuConfig.class, Toml4jConfigSerializer::new);

        // Load config
        this.config = configHolder.getConfig();

        // Load render color
        this.blockRenderingService.setRenderColors(this.config);

        // Grab activation keyBinding
        activationKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "text.autoconfig.blockcounter.option.activationKey",
                GLFW.GLFW_KEY_COMMA,
                "text.category.blockcounter"
        ));

        // Handle activation key press
        standStep.set(ActivationStep.FINISHED);
        clickStep.set(ActivationStep.FINISHED);

        // Handle Standing Activation Method
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (activationKey.wasPressed()) {
                assert client.player != null;

                if (config.activationMethod.equals(ActivationMethod.STANDING)) {
                    handleStanding(client.player);
                } else {
                    handleClickActivation(client.player);
                }
            }
        });

        // Handle Click Activation Method
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (config.activationMethod.equals(ActivationMethod.CLICK)) {
                return handleClick(player, hitResult.getBlockPos());
            } else {
                return ActionResult.PASS;
            }
        });

        // Block rendering
        WorldRenderEvents.LAST.register(context -> {
            if (firstPosition != null) {
                if (config.activationMethod.equals(ActivationMethod.STANDING)) {
                    blockRenderingService.renderStandingSelection(
                            context.matrixStack(),
                            firstPosition,
                            config);
                } else {
                    blockRenderingService.renderClickSelection(
                            context.matrixStack(),
                            firstPosition,
                            config);
                }
            }
        });
    }

    private void handleStanding(PlayerEntity player) {
        if (standStep.get().equals(ActivationStep.FINISHED)) {

            BlockPos firstPos = BlockPos.ofFloored(player.getPos());
            firstPosition = new Vec3d(firstPos.getX(), firstPos.getY(), firstPos.getZ());
            printFirst(player);

            standStep.set(ActivationStep.STARTED);

        } else if (standStep.get().equals(ActivationStep.STARTED)) {

            BlockPos secondPos = BlockPos.ofFloored(player.getPos());
            secondPosition = new Vec3d(secondPos.getX(), secondPos.getY(), secondPos.getZ());

            printSecond(player);

            firstPosition = null;
            secondPosition = null;

            standStep.set(ActivationStep.FINISHED);
        }
    }

    private void handleClickActivation(PlayerEntity player) {
        if (clickStep.get().equals(ActivationStep.FINISHED)) {

            player.sendMessage(
                    Text.literal("Right click first position...")
                            .formatted(Random.chatColorToFormat(config.chatColor)),
                    false
            );

            firstPosition = null;
            secondPosition = null;

            clickStep.set(ActivationStep.STARTED);

        } else {

            player.sendMessage(
                    Text.literal("Block count aborted.")
                            .formatted(Random.chatColorToFormat(config.chatColor)),
                    false
            );

            clickStep.set(ActivationStep.FINISHED);
            firstPosition = null;
            secondPosition = null;
        }
    }

    private ActionResult handleClick(PlayerEntity player, BlockPos pos) {

        if (clickStep.get().equals(ActivationStep.STARTED)) {
            firstPosition = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
            secondPosition = null;

            printFirst(player);

            clickStep.set(ActivationStep.DURING);

            return ActionResult.FAIL;

        } else if (clickStep.get().equals(ActivationStep.DURING)) {
            secondPosition = new Vec3d(pos.getX(), pos.getY(), pos.getZ());

            printSecond(player);

            firstPosition = null;
            secondPosition = null;

            clickStep.set(ActivationStep.FINISHED);

            return ActionResult.FAIL;

        } else {
            return ActionResult.PASS;
        }
    }

    private void printFirst(PlayerEntity player) {

        if (config.showPosMessages) {
            boolean simplify = config.simplifiedMessages;

            String first = Random.formatVec3d(firstPosition, "%,.2f");

            String firstPosLong = "First: %s";
            String firstPosShort = "1: %s";

            player.sendMessage(Text.literal(
                                    simplify ? String.format(firstPosShort, first) : String.format(firstPosLong, first))
                            .formatted(Random.chatColorToFormat(config.chatColor)),
                    false
            );
        }
    }

    private void printSecond(PlayerEntity player) {
        boolean simplify = config.simplifiedMessages;
        boolean isClick = config.activationMethod.equals(ActivationMethod.CLICK);

        if (config.showPosMessages) {
            String second = Random.formatVec3d(secondPosition, "%,.2f");

            String secondPosLong = "Second: %s";
            String secondPosShort = "2: %s";

            player.sendMessage(
                    Text.literal(simplify ? String.format(secondPosShort, second) : String.format(secondPosLong, second))
                            .formatted(Random.chatColorToFormat(config.chatColor)),
                    false
            );
        }

        int dist = BlockCalculations.calculateBlocksOne(firstPosition, secondPosition, isClick);

        String distLong = "Distance: %s %s";
        String distShort = "D: %d";

        player.sendMessage(
                Text.literal((simplify ?
                                String.format(distShort, dist)
                                : String.format(distLong, dist, dist == 1 ? "block" : "blocks")))
                        .formatted(Random.chatColorToFormat(config.chatColor)),
                false
        );

    }

}