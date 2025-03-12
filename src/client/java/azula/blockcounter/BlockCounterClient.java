package azula.blockcounter;

import azula.blockcounter.config.BlockCounterModMenuConfig;
import azula.blockcounter.config.MessageDisplay;
import azula.blockcounter.config.shape.gui.ShapeConfigScreen;
import azula.blockcounter.config.shape.ShapeConfigService;
import azula.blockcounter.rendering.BlockRenderingService;
import azula.blockcounter.util.BlockCalculations;
import azula.blockcounter.util.Random;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public class BlockCounterClient implements ClientModInitializer {
    public static final String MOD_ID = "block-counter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private final BlockRenderingService blockRenderingService = new BlockRenderingService();
    private final ShapeConfigService shapeConfigService = new ShapeConfigService();

    public static KeyBinding activationKey;
    public static KeyBinding configMenuKey;

    private final AtomicReference<ActivationStep> standStep = new AtomicReference<>();
    private final AtomicReference<ActivationStep> clickStep = new AtomicReference<>();
    private final AtomicReference<ActivationStep> shapeStep = new AtomicReference<>();

    private BlockCounterModMenuConfig config;

    private Vec3d firstPosition;
    private Vec3d secondPosition;

    private Direction.Axis lookAxis = null;

    private static BlockCounterClient instance;

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

        // Grab config menu keyBinding
        configMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "text.autoconfig.blockcounter.option.configMenuKey",
                GLFW.GLFW_KEY_DELETE,
                "text.category.blockcounter"
        ));

        // Handle activation key press
        standStep.set(ActivationStep.FINISHED);
        clickStep.set(ActivationStep.FINISHED);
        shapeStep.set(ActivationStep.FINISHED);

        // Handle Standing Activation Method
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            // Check for menu key
            while (configMenuKey.wasPressed()) {
                client.setScreen(new ShapeConfigScreen(this.shapeConfigService, client.currentScreen));
            }

            // Check for activation key
            while (activationKey.wasPressed()) {
                assert client.player != null;

                Shape selected = this.shapeConfigService.getSelectedShape();
                if (selected == null) return;

                handleShapeSelection(selected, client);
            }
        });

        // Handle Click Activation Method
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {

            Shape selected = this.shapeConfigService.getSelectedShape();
            if (selected == null) return ActionResult.PASS;

            if (selected.equals(Shape.LINE)) {
                if (config.activationMethod.equals(ActivationMethod.CLICK)) {
                    return handleClick(player, hitResult.getBlockPos());
                } else {
                    return ActionResult.PASS;
                }
            } else {
                return ActionResult.PASS;
            }
        });

        // Block rendering
        WorldRenderEvents.LAST.register(context -> {

            Shape selected = this.shapeConfigService.getSelectedShape();
            if (selected == null) return;

            handleRender(selected, context);
        });

        instance = this;
    }

    private void handleShapeSelection(Shape selected, MinecraftClient client) {
        if (selected.equals(Shape.LINE)) {
            if (config.activationMethod.equals(ActivationMethod.STANDING)) {
                handleStanding(client.player);
            } else {
                handleClickActivation(client.player);
            }
        } else {
            handleShape(client.player);
        }
    }

    private void handleStanding(PlayerEntity player) {
        if (standStep.get().equals(ActivationStep.FINISHED)) {

            BlockPos firstPos = BlockPos.ofFloored(player.getPos());
            firstPosition = Vec3d.of(firstPos);
            printFirst(player);

            if (config.showDirMessages) {
                player.sendMessage(
                        Text.literal("Activate again for second position...")
                                .formatted(Random.chatColorToFormat(config.chatColor)),
                        !config.msgDisplayLocation.equals(MessageDisplay.CHAT)
                );
            }

            standStep.set(ActivationStep.STARTED);

        } else if (standStep.get().equals(ActivationStep.STARTED)) {

            BlockPos secondPos = BlockPos.ofFloored(player.getPos());
            secondPosition = Vec3d.of(secondPos);

            printSecond(player);

            if (!this.shapeConfigService.canPlaceLine()) {
                firstPosition = null;
                secondPosition = null;

                standStep.set(ActivationStep.FINISHED);
            } else {
                standStep.set(ActivationStep.DURING);
            }

        } else if (standStep.get().equals(ActivationStep.DURING)) {
            firstPosition = null;
            secondPosition = null;

            standStep.set(ActivationStep.FINISHED);
        }
    }

    private void handleClickActivation(PlayerEntity player) {
        if (clickStep.get().equals(ActivationStep.FINISHED)) {

            if (config.showDirMessages) {
                player.sendMessage(
                        Text.literal("Right click first position...")
                                .formatted(Random.chatColorToFormat(config.chatColor)),
                        !config.msgDisplayLocation.equals(MessageDisplay.CHAT)
                );
            }

            firstPosition = null;
            secondPosition = null;

            clickStep.set(ActivationStep.STARTED);

        } else if (clickStep.get().equals(ActivationStep.STARTED)) {

            if (config.showDirMessages) {
                player.sendMessage(
                        Text.literal("Block count aborted.")
                                .formatted(Random.chatColorToFormat(config.chatColor)),
                        !config.msgDisplayLocation.equals(MessageDisplay.CHAT)
                );
            }

            clickStep.set(ActivationStep.FINISHED);
            firstPosition = null;
            secondPosition = null;

        } else {
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

            if (config.showDirMessages) {
                player.sendMessage(
                        Text.literal("Right click second position...")
                                .formatted(Random.chatColorToFormat(config.chatColor)),
                        !config.msgDisplayLocation.equals(MessageDisplay.CHAT)
                );
            }

            return ActionResult.FAIL;

        } else if (clickStep.get().equals(ActivationStep.DURING)) {

            if (!this.shapeConfigService.canPlaceLine()) {
                secondPosition = new Vec3d(pos.getX(), pos.getY(), pos.getZ());

                printSecond(player);

                firstPosition = null;
                secondPosition = null;

                clickStep.set(ActivationStep.FINISHED);

                return ActionResult.FAIL;
            } else {
                if (secondPosition == null) {
                    secondPosition = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
                    printSecond(player);

                    return ActionResult.FAIL;
                } else {
                    return ActionResult.PASS;
                }
            }
        } else {
            return ActionResult.PASS;
        }
    }

    private void handleShape(PlayerEntity player) {

        if (shapeStep.get().equals(ActivationStep.STARTED)) {
            shapeStep.set(ActivationStep.DURING);

            if (config.activationMethod.equals(ActivationMethod.STANDING)) {
                firstPosition = player.getPos().subtract(new Vec3d(0, 1, 0));
            } else {
                firstPosition = blockRenderingService.getCrosshairBlockPos();
            }

            if (config.showDirMessages) {
                player.sendMessage(Text.literal("Activate again to destroy...")
                                .formatted(Random.chatColorToFormat(config.chatColor)),
                        !config.msgDisplayLocation.equals(MessageDisplay.CHAT));
            }

        } else if (shapeStep.get().equals(ActivationStep.DURING)) {
            shapeStep.set(ActivationStep.FINISHED);
            firstPosition = null;
            lookAxis = null;
        } else {
            shapeStep.set(ActivationStep.STARTED);

            if (config.showDirMessages) {
                player.sendMessage(Text.literal("Activate again to place...")
                                .formatted(Random.chatColorToFormat(config.chatColor)),
                        !config.msgDisplayLocation.equals(MessageDisplay.CHAT));
            }
        }
    }

    private void handleRender(Shape selected, WorldRenderContext context) {
        switch (selected) {
            case Shape.LINE:
                if (firstPosition != null) {
                    if (config.activationMethod.equals(ActivationMethod.STANDING)) {

                        BlockPos lockPos = null;
                        if (this.shapeConfigService.canPlaceLine()) {
                            if (secondPosition != null) {
                                lockPos = BlockPos.ofFloored(secondPosition);
                            }
                        }

                        blockRenderingService.renderStandingSelection(
                                context.matrixStack(),
                                firstPosition,
                                lockPos,
                                config);
                    } else {

                        BlockPos lockPos = null;
                        if (this.shapeConfigService.canPlaceLine()) {
                            if (secondPosition != null) {
                                lockPos = BlockPos.ofFloored(secondPosition);
                            }
                        }

                        blockRenderingService.renderClickSelection(
                                context.matrixStack(),
                                firstPosition,
                                lockPos,
                                config);
                    }
                }
                break;
            case Shape.CIRCLE:
                if (shapeStep.get().equals(ActivationStep.STARTED)) {
                    PlayerEntity player = MinecraftClient.getInstance().player;

                    if (player.isSneaking()) {
                        Vec3d crosshairPos = BlockRenderingService.getCrosshairBlockPos();
                        lookAxis = BlockCalculations.findLargestAxisDiff(crosshairPos,
                                new Vec3d(player.getPos().x, player.getPos().y - 1, player.getPos().z));
                    } else {
                        lookAxis = null;
                    }

                    blockRenderingService.renderCircle(context.matrixStack(), null, lookAxis, config);
                } else if (shapeStep.get().equals(ActivationStep.DURING)) {

                    blockRenderingService.renderCircle(context.matrixStack(), BlockPos.ofFloored(firstPosition), lookAxis, config);
                }
                break;
            case Shape.QUAD:
                if (shapeStep.get().equals(ActivationStep.STARTED)) {
                    blockRenderingService.renderQuad(context.matrixStack(), null, config);
                } else if (shapeStep.get().equals(ActivationStep.DURING)) {
                    blockRenderingService.renderQuad(context.matrixStack(), BlockPos.ofFloored(firstPosition), config);
                }

                break;
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
                    !config.msgDisplayLocation.equals(MessageDisplay.CHAT)
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
                    !config.msgDisplayLocation.equals(MessageDisplay.CHAT)
            );
        }

        int dist;
        if (this.shapeConfigService.isAxisAligned()) {
            if (!this.shapeConfigService.isTwoAxis()) {
                dist = BlockCalculations.calculateBlocksOne(firstPosition, secondPosition, isClick);
            } else {
                dist = BlockCalculations.calculateBlocksTwo(firstPosition, secondPosition, isClick);
            }
        } else {
            dist = BlockCalculations.calculateBlocksFree(firstPosition, secondPosition, isClick);
        }


        String distLong = "Distance: %s %s";
        String distShort = "D: %d";

        player.sendMessage(
                Text.literal((simplify ?
                                String.format(distShort, dist)
                                : String.format(distLong, dist, dist == 1 ? "block" : "blocks")))
                        .formatted(Random.chatColorToFormat(config.chatColor)),
                !config.msgDisplayLocation.equals(MessageDisplay.CHAT)
        );

    }

    public static BlockCounterClient getInstance() {
        return instance;
    }

    public ShapeConfigService getShapeConfigService() {
        return this.shapeConfigService;
    }

    public void shapeChanged() {
        this.firstPosition = null;
        this.secondPosition = null;
        this.clickStep.set(ActivationStep.FINISHED);
        this.shapeStep.set(ActivationStep.FINISHED);
        this.lookAxis = null;
    }

}