package azula.blockcounter.config.shape.gui;

import azula.blockcounter.BlockCounterClient;
import azula.blockcounter.config.shape.Shape;
import azula.blockcounter.config.shape.ShapeConfigService;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class ShapeConfigScreen extends Screen {

    private final Identifier BACKGROUND_TEXTURE = Identifier.of(BlockCounterClient.MOD_ID, "textures/gui/shape_config_background.png");

    private final ShapeConfigService configService;
    protected final Screen parent;

    private final int padding = 10;
    private final int ySpacing = 20;

    private final int configWidth = 104;
    private final int configHeight = 169;

    private int yStart;

    private ButtonWidget shapeButton;

    private CheckboxWidget linePlaceable;
    private CheckboxWidget isAxisAligned;
    private CheckboxWidget twoAxisWidget;

    private Slider quadWidthSlider;
    private Slider quadLengthSlider;
    private Slider quadHeightSlider;

    private Slider offsetXSlider;
    private Slider offsetYSlider;
    private Slider offsetZSlider;

    public ShapeConfigScreen(ShapeConfigService service, Screen currentScreen) {
        super(Text.of("Shape Config"));
        this.configService = service;
        this.parent = currentScreen;
    }

    @Override
    protected void init() {
        super.init();

        this.clearChildren();

        yStart = (this.height - this.configHeight) / 3 + padding;

        int buttonWidth = configWidth - 2 * padding;
        int buttonHeight = 2 * padding;

        // Shape selector
        this.shapeButton = ButtonWidget.builder(Text.of("Line"), btn -> {
            this.configService.cycleShape();
            this.init();
        }).position((this.width - this.configWidth) / 2 + padding, yStart)
                .size(buttonWidth, buttonHeight)
                .build();

        // Line options
        this.linePlaceable = CheckboxWidget.builder(Text.of("Placeable"), this.textRenderer)
                .pos(
                        (this.width - this.configWidth) / 2 + padding,
                        yStart + ySpacing + padding)
                .callback((btn, b) -> {
                    this.configService.setPlaceLine(b);
                    this.configService.setXOffset(0);
                    this.configService.setYOffset(0);
                    this.configService.setZOffset(0);
                })
                .checked(this.configService.canPlaceLine())
                .build();

        this.isAxisAligned = CheckboxWidget.builder(Text.of("Axis-Aligned"), this.textRenderer)
                .pos(
                        (this.width - this.configWidth) / 2 + padding,
                        yStart + 2 * ySpacing + padding)
                .callback((btn, b) -> this.configService.setAxisAligned(b))
                .checked(this.configService.isAxisAligned())
                .build();

        this.twoAxisWidget = CheckboxWidget.builder(Text.of("Dual-Axis"), this.textRenderer)
                .pos(
                        (this.width - this.configWidth) / 2 + padding,
                        yStart + 3 * ySpacing + padding)
                .callback((btn, b) -> this.configService.setTwoAxis(b))
                .checked(this.configService.isTwoAxis())
                .build();

        // Quad dimension sliders
        this.quadWidthSlider = new Slider(
                (this.width - this.configWidth) / 2 + padding,
                yStart + 3 * ySpacing + 2,
                buttonWidth,
                buttonHeight,
                Text.of("Width: 1"),
                0,
                1,
                50,
                (sldr, v) -> this.configService.setQuadWidth(v)
        );

        this.quadLengthSlider = new Slider(
                (this.width - this.configWidth) / 2 + padding,
                yStart + 4 * ySpacing + 5,
                buttonWidth,
                buttonHeight,
                Text.of("Length: 1"),
                0,
                1,
                50,
                (sldr, v) -> this.configService.setQuadLength(v)
        );

        this.quadHeightSlider = new Slider(
                (this.width - this.configWidth) / 2 + padding,
                yStart + 5 * ySpacing + 8,
                buttonWidth,
                buttonHeight,
                Text.of("Height: 1"),
                0,
                1,
                50,
                (sldr, v) -> this.configService.setQuadHeight(v)
        );

        // Offset sliders
        this.offsetXSlider = new Slider(
                (this.width - this.configWidth) / 2 + padding,
                yStart + 7 * ySpacing + padding + 2,
                buttonWidth,
                buttonHeight,
                Text.of("X: 0"),
                0.5,
                -50,
                50,
                (sldr, v) -> this.configService.setXOffset(v)
        );

        this.offsetYSlider = new Slider(
                (this.width - this.configWidth) / 2 + padding,
                yStart + 8 * ySpacing + padding + 5,
                buttonWidth,
                buttonHeight,
                Text.of("Y: 0"),
                0.5,
                -50,
                50,
                (sldr, v) -> this.configService.setYOffset(v)
        );

        this.offsetZSlider = new Slider(
                (this.width - this.configWidth) / 2 + padding,
                yStart + 9 * ySpacing + padding + 8,
                buttonWidth,
                buttonHeight,
                Text.of("Z: 0"),
                0.5,
                -50,
                50,
                (sldr, v) -> this.configService.setZOffset(v)
        );

        this.addDrawableChild(this.shapeButton);

        this.addDrawableChild(this.linePlaceable);
        this.addDrawableChild(this.isAxisAligned);
        this.addDrawableChild(this.twoAxisWidget);

        this.addDrawableChild(this.quadWidthSlider);
        this.addDrawableChild(this.quadLengthSlider);
        this.addDrawableChild(this.quadHeightSlider);

        this.addDrawableChild(this.offsetXSlider);
        this.addDrawableChild(this.offsetYSlider);
        this.addDrawableChild(this.offsetZSlider);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

//        this.renderBackground(context, BACKGROUND_TEXTURE);

        this.shapeButton.setMessage(Text.of(this.configService.getSelectedShape().toString()));

        boolean isLine = this.configService.getSelectedShape().equals(Shape.LINE);
        boolean isQuad = this.configService.getSelectedShape().equals(Shape.QUAD);

        this.linePlaceable.visible = isLine;
        this.isAxisAligned.visible = isLine;
        this.twoAxisWidget.visible = isLine && this.configService.isAxisAligned();

        this.quadWidthSlider.visible = isQuad;
        this.quadLengthSlider.visible = isQuad;
        this.quadHeightSlider.visible = isQuad;

        boolean showOffset = this.configService.canPlaceLine() || !isLine;

        this.offsetXSlider.visible = showOffset;
        this.offsetYSlider.visible = showOffset;
        this.offsetZSlider.visible = showOffset;

        if (showOffset) {
            this.offsetXSlider.setMessage(Text.of("X: " + this.configService.getXOffset()));
            this.offsetXSlider.setValue(this.configService.getXOffset());

            this.offsetYSlider.setMessage(Text.of("Y: " + this.configService.getYOffset()));
            this.offsetYSlider.setValue(this.configService.getYOffset());

            this.offsetZSlider.setMessage(Text.of("Z: " + this.configService.getZOffset()));
            this.offsetZSlider.setValue(this.configService.getZOffset());
        }

        if (isQuad) {
            int[] quadDims = this.configService.getDimensions();

            this.quadWidthSlider.setMessage(Text.of("Width: " + quadDims[0]));
            this.quadWidthSlider.setValue(quadDims[0]);

            this.quadLengthSlider.setMessage(Text.of("Length: " + quadDims[1]));
            this.quadLengthSlider.setValue(quadDims[1]);

            this.quadHeightSlider.setMessage(Text.of("Height: " + quadDims[2]));
            this.quadHeightSlider.setValue(quadDims[2]);
        }

        super.render(context, mouseX, mouseY, delta);

        if (isQuad) {

            Integer totalCount = BlockCounterClient.getInstance().getBlockRenderingService().getTotalQuadCount(configService.getDimensions());
            Integer renderCount = BlockCounterClient.getInstance().getBlockRenderingService().getRenderQuadCount(configService.getDimensions());

            context.drawText(this.textRenderer, "Total: " + totalCount + " block(s), " + renderCount + " shown",
                    (this.width - this.configWidth) / 2 + padding,
                    yStart + ySpacing + textRenderer.fontHeight,
                    0xFFFFFFFF,
                    true
            );

            context.drawText(this.textRenderer, "Dimensions",
                    (this.width - this.configWidth) / 2 + padding,
                    yStart + 2 * ySpacing + textRenderer.fontHeight,
                    0xFFFFFFFF,
                    true
            );
        }

        if (showOffset) {
            context.drawText(this.textRenderer, "Offset",
                    (this.width - this.configWidth) / 2 + padding,
                    yStart + 6 * ySpacing + textRenderer.fontHeight + padding,
                    0xFFFFFFFF,
                    true
            );
        }

    }

    // It seems like overriding this method eliminates the setting of
    // shouldCloseOnEscape
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        KeyBinding configKey = BlockCounterClient.configMenuKey;

        if (configKey.matchesKey(keyCode, scanCode) || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            assert client != null;
            client.setScreen(this.parent);
        }

        return true;
    }

    private void renderBackground(DrawContext context, Identifier background) {

        MatrixStack matrices = context.getMatrices();

        RenderSystem.enableBlend();

        matrices.push();
        context.drawTexture(RenderLayer::getGuiTextured, background, (width - configWidth) / 2, (height - configHeight) / 2,
                0, 0, configWidth, configHeight, 256, 256);
        matrices.pop();

        RenderSystem.disableBlend();
    }
}
