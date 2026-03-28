package azula.blockcounter.config.shape.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class IntSliderWidget extends AbstractSliderButton {

    private int minValue;
    private int maxValue;
    private int scaledValue;
    private String label;

    private final List<Consumer<Integer>> valueListeners = new ArrayList<>();

    public IntSliderWidget(String label, int x, int y, int w, int h, int min, int max, int initial) {
        super(x, y, w, h, Component.literal(label), (double) (initial - min) / (max - min));

        this.minValue = min;
        this.maxValue = max;
        this.label = label;

        this.applyValue();
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.literal(label + ": " + this.scaledValue));
    }

    @Override
    protected void applyValue() {
        this.scaledValue = (int) (this.minValue + this.value * (this.maxValue - this.minValue));
        this.valueListeners.forEach(listener -> listener.accept(this.scaledValue));
    }

    public IntSliderWidget addValueListener(Consumer<Integer> listener) {
        this.valueListeners.add(listener);
        return this;
    }
}
