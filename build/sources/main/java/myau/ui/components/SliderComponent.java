package myau.ui.components;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicInteger;
import myau.HUDModule;
import myau.MyauClient;
import myau.ui.ClickGui;
import myau.ui.Component;
import myau.ui.UiRender;
import myau.ui.callback.GuiInput;
import myau.ui.dataset.Slider;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class SliderComponent implements Component {
    private final Slider slider;
    private final ModuleComponent parentModule;
    private int offsetY;
    private int x;
    private int y;
    private boolean dragging = false;
    private double sliderWidth;
    private double displayedSliderWidth;
    private long increment = 0;
    private long decrement = 0;

    public SliderComponent(Slider slider, ModuleComponent parentModule, int offsetY) {
        this.slider = slider;
        this.parentModule = parentModule;
        this.x = parentModule.category.getX() + parentModule.category.getWidth();
        this.y = parentModule.category.getY() + parentModule.offsetY;
        this.offsetY = offsetY;
    }

    @Override
    public void draw(AtomicInteger offset) {
        int sliderStart = this.parentModule.category.getX() + 8;
        int sliderWidth = this.parentModule.category.getWidth() - 16;
        this.displayedSliderWidth += (this.sliderWidth - this.displayedSliderWidth) * 0.24D;
        int sliderEnd = sliderStart + Math.min(sliderWidth, Math.max(0, (int)this.displayedSliderWidth));
        int controlY = this.parentModule.category.getY() + this.offsetY + 1;
        int barY = this.parentModule.category.getY() + this.offsetY + 17;
        UiRender.drawGlassControl(sliderStart, controlY, sliderStart + sliderWidth, controlY + 22, 4.0F, 0xD8172433);
        UiRender.drawRoundedRect(sliderStart + 4, barY, sliderStart + sliderWidth - 4, barY + 5, 2.5F, 0xC53D5269);
        sliderEnd = Math.max(sliderStart + 4, Math.min(sliderStart + sliderWidth - 4, sliderEnd));
        HUDModule hud = (HUDModule) MyauClient.moduleManager.modules.get(HUDModule.class);
        int color = hud != null ? hud.getColor(System.currentTimeMillis(), offset.get()).getRGB() : UiRender.ACCENT;
        if (sliderEnd > sliderStart + 4) {
            UiRender.drawRoundedRect(sliderStart + 4, barY, sliderEnd, barY + 5, 2.5F, color);
        }
        UiRender.drawRoundedRect(sliderEnd - 3.5F, barY - 1.0F, sliderEnd + 3.5F, barY + 6.0F, 2.0F, UiRender.TEXT);
        GL11.glPushMatrix();
        GL11.glScaled(0.75D, 0.75D, 0.75D);
        Minecraft.getMinecraft().fontRendererObj.drawString(this.slider.getName().replace("-", " "), sliderStart / 0.75F + 4.0F, (this.parentModule.category.getY() + this.offsetY + 3) / 0.75F, UiRender.TEXT, false);
        String value = this.slider.getValueColorString();
        float valueX = (sliderStart + sliderWidth - 4) / 0.75F - Minecraft.getMinecraft().fontRendererObj.getStringWidth(value);
        Minecraft.getMinecraft().fontRendererObj.drawString(value, valueX, (this.parentModule.category.getY() + this.offsetY + 3) / 0.75F, color, false);
        GL11.glPopMatrix();
    }

    @Override
    public void setComponentStartAt(int newOffsetY) {
        this.offsetY = newOffsetY;
    }

    @Override
    public int getHeight() {
        return 25;
    }

    @Override
    public void update(int mousePosX, int mousePosY) {
        this.y = this.parentModule.category.getY() + this.offsetY;
        this.x = this.parentModule.category.getX();

        int usableWidth = this.parentModule.category.getWidth() - 16;
        double d = Math.min(usableWidth, Math.max(0, mousePosX - (this.x + 8)));
        this.sliderWidth = (double)usableWidth *
                (this.slider.getInput() - this.slider.getMin()) /
                (this.slider.getMax() - this.slider.getMin());

        if (this.dragging) {
            if (d == 0.0D) {
                this.slider.setValue(this.slider.getMin());
            } else {
                double rawValue = d / (double)usableWidth
                        * (this.slider.getMax() - this.slider.getMin())
                        + this.slider.getMin();

                double inc = this.slider.getIncrement();
                if (inc > 0) {
                    rawValue = Math.round(rawValue / inc) * inc;
                }
                double n = roundToPrecision(rawValue, 2);
                n = Math.max(this.slider.getMin(), Math.min(this.slider.getMax(), n));
                this.slider.setValue(n);
            }
        }
        if (this.increment != 0 && this.increment < System.currentTimeMillis()) {
            this.increment = System.currentTimeMillis() + 50;
            this.slider.stepping(true);
        }
        if (this.decrement != 0 && this.decrement < System.currentTimeMillis()) {
            this.decrement = System.currentTimeMillis() + 50;
            this.slider.stepping(false);
        }
    }

    private static double roundToPrecision(double v, int precision) {
        if (precision < 0) {
            return 0.0D;
        } else {
            BigDecimal bd = new BigDecimal(v);
            bd = bd.setScale(precision, RoundingMode.HALF_UP);
            return bd.doubleValue();
        }
    }

    @Override
    public void mouseDown(int x, int y, int button) {
        if (this.isTextHovered(x, y) && button == 0 && this.parentModule.panelExpand) {
            GuiInput.prompt(slider.getName().replace("-", " "), slider.getValueString(), slider::setValueString, ClickGui.getInstance());
            return;
        }

        if (this.isLeftHalfHovered(x, y) && this.parentModule.panelExpand) {
            if (button == 0) {
                this.dragging = true;
            } else if (button == 1 && this.decrement == 0) {
                this.decrement = System.currentTimeMillis() + 500;
                this.slider.stepping(false);
            }
        }

        if (this.isRightHalfHovered(x, y) && this.parentModule.panelExpand) {
            if (button == 0) {
                this.dragging = true;
            } else if (button == 1 && this.increment == 0) {
                this.increment = System.currentTimeMillis() + 500;
                this.slider.stepping(true);
            }
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        this.dragging = false;
        this.increment = 0;
        this.decrement = 0;
    }

    @Override
    public void keyTyped(char chatTyped, int keyCode) {
    }

    public boolean isTextHovered(int x, int y) {
        return x > this.x + 4 && x < this.x + this.parentModule.category.getWidth() - 4 && y > this.y && y < this.y + 14;
    }

    public boolean isLeftHalfHovered(int x, int y) {
        return x > this.x + 4 && x < this.x + this.parentModule.category.getWidth() / 2 + 1 && y > this.y + 14 && y < this.y + 25;
    }

    public boolean isRightHalfHovered(int x, int y) {
        return x > this.x + this.parentModule.category.getWidth() / 2 && x < this.x + this.parentModule.category.getWidth() - 4 && y > this.y + 14 && y < this.y + 25;
    }

    @Override
    public boolean isVisible() {
        return slider.isVisible();
    }
}
