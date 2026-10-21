package myau.ui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

/**
 * Small, self-contained renderer for the Click GUI.  Keeping the primitives
 * here means every component shares the same radius, shadows and accent
 * treatment instead of looking like a collection of unrelated controls.
 */
public final class UiRender {
    public static final int ACCENT = 0xFF82C4FF;
    public static final int ACCENT_DIM = 0xAA73B8FF;
    public static final int CARD = 0xCD0A1421;
    public static final int CARD_ELEVATED = 0xE01A293A;
    public static final int CONTROL = 0xD01D2A39;
    public static final int CONTROL_HOVER = 0xE1344860;
    public static final int TEXT = 0xFFFFFFFF;
    public static final int TEXT_MUTED = 0xFFC3CBD6;

    private UiRender() {
    }

    public static void drawShadow(float left, float top, float right, float bottom, float radius) {
        drawRoundedRect(left - 3.0F, top + 3.0F, right + 3.0F, bottom + 5.0F, radius + 3.0F, 0x22000000);
        drawRoundedRect(left - 1.0F, top + 1.0F, right + 1.0F, bottom + 2.0F, radius + 1.0F, 0x55000000);
    }

    public static void drawGlassPanel(float left, float top, float right, float bottom, float radius, int color) {
        drawShadow(left, top, right, bottom, radius);
        drawRoundedRect(left, top, right, bottom, radius, color);
        drawRoundedOutline(left, top, right, bottom, radius, 0.8F, 0x7A9CCBFF);
        drawRoundedRect(left + 1.0F, top + 1.0F, right - 1.0F, top + 2.5F, Math.max(1.0F, radius - 1.0F), 0x32FFFFFF);
    }

    public static void drawGlassControl(float left, float top, float right, float bottom, float radius, int color) {
        drawRoundedRect(left, top, right, bottom, radius, color);
        drawRoundedOutline(left, top, right, bottom, radius, 0.65F, 0x4A9AC4F0);
        drawRoundedRect(left + 1.0F, top + 1.0F, right - 1.0F, top + 2.0F, Math.max(1.0F, radius - 1.0F), 0x24FFFFFF);
    }

    public static void drawRoundedRect(float left, float top, float right, float bottom, float radius, int color) {
        if (right <= left || bottom <= top || (color >>> 24) == 0) {
            return;
        }

        float clampedRadius = Math.min(radius, Math.min((right - left) / 2.0F, (bottom - top) / 2.0F));
        // Gui.drawRect is reliable on the fixed-function pipeline used by 1.8.9.
        // It provides an opaque core even on drivers that do not fill GL_POLYGON
        // consistently while a Minecraft GUI is being rendered.
        int coreLeft = (int)Math.ceil(left + clampedRadius);
        int coreRight = (int)Math.floor(right - clampedRadius);
        int coreTop = (int)Math.ceil(top + clampedRadius);
        int coreBottom = (int)Math.floor(bottom - clampedRadius);
        Gui.drawRect(coreLeft, (int)Math.floor(top), coreRight, (int)Math.ceil(bottom), color);
        Gui.drawRect((int)Math.floor(left), coreTop, (int)Math.ceil(right), coreBottom, color);
        beginShape();
        color(color);
        GL11.glBegin(GL11.GL_POLYGON);
        corner(right - clampedRadius, top + clampedRadius, clampedRadius, -90, 0);
        corner(right - clampedRadius, bottom - clampedRadius, clampedRadius, 0, 90);
        corner(left + clampedRadius, bottom - clampedRadius, clampedRadius, 90, 180);
        corner(left + clampedRadius, top + clampedRadius, clampedRadius, 180, 270);
        GL11.glEnd();
        endShape();
    }

    public static void drawRoundedOutline(float left, float top, float right, float bottom, float radius, float lineWidth, int color) {
        if (right <= left || bottom <= top || (color >>> 24) == 0) {
            return;
        }

        float clampedRadius = Math.min(radius, Math.min((right - left) / 2.0F, (bottom - top) / 2.0F));
        beginShape();
        color(color);
        GL11.glLineWidth(lineWidth);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        corner(right - clampedRadius, top + clampedRadius, clampedRadius, -90, 0);
        corner(right - clampedRadius, bottom - clampedRadius, clampedRadius, 0, 90);
        corner(left + clampedRadius, bottom - clampedRadius, clampedRadius, 90, 180);
        corner(left + clampedRadius, top + clampedRadius, clampedRadius, 180, 270);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1.0F);
        endShape();
    }

    public static void drawVerticalGradient(float left, float top, float right, float bottom, int topColor, int bottomColor) {
        if (right <= left || bottom <= top) {
            return;
        }

        beginShape();
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_QUADS);
        color(topColor);
        GL11.glVertex2f(left, top);
        GL11.glVertex2f(right, top);
        color(bottomColor);
        GL11.glVertex2f(right, bottom);
        GL11.glVertex2f(left, bottom);
        GL11.glEnd();
        GL11.glShadeModel(GL11.GL_FLAT);
        endShape();
    }

    public static void drawCircle(float centerX, float centerY, float radius, int color) {
        if (radius <= 0.0F || (color >>> 24) == 0) {
            return;
        }

        beginShape();
        color(color);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(centerX, centerY);
        for (int i = 0; i <= 24; ++i) {
            double angle = Math.PI * 2.0D * (double)i / 24.0D;
            GL11.glVertex2f(centerX + (float)Math.cos(angle) * radius, centerY + (float)Math.sin(angle) * radius);
        }
        GL11.glEnd();
        endShape();
    }

    public static void drawToggle(float left, float top, boolean enabled) {
        drawToggle(left, top, enabled ? 1.0F : 0.0F);
    }

    public static void drawToggle(float left, float top, float progress) {
        float width = 18.0F;
        float height = 9.0F;
        float clampedProgress = Math.max(0.0F, Math.min(1.0F, progress));
        int track = blend(0xB8465A70, 0xE060A9E8, clampedProgress);
        drawGlassControl(left, top, left + width, top + height, height / 2.0F, track);
        drawRoundedOutline(left, top, left + width, top + height, height / 2.0F, 0.7F, blend(0x77546C85, 0xCC9DD8FF, clampedProgress));
        float knobX = left + 4.5F + (width - 9.0F) * clampedProgress;
        drawRoundedRect(knobX - 3.5F, top + 0.75F, knobX + 3.5F, top + 8.25F, 2.0F, 0xFFF4FBFF);
        drawRoundedRect(knobX - 2.25F, top + 1.5F, knobX + 2.25F, top + 2.25F, 1.0F, 0x55FFFFFF);
    }

    public static int blend(int from, int to, float progress) {
        float p = Math.max(0.0F, Math.min(1.0F, progress));
        int a = (int)((float)(from >>> 24) + ((float)(to >>> 24) - (float)(from >>> 24)) * p);
        int r = (int)((float)(from >> 16 & 255) + ((float)(to >> 16 & 255) - (float)(from >> 16 & 255)) * p);
        int g = (int)((float)(from >> 8 & 255) + ((float)(to >> 8 & 255) - (float)(from >> 8 & 255)) * p);
        int b = (int)((float)(from & 255) + ((float)(to & 255) - (float)(from & 255)) * p);
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static void drawCategoryIcon(float left, float top, String category) {
        drawRoundedRect(left, top, left + 9.0F, top + 9.0F, 3.0F, 0x3368A9FF);
        drawRoundedRect(left + 2.0F, top + 2.0F, left + 7.0F, top + 7.0F, 1.4F, ACCENT);
        if ("Combat".equals(category)) {
            drawRoundedRect(left + 4.0F, top + 1.0F, left + 5.0F, top + 8.0F, 0.5F, 0xFFE7F1FF);
        } else if ("Movement".equals(category)) {
            drawRoundedRect(left + 1.0F, top + 4.0F, left + 8.0F, top + 5.0F, 0.5F, 0xFFE7F1FF);
        } else if ("Render".equals(category)) {
            drawCircle(left + 4.5F, top + 4.5F, 1.5F, 0xFFE7F1FF);
        }
    }

    private static void corner(float centerX, float centerY, float radius, int fromDegrees, int toDegrees) {
        if (radius <= 0.0F) {
            GL11.glVertex2f(centerX, centerY);
            return;
        }
        for (int degree = fromDegrees; degree <= toDegrees; degree += 15) {
            double radians = Math.toRadians(degree);
            GL11.glVertex2f(centerX + (float)Math.cos(radians) * radius, centerY + (float)Math.sin(radians) * radius);
        }
    }

    private static void beginShape() {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static void endShape() {
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GlStateManager.resetColor();
    }

    private static void color(int argb) {
        GL11.glColor4f(
            (float)(argb >> 16 & 255) / 255.0F,
            (float)(argb >> 8 & 255) / 255.0F,
            (float)(argb & 255) / 255.0F,
            (float)(argb >>> 24) / 255.0F
        );
    }
}
