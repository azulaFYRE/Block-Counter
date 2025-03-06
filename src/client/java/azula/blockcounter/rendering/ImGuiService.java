package azula.blockcounter.rendering;

import azula.blockcounter.BlockCounterClient;
import azula.blockcounter.Shape;
import azula.blockcounter.config.ImGuiConfig;
import azula.blockcounter.config.ImGuiConfigService;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import imgui.type.ImInt;

import java.io.IOException;

public class ImGuiService {

    private final static ImGuiImplGlfw imGuiGLFW = new ImGuiImplGlfw();
    private final static ImGuiImplGl3 imGuiGL3 = new ImGuiImplGl3();

    private final static String[] shapeOptions = {"Line", "Quad", "Circle"};

    public static ImInt selectedShape = new ImInt(0);

    public static ImBoolean axisAligned = new ImBoolean(true);
    public static ImBoolean twoAxis = new ImBoolean(false);

    // Sliders store the value in an array, don't ask me why
    // Quad
    public final static int[] length = {1};
    public final static int[] width = {1};
    public final static int[] height = {1};

    // Circle
    public final static int[] radius = {1};
    public final static int[] circleHeight = {1};

    // Offset
    public final static int[] xOffset = {0};
    public final static int[] yOffset = {0};
    public final static int[] zOffset = {0};

    // Slider Values
    private final static int MIN = 1;
    private final static int MAX = 100;
    private final static int OFFSET_MIN = -100;
    private final static int OFFSET_MAX = 100;

    private static int totalBlocks = 1;

    private static float[] textColor;
    private static float[] accentColor;
    private static float[] accentColorL1;
    private static float[] accentColorL2;
    private static float[] accentColorD1;
    private static float[] backgroundColor;

    private static ImGuiConfig config;

    public static void init(final long window) throws IOException {

        ImGui.createContext();

        final ImGuiIO configData = ImGui.getIO();
        configData.setIniFilename(null);
        configData.setFontGlobalScale(1.0f);

        configData.setConfigFlags(ImGuiConfigFlags.DockingEnable);

        // Load menu settings here
        config = ImGuiConfigService.loadConfigFromDisk();

        if (config == null) {
            config = new ImGuiConfig();
            ImVec4 defaultTabColor = ImGui.getStyleColorVec4(ImGuiCol.Tab);
            ImVec4 defaultTextColor = ImGui.getStyleColorVec4(ImGuiCol.Text);
            ImVec4 defaultBGColor = ImGui.getStyleColorVec4(ImGuiCol.WindowBg);

            // Accent color will be based off of tab color, other values will be adjusted based on this
            accentColor = imVec4ToFloat(defaultTabColor);
            textColor = imVec4ToFloat(defaultTextColor);
            backgroundColor = imVec4ToFloat(defaultBGColor);

            config.setAccentColor(accentColor);
            config.setTextColor(textColor);
            config.setBackgroundColor(backgroundColor);

            ImGuiConfigService.saveConfigToDisk(config);
        } else {
            accentColor = config.getAccentColor();
            textColor = config.getTextColor();
            backgroundColor = config.getBackgroundColor();
        }

        accentColorL1 = offsetFloatColor(accentColor, 0.1f);
        accentColorL2 = offsetFloatColor(accentColor, 0.2f);
        accentColorD1 = offsetFloatColor(accentColor, -0.1f);

        imGuiGLFW.init(window, true);
        imGuiGL3.init();
    }

    public static void draw(final RenderInterface runnable) {

        imGuiGL3.newFrame();
        imGuiGLFW.newFrame();
        ImGui.newFrame();

        runnable.render(ImGui.getIO());

        ImGui.render();
        imGuiGL3.renderDrawData(ImGui.getDrawData());

    }

    public static void dispose() {
        imGuiGL3.shutdown();

        ImGui.destroyContext();
    }

    public static void renderMenu() throws IOException {

        float[] lastColor = accentColor.clone();

        // DON'T FORGET TO POP!!!!
        // Background
        ImGui.pushStyleColor(ImGuiCol.WindowBg, floatToImVec4(backgroundColor));

        // Pure Accents
        ImGui.pushStyleColor(ImGuiCol.TitleBgActive, floatToImVec4(accentColor));
        ImGui.pushStyleColor(ImGuiCol.Border, floatToImVec4(accentColor));
        ImGui.pushStyleColor(ImGuiCol.Button, floatToImVec4(accentColor));
        ImGui.pushStyleColor(ImGuiCol.ScrollbarBg, floatToImVec4(accentColor));
        ImGui.pushStyleColor(ImGuiCol.FrameBg, floatToImVec4(accentColor));
        ImGui.pushStyleColor(ImGuiCol.Tab, floatToImVec4(accentColor));

        // Adjusted Accents
        ImGui.pushStyleColor(ImGuiCol.CheckMark, floatToImVec4(accentColorL2));

        ImGui.pushStyleColor(ImGuiCol.TabHovered, floatToImVec4(accentColorL2));
        ImGui.pushStyleColor(ImGuiCol.TabActive, floatToImVec4(accentColorL1));

        ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, floatToImVec4(accentColorD1));
        ImGui.pushStyleColor(ImGuiCol.FrameBgActive, floatToImVec4(accentColorL1));

        ImGui.pushStyleColor(ImGuiCol.SliderGrab, floatToImVec4(accentColorL1));
        ImGui.pushStyleColor(ImGuiCol.SliderGrabActive, floatToImVec4(accentColorL2));

        ImGui.pushStyleColor(ImGuiCol.ResizeGrip, floatToImVec4(accentColorD1));
        ImGui.pushStyleColor(ImGuiCol.ResizeGripActive, floatToImVec4(accentColorL1));
        ImGui.pushStyleColor(ImGuiCol.ResizeGripHovered, floatToImVec4(accentColorL2));

        ImGui.pushStyleColor(ImGuiCol.ScrollbarGrab, floatToImVec4(accentColorD1));
        ImGui.pushStyleColor(ImGuiCol.ScrollbarGrabHovered, floatToImVec4(accentColorL1));
        ImGui.pushStyleColor(ImGuiCol.ScrollbarGrabActive, floatToImVec4(accentColorL2));

        ImGui.pushStyleColor(ImGuiCol.ButtonActive, floatToImVec4(accentColorL1));
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, floatToImVec4(accentColorL2));

        // Combo options
        ImGui.pushStyleColor(ImGuiCol.Header, floatToImVec4(accentColorD1));
        ImGui.pushStyleColor(ImGuiCol.HeaderActive, floatToImVec4(accentColorL2));
        ImGui.pushStyleColor(ImGuiCol.HeaderHovered, floatToImVec4(accentColorL1));

        ImGui.pushStyleColor(ImGuiCol.Separator, floatToImVec4(accentColorD1));

        // Text
        ImGui.pushStyleColor(ImGuiCol.Text, floatToImVec4(textColor));

        ImGui.setNextWindowSize(new ImVec2(320, 220), ImGuiCond.Once);

        int pushedStyleCount = 27;

        if (ImGui.begin("Block Counter", null, ImGuiWindowFlags.NoCollapse)) {

            // Tabs
            if (ImGui.beginTabBar("MenuTabs", ImGuiTabBarFlags.NoCloseWithMiddleMouseButton)) {

                // Shapes
                if (ImGui.beginTabItem("Shapes")) {
                    ImGui.text("Shape");

                    // empty string makes dropdown break :/
                    if (ImGui.combo(" ", selectedShape, shapeOptions)) {
                        BlockCounterClient.getInstance().shapeChanged();
                    }

                    Shape selected = Shape.parseInt(selectedShape.get());

                    // Line Config
                    if(selected.equals(Shape.LINE)) {
                        ImGui.checkbox("Axis-aligned", axisAligned);

                        if (axisAligned.get()) {
                            ImGui.checkbox("Dual-axis", twoAxis);
                        }
                    }

                    // Quad Config
                    if (selected.equals(Shape.QUAD)) {
                        ImGui.sliderInt("Length", length, MIN, MAX, ImGuiSliderFlags.AlwaysClamp);
                        ImGui.sliderInt("Width", width, MIN, MAX, ImGuiSliderFlags.AlwaysClamp);
                        ImGui.sliderInt("Height", height, MIN, MAX, ImGuiSliderFlags.AlwaysClamp);
                    }

                    // Circle Config
                    if (selected.equals(Shape.CIRCLE)) {
                        ImGui.sliderInt("Radius", radius, MIN, MAX, ImGuiSliderFlags.AlwaysClamp);
                        ImGui.sliderInt("Height", circleHeight, MIN, MAX, ImGuiSliderFlags.AlwaysClamp);
                    }

                    // Offset
                    if (!selected.equals(Shape.LINE)) {
                        ImGui.separator();
                        ImGui.text("Offset");
                        ImGui.sliderInt("X", xOffset, OFFSET_MIN, OFFSET_MAX, ImGuiSliderFlags.AlwaysClamp);
                        ImGui.sliderInt("Y", yOffset, OFFSET_MIN, OFFSET_MAX, ImGuiSliderFlags.AlwaysClamp);
                        ImGui.sliderInt("Z", zOffset, OFFSET_MIN, OFFSET_MAX, ImGuiSliderFlags.AlwaysClamp);

                        ImGui.separator();

                        totalBlocks = switch (selected) {
                            case LINE -> 1;
                            case QUAD -> length[0] * width[0] * height[0];
                            case CIRCLE -> 1;
                            default -> 1;
                        };

                        ImGui.text("Total Blocks: " + totalBlocks);
                    }

                    ImGui.endTabItem();
                }

                // Menu Settings
                if (ImGui.beginTabItem("Menu Config")) {

                    ImGui.text("Accent Color");
                    ImGui.colorEdit4("Accent Color", accentColor, ImGuiColorEditFlags.NoLabel);

                    // Update altered accent colors if changed
                    if (!colorFloatsEqual(accentColor, lastColor)) {
                        accentColorL1 = offsetFloatColor(accentColor, 0.15f);
                        accentColorL2 = offsetFloatColor(accentColor, 0.3f);
                        accentColorD1 = offsetFloatColor(accentColor, -0.15f);
                    }

                    ImGui.text("Text Color");
                    ImGui.colorEdit4("Text Color", textColor, ImGuiColorEditFlags.NoLabel);

                    ImGui.text("Background Color");
                    ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, new ImVec2(0, 15));
                    ImGui.colorEdit4("Background Color", backgroundColor, ImGuiColorEditFlags.NoLabel);

                    ImGui.separator();
                    boolean shouldSave = ImGui.button("Save");

                    // Save config to disk and display popup
                    if (shouldSave) {
                        config.setAccentColor(accentColor);
                        config.setTextColor(textColor);
                        config.setBackgroundColor(backgroundColor);

                        ImGuiConfigService.saveConfigToDisk(config);

                        ImGui.openPopup("Saved");
                    }

                    // Save confirmation
                    if (ImGui.beginPopupModal("Saved", new ImBoolean(true), ImGuiWindowFlags.NoResize)) {
                        ImGui.text("Your menu config has been saved");
                        ImGui.endPopup();
                    }

                    ImGui.popStyleVar();
                    ImGui.endTabItem();
                }

                ImGui.endTabBar();
            }

        }

        // POP HERE
        ImGui.popStyleColor(pushedStyleCount);

        ImGui.end();
    }

    public static float[] offsetFloatColor(float[] color, float offset) {
        float[] newColor = new float[color.length];

        for (int i = 0; i < color.length; i++) {
            float current = color[i];

            current += offset;

            if (current < 0) current = 0;
            if (current > 1) current = 1;

            newColor[i] = current;
        }

        return newColor;
    }

    // Technically this should be faster than Arrays.equal, maybe?
    public static boolean colorFloatsEqual(float[] color1, float[] color2) {
        return color1[0] == color2[0] &&
                color1[1] == color2[1] &&
                color1[2] == color2[2] &&
                color1[3] == color2[3];
    }

    public static float[] imVec4ToFloat(ImVec4 vec4) {
        return new float[]{vec4.x, vec4.y, vec4.z, vec4.w};
    }

    public static ImVec4 floatToImVec4(float[] color) {
        return new ImVec4(color[0], color[1], color[2], color[3]);
    }
}
