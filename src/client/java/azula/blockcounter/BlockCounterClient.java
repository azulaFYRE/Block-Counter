package azula.blockcounter;

import azula.blockcounter.config.BlockCounterModMenuConfig;
import azula.blockcounter.config.MessageDisplay;
import azula.blockcounter.config.shape.LineConfigService;
import azula.blockcounter.config.shape.LineConfigServiceImpl;
import azula.blockcounter.config.shape.gui.LineConfigScreen;
import azula.blockcounter.rendering.BlockRenderingService;
import azula.blockcounter.rendering.BlockRenderingServiceImpl;
import azula.blockcounter.util.BlockCalculations;
import azula.blockcounter.util.Random;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class BlockCounterClient implements ClientModInitializer {
    public static final String MOD_ID = "block-counter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static BlockCounterClient INSTANCE;

    private final BlockRenderingService blockRenderingService = new BlockRenderingServiceImpl();
    private final LineConfigService lineConfigService = new LineConfigServiceImpl();

    public static KeyMapping activationKey;
    public static KeyMapping configMenuKey;

    private final AtomicReference<ActivationStep> standStep = new AtomicReference<>();
    private final AtomicReference<ActivationStep> clickStep = new AtomicReference<>();

    private BlockCounterModMenuConfig config;

    private Vec3 firstPosition;
    private Vec3 secondPosition;

    private boolean didRightClick = false;
    private boolean activateKeyDown = false;

    @Override
    public void onInitializeClient() {
        ConfigHolder<BlockCounterModMenuConfig> configHolder = AutoConfig
                .register(BlockCounterModMenuConfig.class, Toml4jConfigSerializer::new);

        INSTANCE = this;

        // Load config
        this.config = configHolder.getConfig();

        // Key binding
        KeyMapping.Category blockCounterCategory = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("blockcounter", "category"));

        // Grab activation keyBinding
        activationKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "text.blockcounter.option.activationKey",
                GLFW.GLFW_KEY_COMMA,
                blockCounterCategory
        ));

        // Grab config menu keyBinding
        configMenuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "text.blockcounter.option.configMenuKey",
                GLFW.GLFW_KEY_DELETE,
                blockCounterCategory
        ));

        // Handle activation key press
        standStep.set(ActivationStep.FINISHED);
        clickStep.set(ActivationStep.FINISHED);

        // Handle Standing Activation Method
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            // Check for menu key
            if (configMenuKey.isDown()) {
                client.setScreenAndShow(new LineConfigScreen(this.lineConfigService, client.gui.screen()));
            }

            if (activationKey.isDown() && !this.activateKeyDown) {
                if (config.activationMethod.equals(ActivationMethod.STANDING)) {
                    handleStanding(client.player);
                } else {
                    handleClickActivation(client.player);
                }
            }

            this.activateKeyDown = activationKey.isDown();

            if (client.player != null) {
                boolean didClick = client.mouseHandler.isRightPressed();

                if (didClick && !this.didRightClick && config.activationMethod.equals(ActivationMethod.CLICK)) {
                    LocalPlayer player = client.player;
                    HitResult hitResult = player.raycastHitResult(0, Objects.requireNonNull(client.getCameraEntity()));

                    Vec3 hitPos;

                    if (hitResult.getType().equals(HitResult.Type.BLOCK)) {
                        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                        hitPos = new Vec3(blockHitResult.getBlockPos());
                    } else {
                        hitPos = new Vec3(Random.toIntVec(hitResult.getLocation()));
                    }

                    handleClick(client.player, hitPos);
                }

                this.didRightClick = didClick;
            }

        });

        UseBlockCallback.EVENT.register((_, _, _, _) -> {
            if (this.config.activationMethod.equals(ActivationMethod.CLICK)) {
                if (this.lineConfigService.canPlaceLine()) {
                    if (this.clickStep.get().equals(ActivationStep.STARTED)) {
                        return InteractionResult.FAIL;
                    } else if (this.clickStep.get().equals(ActivationStep.DURING) && this.secondPosition == null) {
                        return InteractionResult.FAIL;
                    } else {
                        return InteractionResult.PASS;
                    }
                } else {
                    if (!this.clickStep.get().equals(ActivationStep.FINISHED)) {
                        return InteractionResult.FAIL;
                    } else {
                        return InteractionResult.PASS;
                    }
                }
            } else {
                return InteractionResult.PASS;
            }

        });

        // Extraction phase here
        LevelExtractionEvents.END_EXTRACTION.register(context -> {

            if (firstPosition == null) return;

            BlockPos lockPos = null;

            if (this.lineConfigService.canPlaceLine()) {
                if (secondPosition != null) {
                    lockPos = new BlockPos(Random.toIntVec(secondPosition));
                }
            }

            if (config.activationMethod.equals(ActivationMethod.STANDING)) {
                blockRenderingService.extractStandingSelection(
                        context,
                        firstPosition,
                        lockPos);
            } else {
                blockRenderingService.extractClickSelection(
                        context,
                        firstPosition,
                        lockPos);
            }
        });

        // Block rendering
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(_ -> {
            if (firstPosition == null) return;

            if (this.lineConfigService.canPlaceLine()) {
                if (this.config.activationMethod.equals(ActivationMethod.STANDING)) {
                    if (this.standStep.get().equals(ActivationStep.FINISHED)) {
                        return;
                    }
                } else {
                    if (this.clickStep.get().equals(ActivationStep.FINISHED)) {
                        return;
                    }
                }
            }

            this.blockRenderingService.renderSelection(config.renderType);

        });
    }

    private void handleStanding(LocalPlayer player) {
        if (standStep.get().equals(ActivationStep.FINISHED)) {

            BlockPos firstPos = player.getOnPos();
            firstPosition = new Vec3(firstPos);
            printFirst(player);

            standStep.set(ActivationStep.STARTED);

        } else if (standStep.get().equals(ActivationStep.STARTED)) {

            BlockPos secondPos = player.getOnPos();
            secondPosition = new Vec3(secondPos);

            printSecond(player);

            if (!this.lineConfigService.canPlaceLine()) {
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

    private void handleClickActivation(LocalPlayer player) {
        if (clickStep.get().equals(ActivationStep.FINISHED)) {

            if (config.msgDisplayLocation.equals(MessageDisplay.CHAT)) {
                player.sendSystemMessage(Component.literal("Right click first position...")
                        .withStyle(Random.chatColorToFormat(config.chatColor)));
            } else {
                player.sendOverlayMessage(Component.literal("Right click first position...")
                        .withStyle(Random.chatColorToFormat(config.chatColor)));
            }

            firstPosition = null;
            secondPosition = null;

            clickStep.set(ActivationStep.STARTED);

        } else if (clickStep.get().equals(ActivationStep.STARTED)) {

            if (config.msgDisplayLocation.equals(MessageDisplay.CHAT)) {
                player.sendSystemMessage(Component.literal("Block count aborted.")
                        .withStyle(Random.chatColorToFormat(config.chatColor)));
            } else {
                player.sendOverlayMessage(Component.literal("Block count aborted.")
                        .withStyle(Random.chatColorToFormat(config.chatColor)));
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

    private void handleClick(LocalPlayer player, Vec3 pos) {

        if (clickStep.get().equals(ActivationStep.STARTED)) {
            firstPosition = pos;
            secondPosition = null;

            printFirst(player);

            clickStep.set(ActivationStep.DURING);

            if (config.msgDisplayLocation.equals(MessageDisplay.CHAT)) {
                player.sendSystemMessage(Component.literal("Right click second position...")
                        .withStyle(Random.chatColorToFormat(config.chatColor)));
            } else {
                player.sendOverlayMessage(Component.literal("Right click second position...")
                        .withStyle(Random.chatColorToFormat(config.chatColor)));
            }

        } else if (clickStep.get().equals(ActivationStep.DURING)) {
            if (!this.lineConfigService.canPlaceLine()) {
                secondPosition = pos;

                printSecond(player);

                firstPosition = null;
                secondPosition = null;

                clickStep.set(ActivationStep.FINISHED);
            } else {
                if (secondPosition == null) {
                    secondPosition = pos;
                    printSecond(player);
                }
            }
        }
    }

    private void printFirst(LocalPlayer player) {

        if (config.showPosMessages) {
            boolean simplify = config.simplifiedMessages;

            String first = Random.formatVec3(firstPosition, "%,.2f");

            String firstPosLong = "First: %s";
            String firstPosShort = "1: %s";

            MutableComponent chatMsg = Component.literal(
                    simplify ? String.format(firstPosShort, first) : String.format(firstPosLong, first))
                    .withStyle(Random.chatColorToFormat(config.chatColor));

            if (config.msgDisplayLocation.equals(MessageDisplay.CHAT)) {
                player.sendSystemMessage(chatMsg);
            } else {
                player.sendOverlayMessage(chatMsg);
            }
        }
    }

    private void printSecond(LocalPlayer player) {
        boolean simplify = config.simplifiedMessages;
        boolean isClick = config.activationMethod.equals(ActivationMethod.CLICK);

        if (config.showPosMessages) {
            String second = Random.formatVec3(secondPosition, "%,.2f");

            String secondPosLong = "Second: %s";
            String secondPosShort = "2: %s";

            MutableComponent chatMsg = Component.literal(
                    simplify ? String.format(secondPosShort, second) : String.format(secondPosLong, second))
                    .withStyle(Random.chatColorToFormat(config.chatColor));

            if (config.msgDisplayLocation.equals(MessageDisplay.CHAT)) {
                player.sendSystemMessage(chatMsg);
            } else {
                player.sendOverlayMessage(chatMsg);
            }
        }

        int dist;
        if (this.lineConfigService.isAxisAligned()) {
            if (!this.lineConfigService.isTwoAxis()) {
                dist = BlockCalculations.calculateBlocksOne(firstPosition, secondPosition, isClick);
            } else {
                dist = BlockCalculations.calculateBlocksTwo(firstPosition, secondPosition, isClick);
            }
        } else {
            dist = BlockCalculations.calculateBlocksFree(firstPosition, secondPosition, isClick);
        }

        String distLong = "Distance: %s %s";
        String distShort = "D: %d";

        MutableComponent chatMsg = Component.literal((simplify ?
                        String.format(distShort, dist)
                        : String.format(distLong, dist, dist == 1 ? "block" : "blocks")))
                .withStyle(Random.chatColorToFormat(config.chatColor));

        if (config.msgDisplayLocation.equals(MessageDisplay.CHAT)) {
            player.sendSystemMessage(chatMsg);
        } else {
            player.sendOverlayMessage(chatMsg);
        }
    }

    public LineConfigService getLineConfigService() {
        return this.lineConfigService;
    }

    public static BlockCounterClient getInstance() {
        return INSTANCE;
    }

    public BlockCounterModMenuConfig getConfig() {
        return this.config;
    }

}