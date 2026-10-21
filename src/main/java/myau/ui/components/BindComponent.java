package myau.ui.components;

import java.util.concurrent.atomic.AtomicInteger;
import myau.HUDModule;
import myau.InputUtils;
import myau.GuiModule;
import myau.MyauClient;
import myau.ui.Component;
import myau.ui.UiRender;
import myau.ui.dataset.BindStage;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class BindComponent implements Component {
    private boolean isBinding;
    private final ModuleComponent parentModule;
    private int offsetY;
    private int x;
    private int y;

    public BindComponent(ModuleComponent b, int offsetY) {
        this.parentModule = b;
        this.x = b.category.getX() + b.category.getWidth();
        this.y = b.category.getY() + b.offsetY;
        this.offsetY = offsetY;
    }

    @Override
    public void draw(AtomicInteger offset) {
        int rowX = this.parentModule.category.getX() + 8;
        int rowY = this.parentModule.category.getY() + this.offsetY + 1;
        int rowWidth = this.parentModule.category.getWidth() - 16;
        UiRender.drawGlassControl(rowX, rowY, rowX + rowWidth, rowY + 14, 4.0F, this.isBinding ? 0xE0335A7C : 0xD8172433);
        if (this.isBinding) {
            UiRender.drawRoundedOutline(rowX, rowY, rowX + rowWidth, rowY + 14, 4.0F, 0.7F, UiRender.ACCENT_DIM);
        }
        GL11.glPushMatrix();
        GL11.glScaled(0.75D, 0.75D, 0.75D);
        String displayText = this.isBinding ? BindStage.binding : BindStage.bind + ": " + InputUtils.getKeyName(this.parentModule.mod.getKeyCode());
        int color = this.isBinding ? UiRender.ACCENT : UiRender.TEXT_MUTED;
        this.renderText(displayText, color);
        GL11.glPopMatrix();
    }

    @Override
    public void update(int mousePosX, int mousePosY) {
        this.y = this.parentModule.category.getY() + this.offsetY;
        this.x = this.parentModule.category.getX();
    }

    @Override
    public void mouseDown(int x, int y, int button) {
        if (this.isHovered(x, y) && button == 0 && this.parentModule.panelExpand) {
            this.isBinding = !this.isBinding;
        } else if (this.isBinding && this.parentModule.panelExpand) {
            int keyIndex = button - 100;
            if (button == 0) {
                this.isBinding = false;
                return;
            }
            this.parentModule.mod.setKeyCode(keyIndex);
            this.isBinding = false;
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
    }

    @Override
    public void keyTyped(char chatTyped, int keyCode) {
        if (this.isBinding) {
            if (keyCode == 1) { // ESCAPE cancels binding
                this.isBinding = false;
                return;
            }
            if (keyCode == 11 || keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK) {
                if (this.parentModule.mod instanceof GuiModule) {
                    this.parentModule.mod.setKeyCode(Keyboard.KEY_RSHIFT);
                } else {
                    this.parentModule.mod.setKeyCode(0);
                }
            } else {
                this.parentModule.mod.setKeyCode(keyCode);
            }
            this.isBinding = false;
        }
    }

    @Override
    public void setComponentStartAt(int newOffsetY) {
        this.offsetY = newOffsetY;
    }

    public boolean isHovered(int x, int y) {
        return x > this.x + 4 && x < this.x + this.parentModule.category.getWidth() - 4 && y > this.y - 1 && y < this.y + 17;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    private void renderText(String s, int color) {
        Minecraft.getMinecraft().fontRendererObj.drawString(s, (this.parentModule.category.getX() + 12) / 0.75F, (this.parentModule.category.getY() + this.offsetY + 4) / 0.75F, color, false);
    }
}
