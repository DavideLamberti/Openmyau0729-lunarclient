package myau;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import myau.mixin.AccessorGuiChat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class HUDModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   public final ModeSetting color = new ModeSetting("color", 3, new String[]{"RAINBOW", "CHROMA", "ASTOLFO", "CUSTOM1", "CUSTOM12", "CUSTOM123"});
   public final FloatSetting colorSpeed = new FloatSetting("color-speed", 1.0F, 0.5F, 1.5F);
   public final PercentSetting colorSaturation = new PercentSetting("color-saturation", 50);
   public final PercentSetting colorBrightness = new PercentSetting("color-brightness", 100);
   public final ColorSetting customColor1 = new ColorSetting("custom-color-1", 16777215, this::usesFirstCustomColor);
   public final ColorSetting customColor2 = new ColorSetting("custom-color-2", 16777215, this::usesSecondCustomColor);
   public final ColorSetting customColor3 = new ColorSetting("custom-color-3", 16777215, this::usesThirdCustomColor);
   public final ModeSetting positionX = new ModeSetting("position-x", 0, new String[]{"LEFT", "RIGHT"});
   public final ModeSetting positionY = new ModeSetting("position-y", 0, new String[]{"TOP", "BOTTOM"});
   public final IntegerSetting offsetX = new IntegerSetting("offset-x", 2, 0, 255);
   public final IntegerSetting offsetY = new IntegerSetting("offset-y", 2, 0, 255);
   public final FloatSetting scale = new FloatSetting("scale", 1.0F, 0.5F, 1.5F);
   public final PercentSetting background = new PercentSetting("background", 25);
   public final BooleanSetting bar = new BooleanSetting("bar", true);
   public final BooleanSetting shadow = new BooleanSetting("shadow", true);
   public final BooleanSetting suffixes = new BooleanSetting("suffixes", true);
   public final BooleanSetting lowerCase = new BooleanSetting("lower-case", false);
   public final BooleanSetting chatOutline = new BooleanSetting("chat-outline", true);
   public final BooleanSetting blinkTimer = new BooleanSetting("blink-timer", true);
   public final BooleanSetting toggleSounds = new BooleanSetting("toggle-sounds", true);
   public final BooleanSetting toggleAlerts = new BooleanSetting("toggle-alerts", true);

   public HUDModule() {
      super("HUD", true, 0, true);
   }

   private String displayName(Module module) {
      String name = module.getName();
      return this.lowerCase.getValue() ? name.toLowerCase(Locale.ROOT) : name;
   }

   private String[] displaySuffixes(Module module) {
      String[] values = module.getHudInfo();
      boolean lowercase = this.lowerCase.getValue();
      if (!lowercase) {
         return values;
      } else {
         for (int index = 0; index < values.length; index++) {
            values[index] = values[index].toLowerCase();
         }

         return values;
      }
   }

   private int displayWidth(String name, String[] values) {
      int width = minecraft.fontRendererObj.getStringWidth(name);
      boolean includeSuffixes = this.suffixes.getValue();
      if (includeSuffixes) {
         for (String suffix : values) {
            width += 3 + minecraft.fontRendererObj.getStringWidth(suffix);
         }
      }

      return width;
   }

   private int moduleWidth(Module module) {
      return this.displayWidth(this.displayName(module), this.displaySuffixes(module));
   }

   private float colorPhase(long time, long row) {
      long period = (long)(3000.0 / Math.pow((double)Math.min(Math.max(0.5F, this.colorSpeed.getValue()), 1.5F), 3.0));
      return 1.0F - (float)(Math.abs(time - row * 300L) % period) / (float)period;
   }

   public Color getColor(long time) {
      return this.getColorForRow(time, 0L);
   }

   public Color getColor(long time, long row) {
      return this.getColorForRow(time, row);
   }

   public Color getColorForRow(long time, long row) {
      Color base = Color.white;
      int mode = this.color.getValue();
      switch (mode) {
         case 0:
            base = ColorUtils.fromHSB(this.colorPhase(time, row), 1.0F, 1.0F);
            break;
         case 1:
            base = ColorUtils.fromHSB(this.colorPhase(time / 3L, 0L), 1.0F, 1.0F);
            break;
         case 2:
            float hue = this.colorPhase(time, row);
            if (hue % 1.0F < 0.5F) {
               hue = 1.0F - hue % 1.0F;
            }

            base = ColorUtils.fromHSB(hue, 1.0F, 1.0F);
            break;
         case 3:
            base = new Color(this.customColor1.getValue());
            break;
         case 4:
            double phase = (double)this.colorPhase(time, row);
            float blend = (float)(2.0 * Math.abs(phase - Math.floor(phase + 0.5)));
            base = ColorUtils.interpolate(blend, new Color(this.customColor1.getValue()), new Color(this.customColor2.getValue()));
            break;
         case 5:
            double threeColorPhase = (double)this.colorPhase(time, row);
            float threeColorBlend = (float)(2.0 * Math.abs(threeColorPhase - Math.floor(threeColorPhase + 0.5)));
            if (threeColorBlend <= 0.5F) {
               base = ColorUtils.interpolate(threeColorBlend * 2.0F, new Color(this.customColor1.getValue()), new Color(this.customColor2.getValue()));
            } else {
               base = ColorUtils.interpolate((threeColorBlend - 0.5F) * 2.0F, new Color(this.customColor2.getValue()), new Color(this.customColor3.getValue()));
            }
      }

      float[] hsb = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
      return Color.getHSBColor(
         hsb[0], hsb[1] * (this.colorSaturation.getValue().floatValue() / 100.0F), hsb[2] * (this.colorBrightness.getValue().floatValue() / 100.0F)
      );
   }

   @EventHandler
   public void onRenderOverlay(RenderOverlayEvent event) {
      boolean outline = this.chatOutline.getValue();
      if (outline && minecraft.currentScreen instanceof GuiChat) {
         String input = ((AccessorGuiChat)minecraft.currentScreen).getInputField().getText().trim();
         if (MyauClient.commandManager != null && MyauClient.commandManager.isClientCommand(input)) {
            RenderUtils.beginOverlay();
            RenderUtils.drawBorderedRect(
               2.0F,
               (float)(minecraft.currentScreen.height - 14),
               (float)(minecraft.currentScreen.width - 2),
               (float)(minecraft.currentScreen.height - 2),
               1.5F,
               0,
               this.getColor(System.currentTimeMillis()).getRGB()
            );
            RenderUtils.endOverlay();
         }
      }

      if (this.isEnabled()) {
         if (!minecraft.gameSettings.showDebugInfo) {
            List<Module> modules = MyauClient.moduleManager
               .modules
               .values()
               .stream()
               .filter(HUDModule::shouldDisplay)
               .sorted(Comparator.comparingInt(this::moduleWidth).reversed())
               .collect(Collectors.toList());
            float rowHeight = (float)minecraft.fontRendererObj.FONT_HEIGHT - 1.0F;
            float xOffset = (float)this.offsetX.getValue().intValue();
            boolean showBar = this.bar.getValue();
            float barSpace = !showBar ? 0.0F : (this.shadow.getValue() ? 2.0F : 1.0F);
            float x = xOffset + (1.0F + barSpace) * this.scale.getValue();
            float y = (float)this.offsetY.getValue().intValue() + 1.0F * this.scale.getValue();
            int horizontalPosition = this.positionX.getValue();
            if (horizontalPosition == 1) {
               x = (float)new ScaledResolution(minecraft).getScaledWidth() - x;
            }

            int verticalPosition = this.positionY.getValue();
            if (verticalPosition == 1) {
               y = (float)new ScaledResolution(minecraft).getScaledHeight() - y - rowHeight * this.scale.getValue();
            }

            GlStateManager.pushMatrix();
            GlStateManager.scale(this.scale.getValue(), this.scale.getValue(), 0.0F);
            long now = System.currentTimeMillis();
            long row = 0L;

            for (Module module : modules) {
               String name = this.displayName(module);
               String[] values = this.displaySuffixes(module);
               int width = this.displayWidth(name, values);
               float rowWidth = (float)(width - (this.shadow.getValue() ? 0 : 1));
               int rgb = this.getColorForRow(now, row).getRGB();
               RenderUtils.beginOverlay();
               if (this.background.getValue() > 0) {
                  float left = x / this.scale.getValue() - 1.0F - (this.positionX.getValue() == 0 ? 0.0F : rowWidth);
                  float top = y / this.scale.getValue() - (this.positionY.getValue() == 0 ? (row == 0L ? 1.0F : 0.0F) : (this.shadow.getValue() ? 1.0F : 0.0F));
                  float right = x / this.scale.getValue() + 1.0F + (this.positionX.getValue() == 0 ? rowWidth : 0.0F);
                  float bottom = y / this.scale.getValue()
                     + rowHeight
                     + (this.positionY.getValue() == 0 ? (this.shadow.getValue() ? 1.0F : 0.0F) : (row == 0L ? 1.0F : 0.0F));
                  RenderUtils.fillRect(left, top, right, bottom, new Color(0.0F, 0.0F, 0.0F, this.background.getValue().floatValue() / 100.0F).getRGB());
               }

               if (this.bar.getValue()) {
                  if (this.shadow.getValue()) {
                     RenderUtils.fillRect(
                        x / this.scale.getValue() + (this.positionX.getValue() == 0 ? -3.0F : 1.0F),
                        y / this.scale.getValue() - (this.positionY.getValue() == 0 ? (row == 0L ? 1.0F : 0.0F) : 1.0F),
                        x / this.scale.getValue() + (this.positionX.getValue() == 0 ? -2.0F : 2.0F),
                        y / this.scale.getValue() + rowHeight + (this.positionY.getValue() != 0 && row != 0L ? 0.0F : 1.0F),
                        rgb
                     );
                     RenderUtils.fillRect(
                        x / this.scale.getValue() + (this.positionX.getValue() == 0 ? -2.0F : 2.0F),
                        y / this.scale.getValue() - (this.positionY.getValue() == 0 ? (row == 0L ? 1.0F : 0.0F) : 1.0F),
                        x / this.scale.getValue() + (this.positionX.getValue() == 0 ? -1.0F : 3.0F),
                        y / this.scale.getValue() + rowHeight + (this.positionY.getValue() != 0 && row != 0L ? 0.0F : 1.0F),
                        (rgb & 16579836) >> 2 | rgb & 0xFF000000
                     );
                  } else {
                     RenderUtils.fillRect(
                        x / this.scale.getValue() + (this.positionX.getValue() == 0 ? -2.0F : 1.0F),
                        y / this.scale.getValue() - (this.positionY.getValue() == 0 && row == 0L ? 1.0F : 0.0F),
                        x / this.scale.getValue() + (this.positionX.getValue() == 0 ? -1.0F : 2.0F),
                        y / this.scale.getValue() + rowHeight + (this.positionY.getValue() != 0 && row == 0L ? 1.0F : 0.0F),
                        rgb
                     );
                  }
               }

               RenderUtils.endOverlay();
               GlStateManager.disableDepth();
               this.drawLabel(name, x, y, rowWidth, false, 0.0F, rgb);
               if (this.suffixes.getValue() && values.length > 0) {
                  float suffixOffset = (float)minecraft.fontRendererObj.getStringWidth(name) + 3.0F;

                  for (String suffix : values) {
                     this.drawLabel(suffix, x, y, rowWidth, true, suffixOffset, ChatColor.GRAY.getRgb());
                     suffixOffset += (float)minecraft.fontRendererObj.getStringWidth(suffix) + (this.shadow.getValue() ? 3.0F : 2.0F);
                  }
               }

               y += (rowHeight + (this.shadow.getValue() ? 1.0F : 0.0F)) * this.scale.getValue() * (this.positionY.getValue() == 0 ? 1.0F : -1.0F);
               row++;
            }

            if (this.blinkTimer.getValue()) {
               OutgoingBufferOwner mode = MyauClient.outgoingPacketBuffer.owner();
               if (mode != OutgoingBufferOwner.NONE && mode != OutgoingBufferOwner.AUTO_BLOCK) {
                  long elapsed = MyauClient.outgoingPacketBuffer.movementPacketCount();
                  if (elapsed > 0L) {
                     GlStateManager.enableBlend();
                     GlStateManager.blendFunc(770, 771);
                     minecraft.fontRendererObj
                        .drawString(
                           String.valueOf(elapsed),
                           (float)new ScaledResolution(minecraft).getScaledWidth() / 2.0F / this.scale.getValue()
                              - (float)minecraft.fontRendererObj.getStringWidth(String.valueOf(elapsed)) / 2.0F,
                           (float)new ScaledResolution(minecraft).getScaledHeight() / 5.0F * 3.0F / this.scale.getValue(),
                           this.getColorForRow(now, row).getRGB() & 16777215 | -1090519040,
                           this.shadow.getValue()
                        );
                     GlStateManager.disableBlend();
                  }
               }
            }

            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
         }
      }
   }

   private void drawLabel(String text, float x, float y, float rowWidth, boolean isSuffix, float suffixOffset, int rgb) {
      if (this.shadow.getValue()) {
         minecraft.fontRendererObj.drawStringWithShadow(text, this.labelX(x, rowWidth, isSuffix, suffixOffset), y / this.scale.getValue(), rgb);
      } else {
         minecraft.fontRendererObj
            .drawString(
               text, this.labelX(x, rowWidth, isSuffix, suffixOffset), y / this.scale.getValue() + (this.positionY.getValue() == 1 ? 1.0F : 0.0F), rgb, false
            );
      }
   }

   private float labelX(float x, float rowWidth, boolean isSuffix, float suffixOffset) {
      float position = x / this.scale.getValue() - (this.positionX.getValue() == 1 ? rowWidth : 0.0F);
      return isSuffix ? position + suffixOffset : position;
   }

   private static boolean shouldDisplay(Module module) {
      boolean enabled = module.isEnabled();
      return enabled && !module.isHidden();
   }

   private boolean usesFirstCustomColor() {
      int mode = this.color.getValue();
      return mode == 3 || this.color.getValue() == 4 || this.color.getValue() == 5;
   }

   private boolean usesSecondCustomColor() {
      int mode = this.color.getValue();
      return mode == 4 || this.color.getValue() == 5;
   }

   private boolean usesThirdCustomColor() {
      int mode = this.color.getValue();
      return mode == 5;
   }
}
