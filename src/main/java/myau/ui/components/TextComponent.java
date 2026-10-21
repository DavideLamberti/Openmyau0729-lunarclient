package myau.ui.components;

import java.util.concurrent.atomic.AtomicInteger;
import myau.ChatColor;
import myau.StringSetting;
import myau.ui.ClickGui;
import myau.ui.Component;
import myau.ui.UiRender;
import myau.ui.callback.GuiInput;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class TextComponent implements Component {
    private final StringSetting setting;
    private final ModuleComponent module;
    private int offsetY;
    private int x;
    private int y;

    public TextComponent(StringSetting setting, ModuleComponent parentModule, int offsetY) {
        this.setting = setting;
        this.module = parentModule;
        this.x = parentModule.category.getX() + parentModule.category.getWidth();
        this.y = parentModule.category.getY() + parentModule.offsetY;
        this.offsetY = offsetY;
    }

    @Override
    public void draw(AtomicInteger offset) {
        int rowX = this.module.category.getX() + 8;
        int rowY = this.module.category.getY() + this.offsetY + 1;
        int rowWidth = this.module.category.getWidth() - 16;
        UiRender.drawGlassControl(rowX, rowY, rowX + rowWidth, rowY + 14, 4.0F, 0xD8172433);
        GL11.glPushMatrix();
        GL11.glScaled(0.75D, 0.75D, 0.75D);
        String name = this.setting.getName().replace("-", " ");
        String value = ChatColor.formatText(this.setting.getDisplayValue());
        Minecraft.getMinecraft().fontRendererObj.drawString(name, (rowX + 4) / 0.75F, (rowY + 3) / 0.75F, UiRender.TEXT, false);
        float valueX = (rowX + rowWidth - 4) / 0.75F - Minecraft.getMinecraft().fontRendererObj.getStringWidth(value);
        Minecraft.getMinecraft().fontRendererObj.drawString(value, valueX, (rowY + 3) / 0.75F, UiRender.ACCENT, false);
        GL11.glPopMatrix();
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
            GuiInput.prompt(setting.getName().replace("-", " "), setting.getValue(), setting::setValue, ClickGui.getInstance());
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
