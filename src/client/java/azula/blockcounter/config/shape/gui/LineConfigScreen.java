package azula.blockcounter.config.shape.gui;

import azula.blockcounter.BlockCounterClient;
import azula.blockcounter.config.shape.LineConfigService;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class LineConfigScreen extends Screen {

    private final Identifier BACKGROUND_TEXTURE = Identifier.of(BlockCounterClient.MOD_ID, "textures/gui/line_config_background.png");

    private final LineConfigService configService;
    protected final Screen parent;

    private final int padding = 10;
    private final int ySpacing = 20;

    private final int configWidth = 104;
    private final int configHeight = 169;

    private int yStart;

    private CheckboxWidget twoAxisWidget;

    private Slider offsetXSlider;
    private Slider offsetYSlider;
    private Slider offsetZSlider;

    public LineConfigScreen(LineConfigService service, Screen currentScreen) {
        super(Text.of("Line Config"));
        this.configService = service;
        this.parent = currentScreen;
    }

    @Override
    protected void init() {
        super.init();

        yStart = (this.height - this.configHeight) / 2 + padding;

        int buttonWidth = configWidth - 2 * padding;
        int buttonHeight = 2 * padding;

        // Line options
        CheckboxWidget placeable = CheckboxWidget.builder(Text.of("Placeable"), this.textRenderer)
                .pos(
                        (this.width - this.configWidth) / 2 + padding,
                        yStart)
                .callback((btn, b) -> {
                    this.configService.setPlaceLine(b);
                    this.configService.setXOffset(0);
                    this.configService.setYOffset(0);
                    this.configService.setZOffset(0);
                })
                .checked(this.configService.canPlaceLine())
                .build();

        CheckboxWidget axisAligned = CheckboxWidget.builder(Text.of("Axis-Aligned"), this.textRenderer)
                .pos(
                        (this.width - this.configWidth) / 2 + padding,
                        yStart + ySpacing)
                .callback((btn, b) -> this.configService.setAxisAligned(b))
                .checked(this.configService.isAxisAligned())
                .build();

        CheckboxWidget twoAxis = CheckboxWidget.builder(Text.of("Dual-Axis"), this.textRenderer)
                .pos(
                        (this.width - this.configWidth) / 2 + padding,
                        yStart + 2 * ySpacing)
                .callback((btn, b) -> this.configService.setTwoAxis(b))
                .checked(this.configService.isTwoAxis())
                .build();


        // Offset sliders
        Slider offsetX = new Slider(
                (this.width - this.configWidth) / 2 + padding,
                yStart + 4 * ySpacing + 2,
                buttonWidth,
                buttonHeight,
                Text.of("X: 0"),
                0.5,
                -50,
                50,
                (sldr, v) -> this.configService.setXOffset(v)
        );

        Slider offsetY = new Slider(
                (this.width - this.configWidth) / 2 + padding,
                yStart + 5 * ySpacing + 2,
                buttonWidth,
                buttonHeight,
                Text.of("Y: 0"),
                0.5,
                -50,
                50,
                (sldr, v) -> this.configService.setYOffset(v)
        );

        Slider offsetZ = new Slider(
                (this.width - this.configWidth) / 2 + padding,
                yStart + 6 * ySpacing + 2,
                buttonWidth,
                buttonHeight,
                Text.of("Z: 0"),
                0.5,
                -50,
                50,
                (sldr, v) -> this.configService.setZOffset(v)
        );

        this.addDrawableChild(placeable);
        this.addDrawableChild(axisAligned);
        this.addDrawableChild(twoAxis);

        this.addDrawableChild(offsetX);
        this.addDrawableChild(offsetY);
        this.addDrawableChild(offsetZ);

        this.twoAxisWidget = twoAxis;

        this.offsetXSlider = offsetX;
        this.offsetYSlider = offsetY;
        this.offsetZSlider = offsetZ;

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        this.renderBackground(context, BACKGROUND_TEXTURE);

        this.twoAxisWidget.visible = this.configService.isAxisAligned();

        boolean canPlace = this.configService.canPlaceLine();

        this.offsetXSlider.visible = canPlace;
        this.offsetYSlider.visible = canPlace;
        this.offsetZSlider.visible = canPlace;

        if (canPlace) {
            this.offsetXSlider.setMessage(Text.of("X: " + this.configService.getXOffset()));
            this.offsetXSlider.setValue(this.configService.getXOffset());

            this.offsetYSlider.setMessage(Text.of("Y: " + this.configService.getYOffset()));
            this.offsetYSlider.setValue(this.configService.getYOffset());

            this.offsetZSlider.setMessage(Text.of("Z: " + this.configService.getZOffset()));
            this.offsetZSlider.setValue(this.configService.getZOffset());
        }

        super.render(context, mouseX, mouseY, delta);

        if (canPlace) {
            context.drawText(this.textRenderer, "Offset",
                    (this.width - this.configWidth) / 2 + padding,
                    yStart + 3 * ySpacing + textRenderer.fontHeight,
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
