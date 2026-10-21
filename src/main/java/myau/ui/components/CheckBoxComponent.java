package myau.ui.components;

import java.util.concurrent.atomic.AtomicInteger;
import myau.BooleanSetting;
import myau.ChatColor;
import myau.ui.Component;
import myau.ui.UiRender;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class CheckBoxComponent implements Component {
    private final BooleanSetting setting;
    private final ModuleComponent module;
    private int offsetY;
    private int x;
    private int y;
    private float toggleAnimation;

    public CheckBoxComponent(BooleanSetting setting, ModuleComponent parentModule, int offsetY) {
        this.setting = setting;
        this.module = parentModule;
        this.x = parentModule.category.getX() + parentModule.category.getWidth();
        this.y = parentModule.category.getY() + parentModule.offsetY;
        this.offsetY = offsetY;
    }

    @Override
    public void draw(AtomicInteger offset) {
        this.toggleAnimation += ((this.setting.getValue() ? 1.0F : 0.0F) - this.toggleAnimation) * 0.18F;
        int rowX = this.module.category.getX() + 8;
        int rowY = this.module.category.getY() + this.offsetY + 1;
        int rowWidth = this.module.category.getWidth() - 16;
        UiRender.drawGlassControl(rowX, rowY, rowX + rowWidth, rowY + 14, 4.0F, 0xD8172433);
        GL11.glPushMatrix();
        GL11.glScaled(0.75D, 0.75D, 0.75D);
        Minecraft.getMinecraft().fontRendererObj.drawString(this.setting.getName().replace("-", " "), (rowX + 4) / 0.75F, (rowY + 3) / 0.75F, UiRender.TEXT, false);
        GL11.glPopMatrix();
        UiRender.drawToggle(rowX + rowWidth - 22.0F, rowY + 2.5F, this.toggleAnimation);
    }

    @Override
    public void setComponentStartAt(int newOffsetY) {
        this.offsetY = newOffsetY;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public void update(int mousePosX, int mousePosY) {
        this.y = this.module.category.getY() + this.offsetY;
        this.x = this.module.category.getX();
    }

    @Override
    public void mouseDown(int x, int y, int button) {
        if (this.isHovered(x, y) && button == 0 && this.module.panelExpand) {
            this.setting.setValue(!this.setting.getValue());
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
    }

    @Override
    public void keyTyped(char chatTyped, int keyCode) {
    }

    public boolean isHovered(int x, int y) {
        return x > this.x + 4 && x < this.x + this.module.category.getWidth() - 4 && y > this.y && y < this.y + 17;
    }

    @Override
    public boolean isVisible() {
        return setting.isVisible();
    }
}
