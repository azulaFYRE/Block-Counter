package azula.blockcounter.config.shape.gui;

import azula.blockcounter.BlockCounterClient;
import azula.blockcounter.config.shape.LineConfigService;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class LineConfigScreen extends Screen {

    private final Identifier BACKGROUND_TEXTURE = Identifier.fromNamespaceAndPath(BlockCounterClient.MOD_ID, "textures/gui/line_config_background.png");

    private final LineConfigService configService;
    protected final Screen parent;

    private final int padding = 10;
    private final int ySpacing = 20;

    private final int configWidth = 104;
    private final int configHeight = 169;

    private int yStart;

    private Checkbox twoAxisWidget;

    private IntSliderWidget offsetXSlider;
    private IntSliderWidget offsetYSlider;
    private IntSliderWidget offsetZSlider;

    public LineConfigScreen(LineConfigService service, Screen currentScreen) {
        super(Component.literal("Line Config"));
        this.configService = service;
        this.parent = currentScreen;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = configWidth - 2 * padding;
        int buttonHeight = 18;

        this.yStart = (this.height - this.configHeight) / 2 + padding;

        // Line options
        Checkbox placeable = Checkbox.builder(Component.literal("Placeable"), this.font)
                .pos(
                        (this.width - this.configWidth) / 2 + padding,
                        yStart)
                .onValueChange((_, b) -> {
                    this.configService.setPlaceLine(b);
                    this.configService.setXOffset(0);
                    this.configService.setYOffset(0);
                    this.configService.setZOffset(0);
                })
                .selected(this.configService.canPlaceLine())
                .build();

        Checkbox axisAligned = Checkbox.builder(Component.literal("Axis-Aligned"), this.font)
                .pos(
                        (this.width - this.configWidth) / 2 + padding,
                        yStart + ySpacing)
                .onValueChange((_, b) -> this.configService.setAxisAligned(b))
                .selected(this.configService.isAxisAligned())
                .build();

        Checkbox twoAxis = Checkbox.builder(Component.literal("Dual-Axis"), this.font)
                .pos(
                        (this.width - this.configWidth) / 2 + padding,
                        yStart + 2 * ySpacing)
                .onValueChange((_, b) -> this.configService.setTwoAxis(b))
                .selected(this.configService.isTwoAxis())
                .build();

        // Offset sliders
        this.offsetXSlider = new IntSliderWidget("X",
                (this.width - this.configWidth) / 2 + padding,
                yStart + 4 * ySpacing + 2,
                buttonWidth, buttonHeight,
                -50, 50,
                this.configService.getXOffset())
                .addValueListener(this.configService::setXOffset);

        this.offsetYSlider = new IntSliderWidget("Y",
                (this.width - this.configWidth) / 2 + padding,
                yStart + 5 * ySpacing + 2,
                buttonWidth, buttonHeight,
                -50, 50,
                this.configService.getYOffset())
                .addValueListener(this.configService::setYOffset);

        this.offsetZSlider = new IntSliderWidget("Z",
                (this.width - this.configWidth) / 2 + padding,
                yStart + 6 * ySpacing + 2,
                buttonWidth, buttonHeight,
                -50, 50,
                this.configService.getZOffset())
                .addValueListener(this.configService::setZOffset);

        this.addRenderableWidget(placeable);
        this.addRenderableWidget(axisAligned);
        this.addRenderableWidget(twoAxis);

        this.addRenderableWidget(this.offsetXSlider);
        this.addRenderableWidget(this.offsetYSlider);
        this.addRenderableWidget(this.offsetZSlider);

        this.twoAxisWidget = twoAxis;
    }

    // It seems like overriding this method eliminates the setting of
    // shouldCloseOnEscape
    @Override
    public boolean keyPressed(@NotNull KeyEvent keyCode) {
        KeyMapping configKey = BlockCounterClient.configMenuKey;

        if (configKey.matches(keyCode) || keyCode.isEscape()) {
            minecraft.setScreenAndShow(this.parent);
        }

        return true;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.blit(RenderPipelines.GUI_TEXTURED,
                BACKGROUND_TEXTURE,
                (width - configWidth) / 2,
                (height - configHeight) / 2,
                0, 0,
                configWidth, configHeight,
                256, 256);
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.twoAxisWidget.visible = this.configService.isAxisAligned();

        boolean canPlace = this.configService.canPlaceLine();

        this.offsetXSlider.visible = canPlace;
        this.offsetYSlider.visible = canPlace;
        this.offsetZSlider.visible = canPlace;

        if (canPlace) {
            graphics.text(this.font, "Offset",
                    (this.width - this.configWidth) / 2 + padding,
                    yStart + 3 * ySpacing + font.lineHeight,
                    0xFFFFFFFF,
                    true);
        }

        super.extractRenderState(graphics, mouseX, mouseY, a);
    }
}
