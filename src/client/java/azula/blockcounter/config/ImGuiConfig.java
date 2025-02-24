package azula.blockcounter.config;

public class ImGuiConfig {

    private float[] accentColor;
    private float[] textColor;
    private float[] backgroundColor;

    public ImGuiConfig() {
        this.accentColor = new float[4];
        this.textColor = new float[4];
        this.backgroundColor = new float[4];
    }

    public ImGuiConfig(float[] accent, float[] text, float[] backgroundColor) {
        this.accentColor = accent;
        this.textColor = text;
        this.backgroundColor = backgroundColor;
    }

    public void setAccentColor(float[] newColor) {
        this.accentColor = newColor;
    }

    public void setTextColor(float[] newColor) {
        this.textColor = newColor;
    }

    public void setBackgroundColor(float[] newColor) {
        this.backgroundColor = newColor;
    }

    public float[] getAccentColor() {
        return this.accentColor;
    }

    public float[] getTextColor() {
        return this.textColor;
    }

    public float[] getBackgroundColor() {
        return this.backgroundColor;
    }

}
