package azula.blockcounter.rendering;

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
    public static ImBoolean filled = new ImBoolean(false);

    // Circle
    public final static int[] radius = {1};
    public final static int[] circleHeight = {1};
    public static ImBoolean circleFilled = new ImBoolean(false);

    // Slider Values
    private final static int MIN = 1;
    private final static int MAX = 100;

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
                    ImGui.combo(" ", selectedShape, shapeOptions); // empty string makes dropdown break :/

                    // Line Config
                    if(selectedShape.intValue() == Shape.LINE.ordinal()) {
                        ImGui.checkbox("Axis-aligned", axisAligned);

                        if (axisAligned.get()) {
                            ImGui.checkbox("Dual-axis", twoAxis);
                        }
                    }

                    // Quad Config
                    if (selectedShape.intValue() == Shape.QUAD.ordinal()) {
                        ImGui.sliderInt("Length", length, MIN, MAX, ImGuiSliderFlags.AlwaysClamp);
                        ImGui.sliderInt("Width", width, MIN, MAX, ImGuiSliderFlags.AlwaysClamp);
                        ImGui.sliderInt("Height", height, MIN, MAX, ImGuiSliderFlags.AlwaysClamp);
                        ImGui.checkbox("Filled", filled);
                    }

                    // Circle Config
                    if (selectedShape.intValue() == Shape.CIRCLE.ordinal()) {
                        ImGui.sliderInt("Radius", radius, MIN, MAX, ImGuiSliderFlags.AlwaysClamp);
                        ImGui.sliderInt("Height", circleHeight, MIN, MAX, ImGuiSliderFlags.AlwaysClamp);
                        ImGui.checkbox("Filled", circleFilled);
                    }

                    ImGui.endTabItem();
                }

                // Menu Settings
                if (ImGui.beginTabItem("Menu Config")) {

                    // TODO: ADD A RESET BUTTON TO SWITCH BACK TO LAST LOADED COLOR
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

                    if (shouldSave) {
                        config.setAccentColor(accentColor);
                        config.setTextColor(textColor);
                        config.setBackgroundColor(backgroundColor);

                        ImGuiConfigService.saveConfigToDisk(config);
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
