package azula.blockcounter.config.shape;

import azula.blockcounter.BlockCounterClient;
import azula.blockcounter.Shape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ShapeConfigScreen extends Screen {

    private final Identifier BACKGROUND_TEXTURE = Identifier.of(BlockCounterClient.MOD_ID, "textures/gui/shape_config_background.png");

    private final ShapeConfigService configService;

    protected final Screen parent;

    private final int configWidth = 176;
    private final int configHeight = 294;

    private int yStart;

    private final int padding = 10;
    private final int ySpacing = 15;

    private ButtonWidget shapeButton;

    private CheckboxWidget dualAxis;

    private Slider qLength;
    private Slider qWidth;
    private Slider qHeight;

    private Slider cRadius;
    private Slider cHeight;

    private Slider offsetX;
    private Slider offsetY;
    private Slider offsetZ;

    private List<ClickableWidget> lineButtons = new ArrayList<>();
    private List<ClickableWidget> quadButtons = new ArrayList<>();
    private List<ClickableWidget> circleButtons = new ArrayList<>();

    private CheckboxWidget calcUsingRendered;

    public ShapeConfigScreen(ShapeConfigService service, Screen currentScreen) {
        super(Text.of("Block Title"));
        this.configService = service;
        this.parent = currentScreen;
    }

    @Override
    protected void init() {
        super.init();

        yStart = (this.height - this.configHeight) / 2 + padding;

        int buttonWidth = configWidth - 20;
        int buttonHeight = 20;
        ButtonWidget shape = ButtonWidget.builder(
                        Text.of(this.configService.getSelectedShape().toString()),
                        (button) -> this.cycleShape())
                .dimensions(
                        (this.width - this.configWidth) / 2 + padding,
                        yStart + padding,
                        buttonWidth,
                        buttonHeight)
                .build();

        CheckboxWidget placeable = CheckboxWidget.builder(Text.of("Placeable"), this.textRenderer)
                .pos(
                        (this.width - this.configWidth) / 2 + padding,
                        yStart + 2 * (ySpacing + padding / 2))
                .callback((btn, b) -> this.configService.setPlaceLine(b))
                .checked(this.configService.canPlaceLine())
                .build();

        CheckboxWidget axisAligned = CheckboxWidget.builder(Text.of("Axis-Aligned"), this.textRenderer)
                .pos(
                        (this.width - this.configWidth) / 2 + padding,
                        yStart + 3 * (ySpacing + padding / 2))
                .callback((btn, b) -> this.configService.setAxisAligned(b))
                .checked(this.configService.isAxisAligned())
                .build();

        CheckboxWidget twoAxis = CheckboxWidget.builder(Text.of("Dual-Axis"), this.textRenderer)
                .pos(
                        (this.width - this.configWidth) / 2 + padding,
                        yStart + 4 * (ySpacing + padding / 2))
                .callback((btn, b) -> this.configService.setTwoAxis(b))
                .checked(this.configService.isTwoAxis())
                .build();

        Slider qLength = new Slider(
                (this.width - this.configWidth) / 2 + padding,
                yStart + 2 * (ySpacing + padding / 2) + textRenderer.fontHeight + 5,
                buttonWidth,
                buttonHeight,
                Text.of("Length: 1"),
                0,
                1,
                100,
                (sldr, v) -> this.configService.setQLength(v)
        );

        Slider qWidth = new Slider(
                (this.width - this.configWidth) / 2 + padding,
                yStart + 3 * (ySpacing + padding / 2) + textRenderer.fontHeight + 10,
                buttonWidth,
                buttonHeight,
                Text.of("Width: 1"),
                0,
                1,
                100,
                (sldr, v) -> this.configService.setQWidth(v)
        );

        Slider qHeight = new Slider(
                (this.width - this.configWidth) / 2 + padding,
                yStart + 4 * (ySpacing + padding / 2) + textRenderer.fontHeight + 15,
                buttonWidth,
                buttonHeight,
                Text.of("Height: 1"),
                0,
                1,
                100,
                (sldr, v) -> this.configService.setQHeight(v)
        );

        Slider cRadius = new Slider(
                (this.width - this.configWidth) / 2 + padding,
                yStart + 2 * (ySpacing + padding / 2) + textRenderer.fontHeight + 5,
                buttonWidth,
                buttonHeight,
                Text.of("Radius: 1"),
                0,
                1,
                100,
                (sldr, v) -> this.configService.setCRadius(v)
        );

        CheckboxWidget isSphere = CheckboxWidget.builder(Text.of("Sphere"), this.textRenderer)
                .pos(
                        (this.width - this.configWidth) / 2 + padding,
                        yStart + 3 * (ySpacing + padding / 2) + textRenderer.fontHeight + 10)
                .callback((btn, b) -> this.configService.setSphere(b))
                .checked(this.configService.isSphere())
                .build();

        Slider cHeight = new Slider(
                (this.width - this.configWidth) / 2 + padding,
                yStart + 4 * (ySpacing + padding / 2) + textRenderer.fontHeight + 15,
                buttonWidth,
                buttonHeight,
                Text.of("Height: 1"),
                0,
                1,
                100,
                (sldr, v) -> this.configService.setCHeight(v)
        );

        Slider offsetX = new Slider(
                (this.width - this.configWidth) / 2 + padding,
                yStart + 7 * (ySpacing + padding / 2) + 10,
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
                yStart + 8 * (ySpacing + padding / 2) + 15,
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
                yStart + 9 * (ySpacing + padding / 2) + 20,
                buttonWidth,
                buttonHeight,
                Text.of("Z: 0"),
                0.5,
                -50,
                50,
                (sldr, v) -> this.configService.setZOffset(v)
        );

        CheckboxWidget calcUsingRendered = CheckboxWidget.builder(Text.of("Only calculate rendered"), this.textRenderer)
                .pos(
                        (this.width - this.configWidth) / 2 + padding,
                        yStart + 11 * (ySpacing + padding / 2) + ySpacing)
                .callback((btn, b) -> this.configService.setCalcUsingRendered(b))
                .checked(this.configService.isCalcUsingRendered())
                .tooltip(Tooltip.of(Text.of("By default, all blocks that would be inside the entire shape are computed. " +
                        "With this option checked, only the blocks that you see rendered are added in the total.")))
                .build();


        this.shapeButton = shape;

        this.dualAxis = twoAxis;

        this.qLength = qLength;
        this.qWidth = qWidth;
        this.qHeight = qHeight;

        this.cRadius = cRadius;
        this.cHeight = cHeight;

        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;

        this.calcUsingRendered = calcUsingRendered;

        this.lineButtons.addAll(Arrays.asList(placeable, axisAligned, twoAxis));

        this.quadButtons.addAll(Arrays.asList(qLength, qWidth, qHeight));

        this.circleButtons.addAll(Arrays.asList(cRadius, isSphere, cHeight));

        this.addDrawableChild(shape);

        this.addDrawableChild(offsetX);
        this.addDrawableChild(offsetY);
        this.addDrawableChild(offsetZ);

        for (ClickableWidget b : lineButtons) {
            this.addDrawableChild(b);
        }

        for (ClickableWidget b : quadButtons) {
            this.addDrawableChild(b);
        }

        for (ClickableWidget b : circleButtons) {
            this.addDrawableChild(b);
        }

        this.addDrawableChild(calcUsingRendered);

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, BACKGROUND_TEXTURE);

        boolean isLine = this.configService.getSelectedShape().equals(Shape.LINE);

        this.lineButtons.forEach(b -> b.visible = this.configService.getSelectedShape().equals(Shape.LINE));
        this.quadButtons.forEach(b -> b.visible = this.configService.getSelectedShape().equals(Shape.QUAD));
        this.circleButtons.forEach(b -> b.visible = this.configService.getSelectedShape().equals(Shape.CIRCLE));

        this.dualAxis.visible = isLine && this.configService.isAxisAligned();

        this.cHeight.visible = this.configService.getSelectedShape().equals(Shape.CIRCLE) && !this.configService.isSphere();

        this.offsetX.visible = !isLine;
        this.offsetY.visible = !isLine;
        this.offsetZ.visible = !isLine;

        // Set slider texts and values
        this.qLength.setMessage(Text.of("Length: " + this.configService.getQLength()));
        this.qLength.setValue(this.configService.getQLength());

        this.qWidth.setMessage(Text.of("Width: " + this.configService.getQWidth()));
        this.qWidth.setValue(this.configService.getQWidth());

        this.qHeight.setMessage(Text.of("Height: " + this.configService.getQHeight()));
        this.qHeight.setValue(this.configService.getQHeight());

        this.cRadius.setMessage(Text.of("Radius: " + this.configService.getCRadius()));
        this.cRadius.setValue(this.configService.getCRadius());

        this.cHeight.setMessage(Text.of("Height: " + this.configService.getCHeight()));
        this.cHeight.setValue(this.configService.getCHeight());

        this.offsetX.setMessage(Text.of("X: " + this.configService.getXOffset()));
        this.offsetX.setValue(this.configService.getXOffset());

        this.offsetY.setMessage(Text.of("Y: " + this.configService.getYOffset()));
        this.offsetY.setValue(this.configService.getYOffset());

        this.offsetZ.setMessage(Text.of("Z: " + this.configService.getZOffset()));
        this.offsetZ.setValue(this.configService.getZOffset());

        this.calcUsingRendered.visible = !isLine;

        super.render(context, mouseX, mouseY, delta);

        context.drawText(this.textRenderer, "Shape",
                (this.width - this.configWidth) / 2 + padding,
                yStart,
                0xFFFFFFFF,
                true
        );

        if (!isLine) {
            context.drawText(this.textRenderer, "Dimensions",
                    (this.width - this.configWidth) / 2 + padding,
                    yStart + 2 * (ySpacing + padding / 2),
                    0xFFFFFFFF,
                    true
            );

            context.drawText(this.textRenderer, "Offset",
                    (this.width - this.configWidth) / 2 + padding,
                    yStart + 6 * (ySpacing + padding / 2) + 15,
                    0xFFFFFFFF,
                    true
            );

            context.drawText(this.textRenderer, "Total Blocks: " + this.configService.getTotalBlocks(),
                    (this.width - this.configWidth) / 2 + padding,
                    yStart + 12 * (ySpacing + padding / 2) + 20,
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

    public void renderBackground(DrawContext context, Identifier background) {

        MatrixStack matrices = context.getMatrices();

        RenderSystem.enableBlend();

        matrices.push();
        context.drawTexture(RenderLayer::getGuiTextured, background, (width - configWidth) / 2, (height - configHeight) / 2,
                0, 0, configWidth, configHeight, 256, 512);
        matrices.pop();

        RenderSystem.disableBlend();
    }

    private void cycleShape() {
        int shapeIndex = this.configService.getSelectedShape().ordinal();

        if (shapeIndex == Shape.values().length - 1) {
            shapeIndex = 0;
        } else {
            shapeIndex++;
        }

        this.configService.setSelectedShape(Shape.parseInt(shapeIndex));
        this.shapeButton.setMessage(Text.of(this.configService.getSelectedShape().toString()));
    }

}
