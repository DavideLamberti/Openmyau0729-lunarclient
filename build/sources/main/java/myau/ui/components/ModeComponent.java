package myau.ui.components;

import java.util.concurrent.atomic.AtomicInteger;
import myau.ChatColor;
import myau.ModeSetting;
import myau.ui.Component;
import myau.ui.UiRender;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class ModeComponent implements Component {
    private final ModeSetting setting;
    private final ModuleComponent parentModule;
    private int x;
    private int y;
    private int offsetY;

    public ModeComponent(ModeSetting setting, ModuleComponent parentModule, int offsetY) {
        this.setting = setting;
        this.parentModule = parentModule;
        this.x = parentModule.category.getX() + parentModule.category.getWidth();
        this.y = parentModule.category.getY() + parentModule.offsetY;
        this.offsetY = offsetY;
    }

    @Override
    public void draw(AtomicInteger offset) {
        int rowX = this.parentModule.category.getX() + 8;
        int rowY = this.parentModule.category.getY() + this.offsetY + 1;
        int rowWidth = this.parentModule.category.getWidth() - 16;
        UiRender.drawGlassControl(rowX, rowY, rowX + rowWidth, rowY + 14, 4.0F, 0xD8172433);
        GL11.glPushMatrix();
        GL11.glScaled(0.75D, 0.75D, 0.75D);
        String mode = this.setting.getModeName();
        mode = mode.replace("_", " ");
        String formattedMode = mode.isEmpty() ? "?" : mode.substring(0, 1).toUpperCase() + (mode.length() > 1 ? mode.substring(1).toLowerCase() : "");
        Minecraft.getMinecraft().fontRendererObj.drawString(this.setting.getName().replace("-", " "), (rowX + 4) / 0.75F, (rowY + 3) / 0.75F, UiRender.TEXT, false);
        float valueX = (rowX + rowWidth - 4) / 0.75F - Minecraft.getMinecraft().fontRendererObj.getStringWidth(formattedMode);
        Minecraft.getMinecraft().fontRendererObj.drawString(ChatColor.formatText("&9" + formattedMode), valueX, (rowY + 3) / 0.75F, UiRender.ACCENT, false);
        GL11.glPopMatrix();
    }

    @Override
    public void update(int mousePosX, int mousePosY) {
        this.y = this.parentModule.category.getY() + this.offsetY;
        this.x = this.parentModule.category.getX();
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
    public void mouseDown(int x, int y, int button) {
        if (isHovered(x, y)) {
            if (button == 0) {
                this.setting.nextMode();
            } else if (button == 1) {
                this.setting.previousMode();
            }
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
    }

    @Override
    public void keyTyped(char chatTyped, int keyCode) {
    }

    private boolean isHovered(int x, int y) {
        return x > this.x + 4 && x < this.x + this.parentModule.category.getWidth() - 4 && y > this.y && y < this.y + 17;
    }

    @Override
    public boolean isVisible() {
        return setting.isVisible();
    }
}
