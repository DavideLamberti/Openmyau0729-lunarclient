package myau.ui.dataset.impl;

import myau.ChatColor;
import myau.PercentSetting;
import myau.ui.dataset.Slider;

public class PercentageSlider extends Slider {
    private final PercentSetting setting;

    public PercentageSlider(PercentSetting setting) {
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
        setting.setValue((int) Math.round(value));
    }

    @Override
    public void setValueString(String value) {
        try {
            setting.setValue(Integer.parseInt(value));
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
        return 1.0;
    }

    @Override
    public boolean isVisible() {
        return setting.isVisible();
    }

    @Override
    public void stepping(boolean increment) {
        if (increment) {
            if (setting.getValue() >= setting.getMax()) return;
            setting.setValue(setting.getValue() + 1);
        } else {
            if (setting.getValue() <= setting.getMin()) return;
            setting.setValue(setting.getValue() - 1);
        }
    }
}
