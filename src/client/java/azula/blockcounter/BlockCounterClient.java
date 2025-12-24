package azula.blockcounter;

import azula.blockcounter.config.BlockCounterModMenuConfig;
import azula.blockcounter.config.MessageDisplay;
import azula.blockcounter.config.shape.Shape;
import azula.blockcounter.config.shape.ShapeConfigService;
import azula.blockcounter.config.shape.ShapeConfigServiceImpl;
import azula.blockcounter.config.shape.gui.ShapeConfigScreen;
import azula.blockcounter.rendering.BlockRenderingService;
import azula.blockcounter.rendering.BlockRenderingServiceImpl;
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
import net.minecraft.util.hit.BlockHitResult;
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

    private static BlockCounterClient INSTANCE;

    private final BlockRenderingService blockRenderingService = new BlockRenderingServiceImpl();
    private final ShapeConfigService shapeConfigService = new ShapeConfigServiceImpl();

    public static KeyBinding activationKey;
    public static KeyBinding configMenuKey;

    private final AtomicReference<ActivationStep> standStep = new AtomicReference<>();
    private final AtomicReference<ActivationStep> clickStep = new AtomicReference<>();
    private final AtomicReference<ActivationStep> shapeStep = new AtomicReference<>();

    private BlockCounterModMenuConfig config;

    private Direction.Axis lookAxis = null;

    private Vec3d firstPosition;
    private Vec3d secondPosition;

    private boolean didRightClick = false;

    @Override
    public void onInitializeClient() {
        ConfigHolder<BlockCounterModMenuConfig> configHolder = AutoConfig
                .register(BlockCounterModMenuConfig.class, Toml4jConfigSerializer::new);

        INSTANCE = this;

        // Load config
        this.config = configHolder.getConfig();

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

        // Handle Activation Methods
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            // Check for menu key
            while (configMenuKey.wasPressed()) {
                client.setScreen(new ShapeConfigScreen(this.shapeConfigService, client.currentScreen));
            }

            while (activationKey.wasPressed()) {
                assert client.player != null;

                handleShapeActivation(client);
            }

            if (client.world != null && client.player != null) {
                boolean didClick = MinecraftClient.getInstance().mouse.wasRightButtonClicked();

                if (didClick && didClick != this.didRightClick && this.config.activationMethod.equals(ActivationMethod.CLICK)) {
                    PlayerEntity player = client.player;
                    BlockHitResult hitResult = (BlockHitResult) player.raycast(5, 0f, true);
                    handleClick(client.player, hitResult.getBlockPos());
                }

                this.didRightClick = didClick;
            }

        });

        UseBlockCallback.EVENT.register((playerEntity, world, hand, blockHitResult) -> {
            if (this.shapeConfigService.getSelectedShape().equals(Shape.LINE)) {
                if (this.config.activationMethod.equals(ActivationMethod.CLICK)) {
                    if (this.shapeConfigService.canPlaceLine()) {
                        if (this.clickStep.get().equals(ActivationStep.STARTED)) {
                            return ActionResult.FAIL;
                        } else if (this.clickStep.get().equals(ActivationStep.DURING) && this.secondPosition == null) {
                            return ActionResult.FAIL;
                        }
                    } else {
                        if (!this.clickStep.get().equals(ActivationStep.FINISHED)) {
                            return ActionResult.FAIL;
                        }
                    }
                }
            }

            return ActionResult.PASS;
        });

        // Block rendering
        WorldRenderEvents.LAST.register(this::handleRender);
    }

    private void handleShapeActivation(MinecraftClient client) {
        if (this.shapeConfigService.getSelectedShape().equals(Shape.LINE)) {
            if (this.config.activationMethod.equals(ActivationMethod.STANDING)) {
                handleStanding(client.player);
            } else {
                handleClickActivation(client.player);
            }
        } else {
            this.handleShape(client.player);
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

            player.sendMessage(Text.literal("Activate again to destroy...")
                            .formatted(Random.chatColorToFormat(config.chatColor)),
                    !config.msgDisplayLocation.equals(MessageDisplay.CHAT));

        } else if (shapeStep.get().equals(ActivationStep.DURING)) {
            shapeStep.set(ActivationStep.FINISHED);
            firstPosition = null;
            lookAxis = null;
        } else {
            shapeStep.set(ActivationStep.STARTED);

            player.sendMessage(Text.literal("Activate again to place...")
                            .formatted(Random.chatColorToFormat(config.chatColor)),
                    !config.msgDisplayLocation.equals(MessageDisplay.CHAT));
        }
    }

    private void handleRender(WorldRenderContext context) {
        switch (this.shapeConfigService.getSelectedShape()) {
            case LINE -> renderLine(context);
            case QUAD -> renderQuad(context);
            case SPHERE -> {}
        }
    }

    private void renderLine(WorldRenderContext context) {
        if (firstPosition != null) {

            BlockPos lockPos = null;

            if (this.shapeConfigService.canPlaceLine()) {
                if (secondPosition != null) {
                    lockPos = BlockPos.ofFloored(secondPosition);
                }
            }

            if (this.config.activationMethod.equals(ActivationMethod.STANDING)) {
                blockRenderingService.renderStandingSelection(
                        context,
                        firstPosition,
                        lockPos);
            } else {
                blockRenderingService.renderClickSelection(
                        context,
                        firstPosition,
                        lockPos);
            }
        }
    }

    private void renderQuad(WorldRenderContext context) {
        if (shapeStep.get().equals(ActivationStep.STARTED)) {
            blockRenderingService.renderQuad(context, null);
        } else if (shapeStep.get().equals(ActivationStep.DURING)) {
            blockRenderingService.renderQuad(context, BlockPos.ofFloored(firstPosition));
        }
    }

    private void handleStanding(PlayerEntity player) {
        if (standStep.get().equals(ActivationStep.FINISHED)) {

            BlockPos firstPos = BlockPos.ofFloored(player.getPos());
            firstPosition = Vec3d.of(firstPos);
            printFirst(player);

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
        } else {
            firstPosition = null;
            secondPosition = null;

            standStep.set(ActivationStep.FINISHED);
        }
    }

    private void handleClickActivation(PlayerEntity player) {
        if (clickStep.get().equals(ActivationStep.FINISHED)) {

            player.sendMessage(
                    Text.literal("Right click first position...")
                            .formatted(Random.chatColorToFormat(this.config.chatColor)),
                    !this.config.msgDisplayLocation.equals(MessageDisplay.CHAT)
            );

            firstPosition = null;
            secondPosition = null;

            clickStep.set(ActivationStep.STARTED);

        } else if (clickStep.get().equals(ActivationStep.STARTED)) {

            player.sendMessage(
                    Text.literal("Block count aborted.")
                            .formatted(Random.chatColorToFormat(this.config.chatColor)),
                    !this.config.msgDisplayLocation.equals(MessageDisplay.CHAT)
            );

            clickStep.set(ActivationStep.FINISHED);

            firstPosition = null;
            secondPosition = null;
        } else {
            clickStep.set(ActivationStep.FINISHED);

            firstPosition = null;
            secondPosition = null;
        }
    }

    private void handleClick(PlayerEntity player, BlockPos pos) {

        if (clickStep.get().equals(ActivationStep.STARTED)) {
            firstPosition = Vec3d.of(pos);
            secondPosition = null;

            printFirst(player);

            clickStep.set(ActivationStep.DURING);

            player.sendMessage(
                    Text.literal("Right click second position...")
                            .formatted(Random.chatColorToFormat(this.config.chatColor)),
                    !this.config.msgDisplayLocation.equals(MessageDisplay.CHAT)
            );

        } else if (clickStep.get().equals(ActivationStep.DURING)) {
            if (!this.shapeConfigService.canPlaceLine()) {
                secondPosition = Vec3d.of(pos);

                printSecond(player);

                firstPosition = null;
                secondPosition = null;

                clickStep.set(ActivationStep.FINISHED);
            } else {
                if (secondPosition == null) {
                    secondPosition = Vec3d.of(pos);
                    printSecond(player);
                }
            }
        }
    }

    private void printFirst(PlayerEntity player) {

        if (this.config.showPosMessages) {
            boolean simplify = this.config.simplifiedMessages;

            String first = Random.formatVec3d(firstPosition, "%,.2f");

            String firstPosLong = "First: %s";
            String firstPosShort = "1: %s";

            player.sendMessage(Text.literal(
                                    simplify ? String.format(firstPosShort, first) : String.format(firstPosLong, first))
                            .formatted(Random.chatColorToFormat(this.config.chatColor)),
                    !this.config.msgDisplayLocation.equals(MessageDisplay.CHAT)
            );
        }
    }

    private void printSecond(PlayerEntity player) {
        boolean simplify = this.config.simplifiedMessages;
        boolean isClick = this.config.activationMethod.equals(ActivationMethod.CLICK);

        if (this.config.showPosMessages) {
            String second = Random.formatVec3d(secondPosition, "%,.2f");

            String secondPosLong = "Second: %s";
            String secondPosShort = "2: %s";

            player.sendMessage(
                    Text.literal(simplify ? String.format(secondPosShort, second) : String.format(secondPosLong, second))
                            .formatted(Random.chatColorToFormat(this.config.chatColor)),
                    !this.config.msgDisplayLocation.equals(MessageDisplay.CHAT)
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
                        .formatted(Random.chatColorToFormat(this.config.chatColor)),
                !this.config.msgDisplayLocation.equals(MessageDisplay.CHAT)
        );

    }

    public ShapeConfigService getShapeConfigService() {
        return this.shapeConfigService;
    }

    public static BlockCounterClient getInstance() {
        return INSTANCE;
    }

    public BlockCounterModMenuConfig getConfig() {
        return this.config;
    }

    public BlockRenderingService getBlockRenderingService() {
        return this.blockRenderingService;
    }

    public void shapeChanged() {
        this.firstPosition = null;
        this.secondPosition = null;

        this.clickStep.set(ActivationStep.FINISHED);
        this.shapeStep.set(ActivationStep.FINISHED);

        this.lookAxis = null;
    }

}