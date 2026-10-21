package myau.ui.dataset.impl;

import myau.ChatColor;
import myau.FloatSetting;
import myau.ui.dataset.Slider;

public class FloatSlider extends Slider {
    private final FloatSetting setting;

    public FloatSlider(FloatSetting setting) {
        this.setting = setting;
    }

    @Override
    public double getInput() {
        return setting.getValue();
    }

    @Override
    public double getMin() {
        return setting.getMin();
    }

    @Override
    public double getMax() {
        return setting.getMax();
    }

    @Override
    public void setValue(double value) {
        setting.setValue((float) value);
    }

    @Override
    public void setValueString(String value) {
        try {
            setting.setValue(Float.parseFloat(value));
        } catch (Exception ignore) {
        }
    }

    @Override
    public String getName() {
        return setting.getName().replace("-", " ");
    }

    @Override
    public String getValueString() {
        return setting.getValue().toString();
    }

    @Override
    public String getValueColorString() {
        return ChatColor.formatText(setting.getDisplayValue());
    }

    @Override
    public double getIncrement() {
        return 0.1;
    }

    @Override
    public boolean isVisible() {
        return setting.isVisible();
    }

    @Override
    public void stepping(boolean increment) {
        if (increment) {
            if (setting.getValue() >= setting.getMax()) return;
            setting.setValue(Math.round(setting.getValue() * 10.0F + 1.0F) / 10.0F);
        } else {
            if (setting.getValue() <= setting.getMin()) return;
            setting.setValue(Math.round(setting.getValue() * 10.0F - 1.0F) / 10.0F);
        }
    }
}
