package myau.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import myau.Module;
import myau.ui.Component;
import myau.ui.UiRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

public class CategoryComponent {
    private static final int MAX_HEIGHT = 300;
    private static final int HEADER_HEIGHT = 22;
    private static final int PANEL_WIDTH = 122;

    public ArrayList<Component> modulesInCategory = new ArrayList<>();
    public String categoryName;
    private boolean categoryOpened;
    private int width;
    private int y;
    private int x;
    private final int bh;
    public boolean dragging;
    public int xx;
    public int yy;
    public boolean pin = false;
    private double marginY, marginX;
    private int scroll = 0;
    private double animScroll = 0;
    private int height = 0;
    private float openAnimation = 0.0F;
    private float headerHoverAnimation = 0.0F;
    private boolean headerHovered;

    public CategoryComponent(String category, List<Module> modules) {
        this.categoryName = category;
        this.width = PANEL_WIDTH;
        this.x = 5;
        this.y = 5;
        this.bh = HEADER_HEIGHT;
        this.xx = 0;
        this.categoryOpened = true;
        this.dragging = false;
        int tY = this.bh + 4;
        this.marginX = this.width - 22;
        this.marginY = 6.0;
        for (Module mod : modules) {
            ModuleComponent b = new ModuleComponent(mod, this, tY);
            this.modulesInCategory.add(b);
            tY += 16;
        }
    }

    public ArrayList<Component> getModules() {
        return this.modulesInCategory;
    }

    public void setX(int n) {
        this.x = n;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void mousePressed(boolean d) {
        this.dragging = d;
    }

    public boolean isPin() {
        return this.pin;
    }

    public void setPin(boolean on) {
        this.pin = on;
    }

    public boolean isOpened() {
        return this.categoryOpened;
    }

    public void setOpened(boolean on) {
        this.categoryOpened = on;
    }

    public void render(FontRenderer renderer) {
        this.width = PANEL_WIDTH;
        update();
        height = 0;
        for (Component moduleRenderManager : this.modulesInCategory) {
            height += moduleRenderManager.getHeight();
        }
        int maxScroll = Math.max(0, height - MAX_HEIGHT);
        if (scroll > maxScroll) scroll = maxScroll;
        if (animScroll > maxScroll) animScroll = maxScroll;
        animScroll += (scroll - animScroll) * 0.2;
        this.openAnimation += ((this.categoryOpened ? 1.0F : 0.0F) - this.openAnimation) * 0.18F;
        this.headerHoverAnimation += (((this.headerHovered || this.dragging) ? 1.0F : 0.0F) - this.headerHoverAnimation) * 0.20F;
        int displayHeight = (int)((float)Math.min(height, MAX_HEIGHT) * easeOut(this.openAnimation));
        boolean visuallyOpened = displayHeight > 0;
        if (!this.modulesInCategory.isEmpty() && visuallyOpened) {
            UiRender.drawGlassPanel(this.x, this.y, this.x + this.width, this.y + this.bh + displayHeight + 6, 8.0F, UiRender.CARD);
        } else {
            UiRender.drawShadow(this.x, this.y, this.x + this.width, this.y + this.bh, 8.0F);
        }
        UiRender.drawGlassControl(this.x, this.y, this.x + this.width, this.y + this.bh, 8.0F, UiRender.blend(UiRender.CARD_ELEVATED, 0xE2385069, this.headerHoverAnimation));
        UiRender.drawRoundedRect(this.x + 1.0F, this.y + this.bh - 3.0F, this.x + this.width - 1.0F, this.y + this.bh, 1.5F, 0xB5101925);
        UiRender.drawCategoryIcon(this.x + 8.0F, this.y + 6.0F, this.categoryName);
        renderer.drawString(this.categoryName.toUpperCase(), this.x + 21.0F, this.y + 8.0F, UiRender.TEXT, false);
        renderer.drawString(Integer.toString(this.modulesInCategory.size()), this.x + this.width - 37.0F, this.y + 8.0F, UiRender.TEXT_MUTED, false);
        renderer.drawString(this.categoryOpened ? "-" : "+", this.x + this.width - 20.0F, (float)((double)this.y + marginY), UiRender.TEXT, false);
        UiRender.drawCircle(this.x + this.width - 8.0F, this.y + 11.0F, 2.0F, this.pin ? UiRender.ACCENT : 0xFF515A67);
        if (visuallyOpened && !this.modulesInCategory.isEmpty()) {
            int renderHeight = 0;
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            double scale = sr.getScaleFactor();
            int bottom = this.y + this.bh + displayHeight + 5;
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor((int)((this.x + 3) * scale), (int)((sr.getScaledHeight() - bottom) * scale), (int)((this.width - 6) * scale), (int)(displayHeight * scale));
            for (Component c2 : this.modulesInCategory) {
                int compHeight = c2.getHeight();
                if (renderHeight + compHeight > animScroll &&
                        renderHeight < animScroll + displayHeight) {
                    int drawY = (int) (renderHeight - animScroll);
                    c2.setComponentStartAt(this.bh + 4 + drawY);
                    c2.draw(new AtomicInteger(0));
                }
                renderHeight += compHeight;
            }
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            if (height > MAX_HEIGHT && displayHeight >= MAX_HEIGHT - 1) {
                float scrollY = (float) this.y + this.bh + 3 + (float) (animScroll * MAX_HEIGHT / height);
                UiRender.drawRoundedRect(this.x + this.width - 5.0F, scrollY, this.x + this.width - 3.0F, scrollY + ((float)MAX_HEIGHT * MAX_HEIGHT / height), 1.0F, UiRender.ACCENT_DIM);
            }
        }
    }

    public void update() {
        int offset = this.bh + 4;
        for (Component component : this.modulesInCategory) {
            component.setComponentStartAt(offset);
            offset += component.getHeight();
        }
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public void handleDrag(int x, int y) {
        this.headerHovered = isHeaderHovered(x, y);
        if (this.dragging) {
            this.setX(x - this.xx);
            this.setY(y - this.yy);
        }
    }

    public boolean isHovered(int x, int y) {
        return isPinHovered(x, y);
    }

    public boolean mousePressed(int x, int y) {
        return isCollapseHovered(x, y);
    }

    public boolean insideArea(int x, int y) {
        return isHeaderHovered(x, y);
    }

    public boolean isHeaderHovered(int mouseX, int mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.bh;
    }

    public boolean isCollapseHovered(int mouseX, int mouseY) {
        return mouseX >= this.x + this.width - 27 && mouseX <= this.x + this.width - 13 && mouseY >= this.y + 3 && mouseY <= this.y + this.bh - 3;
    }

    public boolean isPinHovered(int mouseX, int mouseY) {
        return mouseX >= this.x + this.width - 14 && mouseX <= this.x + this.width - 2 && mouseY >= this.y + 3 && mouseY <= this.y + this.bh - 3;
    }

    public void beginDrag(int mouseX, int mouseY) {
        this.dragging = true;
        this.xx = mouseX - this.x;
        this.yy = mouseY - this.y;
    }

    public String getName() {
        return categoryName;
    }

    public void setLocation(int parseInt, int parseInt1) {
        this.x = parseInt;
        this.y = parseInt1;
    }

    public void onScroll(int mouseX, int mouseY, int scrollAmount) {
        if (!categoryOpened || height <= MAX_HEIGHT) return;

        int areaTop = this.y + this.bh;
        int areaBottom = this.y + this.bh + MAX_HEIGHT;

        if (mouseX >= this.x && mouseX <= this.x + width && mouseY >= areaTop && mouseY <= areaBottom) {
            scroll -= scrollAmount * 12;
            scroll = Math.max(0, Math.min(scroll, height - MAX_HEIGHT));
        }
    }

    private static float easeOut(float value) {
        float clamped = Math.max(0.0F, Math.min(1.0F, value));
        float inverse = 1.0F - clamped;
        return 1.0F - inverse * inverse * inverse;
    }
}
