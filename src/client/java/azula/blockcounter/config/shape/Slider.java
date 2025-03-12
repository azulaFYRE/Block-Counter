package azula.blockcounter.config.shape;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class Slider extends SliderWidget {

    private double min;
    private double max;

    private boolean negativeMin;

    public interface SliderCallback {
        void onValueChange(SliderWidget widget, int value);
    }

    private final SliderCallback callback;

    public Slider(int x, int y, int width, int height, Text text, double value, double min, double max, SliderCallback callback) {
        super(x, y, width, height, text, value);
        this.callback = callback;
        this.min = min;
        this.max = max;
        this.value = value;
        this.negativeMin = this.min < 0;
    }

    @Override
    protected void updateMessage() {
    }

    @Override
    protected void applyValue() {
        int scaledIntValue;
        if (this.negativeMin) {
            scaledIntValue = (int) (this.value * (this.max - this.min) - (Math.abs(this.min)));
        } else {
            scaledIntValue = (int) (this.value * (this.max - this.min) + this.min);
        }
        this.callback.onValueChange(this, scaledIntValue);
    }

    protected void setValue(double value) {
        if (this.negativeMin) {
            this.value = (value + Math.abs(this.min)) / (this.max - this.min);
        } else {
            this.value = (value - this.min) / (this.max - this.min);
        }

    }
}
