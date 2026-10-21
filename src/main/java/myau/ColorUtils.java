package myau;

import java.awt.Color;

public final class ColorUtils {
   public static final Color RED = new Color(255, 0, 0);
   public static final Color ORANGE = new Color(255, 165, 0);
   public static final Color YELLOW = new Color(255, 255, 0);
   public static final Color GREEN = new Color(0, 255, 0);

   private ColorUtils() {
   }

   public static Color fromHSB(float hue, float saturation, float brightness) {
      return new Color(Color.HSBtoRGB(hue, saturation, brightness));
   }

   public static Color interpolate(float fraction, Color startColor, Color endColor) {
      fraction = Math.min(Math.max(fraction, 0.0F), 1.0F);
      return new Color(
         (int)((float)startColor.getRed() + fraction * (float)(endColor.getRed() - startColor.getRed())),
         (int)((float)startColor.getGreen() + fraction * (float)(endColor.getGreen() - startColor.getGreen())),
         (int)((float)startColor.getBlue() + fraction * (float)(endColor.getBlue() - startColor.getBlue()))
      );
   }

   public static Color healthGradient(float fraction) {
      if (fraction >= 0.9F) {
         return GREEN;
      } else if (fraction >= 0.55F) {
         return interpolate((fraction - 0.55F) / 0.35F, YELLOW, GREEN);
      } else if (fraction >= 0.45F) {
         return YELLOW;
      } else {
         return fraction >= 0.1F ? interpolate((fraction - 0.1F) / 0.35F, RED, YELLOW) : RED;
      }
   }

   public static Color scaleBrightness(Color color, float multiplier) {
      return scaleBrightnessWithAlpha(color, multiplier, color.getAlpha());
   }

   public static Color scaleBrightnessWithAlpha(Color color, float multiplier, int alpha) {
      return new Color(
         Math.min(Math.max((int)((float)color.getRed() * multiplier), 0), 255),
         Math.min(Math.max((int)((float)color.getGreen() * multiplier), 0), 255),
         Math.min(Math.max((int)((float)color.getBlue() * multiplier), 0), 255),
         alpha
      );
   }
}
