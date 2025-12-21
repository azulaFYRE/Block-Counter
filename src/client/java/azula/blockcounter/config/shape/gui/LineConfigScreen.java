package azula.blockcounter.config.shape.gui;

import azula.blockcounter.BlockCounterClient;
import azula.blockcounter.config.shape.LineConfigService;
import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;

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

    private ClickableWidget offsetXSlider;
    private ClickableWidget offsetYSlider;
    private ClickableWidget offsetZSlider;

    private final SimpleOption<Integer> offsetXOption;
    private final SimpleOption<Integer> offsetYOption;
    private final SimpleOption<Integer> offsetZOption;

    public LineConfigScreen(LineConfigService service, Screen currentScreen) {
        super(Text.of("Line Config"));
        this.configService = service;
        this.parent = currentScreen;

        this.offsetXOption = new SimpleOption<>("option.blockcounter.offset.x", SimpleOption.emptyTooltip(), (text, value) -> Text.of(text.getString() + " " + value),
                new SimpleOption.ValidatingIntSliderCallbacks(-50, 50, true),
                Codec.INT.xmap((value) -> value, (value) -> value), this.configService.getXOffset(), this.configService::setXOffset);

        this.offsetYOption = new SimpleOption<>("option.blockcounter.offset.y", SimpleOption.emptyTooltip(), (text, value) -> Text.of(text.getString() + " " + value),
                new SimpleOption.ValidatingIntSliderCallbacks(-50, 50, true),
                Codec.INT.xmap((value) -> value, (value) -> value), this.configService.getYOffset(), this.configService::setYOffset);

        this.offsetZOption = new SimpleOption<>("option.blockcounter.offset.z", SimpleOption.emptyTooltip(), (text, value) -> Text.of(text.getString() + " " + value),
                new SimpleOption.ValidatingIntSliderCallbacks(-50, 50, true),
                Codec.INT.xmap((value) -> value, (value) -> value), this.configService.getZOffset(), this.configService::setZOffset);
    }

    @Override
    protected void init() {
        super.init();

        yStart = (this.height - this.configHeight) / 2 + padding;

        int buttonWidth = configWidth - 2 * padding;

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

        this.offsetXSlider = offsetXOption.createWidget(
                client.options,
                (this.width - this.configWidth) / 2 + padding,
                yStart + 4 * ySpacing + 2,
                buttonWidth);

        this.offsetYSlider = offsetYOption.createWidget(
                client.options,
                (this.width - this.configWidth) / 2 + padding,
                yStart + 5 * ySpacing + 2,
                buttonWidth);

        this.offsetZSlider = offsetZOption.createWidget(
                client.options,
                (this.width - this.configWidth) / 2 + padding,
                yStart + 6 * ySpacing + 2,
                buttonWidth);

        this.addDrawableChild(placeable);
        this.addDrawableChild(axisAligned);
        this.addDrawableChild(twoAxis);

        this.addDrawableChild(this.offsetXSlider);
        this.addDrawableChild(this.offsetYSlider);
        this.addDrawableChild(this.offsetZSlider);

        this.twoAxisWidget = twoAxis;

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        this.renderBackground(context, BACKGROUND_TEXTURE);

        this.twoAxisWidget.visible = this.configService.isAxisAligned();

        boolean canPlace = this.configService.canPlaceLine();

        this.offsetXSlider.visible = canPlace;
        this.offsetYSlider.visible = canPlace;
        this.offsetZSlider.visible = canPlace;

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
    public boolean keyPressed(KeyInput keyCode) {
        KeyBinding configKey = BlockCounterClient.configMenuKey;

        if (configKey.matchesKey(keyCode) || keyCode.isEscape()) {
            assert client != null;
            client.setScreen(this.parent);
        }

        return true;
    }

    private void renderBackground(DrawContext context, Identifier background) {

        Matrix3x2fStack matrices = context.getMatrices();

        matrices.pushMatrix();
        context.drawTexture(RenderPipelines.GUI_TEXTURED, background, (width - configWidth) / 2, (height - configHeight) / 2,
                0, 0, configWidth, configHeight, 256, 256);
        matrices.popMatrix();

    }
}
