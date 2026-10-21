package myau.ui.components;

import java.awt.Color;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import myau.*;
import myau.ui.Component;
import myau.ui.UiRender;
import myau.ui.dataset.impl.FloatSlider;
import myau.ui.dataset.impl.IntSlider;
import myau.ui.dataset.impl.PercentageSlider;
import net.minecraft.client.Minecraft;

public class ModuleComponent implements Component {
    public Module mod;
    public CategoryComponent category;
    public int offsetY;
    private final ArrayList<Component> settings;
    public boolean panelExpand;
    private boolean hovered;
    private float hoverAnimation;
    private float toggleAnimation;

    public ModuleComponent(Module mod, CategoryComponent category, int offsetY) {
        this.mod = mod;
        this.category = category;
        this.offsetY = offsetY;
        this.settings = new ArrayList<>();
        this.panelExpand = false;
        int y = offsetY + 20;

        ArrayList<Setting<?>> moduleSettings = MyauClient.settingManager.settingsByModule.get(mod.getClass());
        if (moduleSettings != null && !moduleSettings.isEmpty()) {
            for (Setting<?> baseSetting : moduleSettings) {
                if (baseSetting instanceof BooleanSetting) {
                    BooleanSetting setting = (BooleanSetting) baseSetting;
                    CheckBoxComponent c = new CheckBoxComponent(setting, this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseSetting instanceof FloatSetting) {
                    FloatSetting setting = (FloatSetting) baseSetting;
                    SliderComponent c = new SliderComponent(new FloatSlider(setting), this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseSetting instanceof IntegerSetting) {
                    IntegerSetting setting = (IntegerSetting) baseSetting;
                    SliderComponent c = new SliderComponent(new IntSlider(setting), this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseSetting instanceof PercentSetting) {
                    PercentSetting setting = (PercentSetting) baseSetting;
                    SliderComponent c = new SliderComponent(new PercentageSlider(setting), this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseSetting instanceof ModeSetting) {
                    ModeSetting setting = (ModeSetting) baseSetting;
                    ModeComponent c = new ModeComponent(setting, this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseSetting instanceof ColorSetting) {
                    ColorSetting setting = (ColorSetting) baseSetting;
                    ColorSliderComponent c = new ColorSliderComponent(setting, this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                } else if (baseSetting instanceof StringSetting) {
                    StringSetting setting = (StringSetting) baseSetting;
                    TextComponent c = new TextComponent(setting, this, y);
                    this.settings.add(c);
                    y += c.getHeight();
                }
            }
        }

        this.settings.add(new BindComponent(this, y));
    }

    @Override
    public void setComponentStartAt(int newOffsetY) {
        this.offsetY = newOffsetY;
        int y = this.offsetY + 20;

        for (Component c : this.settings) {
            c.setComponentStartAt(y);
            if (c.isVisible()) {
                y += c.getHeight();
            }
        }
    }

    @Override
    public void draw(AtomicInteger offset) {
        this.hoverAnimation = animate(this.hoverAnimation, this.hovered ? 1.0F : 0.0F, 0.22F);
        this.toggleAnimation = animate(this.toggleAnimation, this.mod.isEnabled() ? 1.0F : 0.0F, 0.18F);
        int textColor;
        if (this.mod.isEnabled()) {
            textColor = UiRender.TEXT;
        } else {
            textColor = UiRender.TEXT_MUTED;
        }
        int rowX = this.category.getX() + 5;
        int rowY = this.category.getY() + this.offsetY + 2;
        int rowWidth = this.category.getWidth() - 10;
        int idleColor = UiRender.blend(UiRender.CONTROL, UiRender.CONTROL_HOVER, this.hoverAnimation);
        int rowColor = UiRender.blend(idleColor, 0xE0253E5B, this.toggleAnimation);
        UiRender.drawGlassControl(rowX, rowY, rowX + rowWidth, rowY + 16, 5.0F, rowColor);
        if (this.mod.isEnabled()) {
            UiRender.drawRoundedRect(rowX, rowY + 3, rowX + 2, rowY + 13, 1.0F, UiRender.blend(0xFF416A97, UiRender.ACCENT, this.toggleAnimation));
        }
        if (this.panelExpand && !this.settings.isEmpty()) {
            UiRender.drawRoundedOutline(rowX, rowY, rowX + rowWidth, rowY + 16, 5.0F, 0.7F, 0x555B89BA);
        }
        Minecraft.getMinecraft().fontRendererObj.drawString(
            this.mod.getName(),
            rowX + 7,
            rowY + 4,
            textColor
        );
        UiRender.drawToggle(rowX + rowWidth - 23.0F, rowY + 3.5F, this.toggleAnimation);
        if (this.panelExpand && !this.settings.isEmpty()) {
            for (Component c : this.settings) {
                if (c.isVisible()) {
                    c.draw(offset);
                    offset.incrementAndGet();
                }
            }
        }
    }

    @Override
    public int getHeight() {
        if (!this.panelExpand) {
            return 20;
        } else {
            int h = 20;
            for (Component c : this.settings) {
                if (c.isVisible()) {
                    h += c.getHeight();
                }
            }
            return h;
        }
    }

    @Override
    public void update(int mousePosX, int mousePosY) {
        this.hovered = this.isHovered(mousePosX, mousePosY);
        if (!panelExpand) return;
        if (!this.settings.isEmpty()) {
            for (Component c : this.settings) {
                if (c.isVisible()) {
                    c.update(mousePosX, mousePosY);
                }
            }
        }
    }

    @Override
    public void mouseDown(int x, int y, int button) {
        if (this.isHovered(x, y) && button == 0) {
            this.mod.toggle();
        }

        if (this.isHovered(x, y) && button == 1) {
            this.panelExpand = !this.panelExpand;
        }

        if (!panelExpand) return;
        for (Component c : this.settings) {
            if (c.isVisible()) {
                c.mouseDown(x, y, button);
            }
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        if (!panelExpand) return;
        for (Component c : this.settings) {
            if (c.isVisible()) {
                c.mouseReleased(x, y, button);
            }
        }
    }

    @Override
    public void keyTyped(char chatTyped, int keyCode) {
        if (!panelExpand) return;
        for (Component c : this.settings) {
            if (c.isVisible()) {
                c.keyTyped(chatTyped, keyCode);
            }
        }
    }

    public boolean isHovered(int x, int y) {
        return x > this.category.getX() + 4 && x < this.category.getX() + this.category.getWidth() - 4 && y > this.category.getY() + this.offsetY + 1 && y < this.category.getY() + 19 + this.offsetY;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    private static float animate(float current, float target, float speed) {
        return current + (target - current) * speed;
    }
}
