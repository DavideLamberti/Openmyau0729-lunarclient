package myau;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.util.ResourceLocation;

public class TargetHUDModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private static final DecimalFormat heartFormat = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));
   private static final DecimalFormat differenceFormat = new DecimalFormat("+0.0;-0.0", new DecimalFormatSymbols(Locale.US));
   private final ElapsedTimer targetTimer = new ElapsedTimer();
   private final ElapsedTimer animationTimer = new ElapsedTimer();
   private EntityLivingBase lastAttackedTarget;
   private EntityLivingBase displayedTarget;
   private ResourceLocation skin;
   private float previousHearts;
   private float targetHearts;
   private float maximumHearts;
   public final ModeSetting color = new ModeSetting("color", 0, new String[]{"DEFAULT", "HUD"});
   public final ModeSetting positionX = new ModeSetting("position-x", 1, new String[]{"LEFT", "MIDDLE", "RIGHT"});
   public final ModeSetting positionY = new ModeSetting("position-y", 1, new String[]{"TOP", "MIDDLE", "BOTTOM"});
   public final FloatSetting scale = new FloatSetting("scale", 1.0F, 0.5F, 1.5F);
   public final IntegerSetting offsetX = new IntegerSetting("offset-x", 0, -255, 255);
   public final IntegerSetting offsetY = new IntegerSetting("offset-y", 40, -255, 255);
   public final PercentSetting background = new PercentSetting("background", 25);
   public final BooleanSetting head = new BooleanSetting("head", true);
   public final BooleanSetting indicator = new BooleanSetting("indicator", true);
   public final BooleanSetting outline = new BooleanSetting("outline", false);
   public final BooleanSetting animations = new BooleanSetting("animations", true);
   public final BooleanSetting shadow = new BooleanSetting("shadow", true);
   public final BooleanSetting kaOnly = new BooleanSetting("ka-only", true);
   public final BooleanSetting chatPreview = new BooleanSetting("chat-preview", false);

   public TargetHUDModule() {
      super("TargetHUD", false, 0, true);
   }

   private EntityLivingBase selectTarget() {
      KillAuraModule combatModule = (KillAuraModule)MyauClient.moduleManager.modules.get(KillAuraModule.class);
      boolean combatEnabled = combatModule.isEnabled();
      if (combatEnabled && combatModule.isCombatInputAllowed() && EntityUtils.isLoaded(combatModule.getTargetEntity())) {
         return combatModule.getTargetEntity();
      } else if (!this.kaOnly.getValue() && !this.targetTimer.hasElapsed(1500L) && EntityUtils.isLoaded(this.lastAttackedTarget)) {
         return this.lastAttackedTarget;
      } else {
         return this.chatPreview.getValue() && minecraft.currentScreen instanceof GuiChat ? minecraft.thePlayer : null;
      }
   }

   private ResourceLocation skinFor(EntityLivingBase target) {
      if (target instanceof EntityPlayer) {
         NetworkPlayerInfo info = minecraft.getNetHandler().getPlayerInfo(target.getName());
         if (info != null) {
            return info.getLocationSkin();
         }
      }

      return null;
   }

   private Color colorFor(EntityLivingBase target) {
      boolean player = target instanceof EntityPlayer;
      if (player) {
         if (EntityUtils.isFriend((EntityPlayer)target)) {
            return MyauClient.friendList.color();
         }

         if (EntityUtils.isEnemy((EntityPlayer)target)) {
            return MyauClient.enemyList.color();
         }
      }

      switch (this.color.getValue()) {
         case 0:
            return player ? EntityUtils.teamColor((EntityPlayer)target, 1.0F) : new Color(-1);
         case 1:
            int hudColor = ((HUDModule)MyauClient.moduleManager.modules.get(HUDModule.class)).getColor(System.currentTimeMillis()).getRGB();
            return new Color(hudColor);
         default:
            return new Color(-1);
      }
   }

   @EventHandler
   public void onRenderOverlay(RenderOverlayEvent event) {
      if (this.isEnabled()) {
         if (minecraft.thePlayer != null) {
            EntityLivingBase previousTarget = this.displayedTarget;
            this.displayedTarget = this.selectTarget();
            if (this.displayedTarget != null) {
               float playerHearts = (minecraft.thePlayer.getHealth() + minecraft.thePlayer.getAbsorptionAmount()) / 2.0F;
               float absorptionHearts = this.displayedTarget.getAbsorptionAmount() / 2.0F;
               float totalHearts = this.displayedTarget.getHealth() / 2.0F + absorptionHearts;
               if (this.displayedTarget != previousTarget) {
                  this.skin = null;
                  this.animationTimer.setLastResetMillis(0L);
                  this.previousHearts = totalHearts;
                  this.targetHearts = totalHearts;
               }

               boolean animateHealth = this.animations.getValue();
               if (!animateHealth || this.animationTimer.hasElapsed(150L)) {
                  this.previousHearts = this.targetHearts;
                  this.targetHearts = totalHearts;
                  this.maximumHearts = this.displayedTarget.getMaxHealth() / 2.0F;
                  if (this.previousHearts != this.targetHearts) {
                     this.animationTimer.reset();
                  }
               }

               ResourceLocation currentSkin = this.skinFor(this.displayedTarget);
               if (currentSkin != null) {
                  this.skin = currentSkin;
               }

               float elapsed = (float)Math.min(Math.max(this.animationTimer.elapsedMillis(), 0L), 150L);
               float interpolatedHearts = MathUtils.lerpFloat(this.previousHearts, this.targetHearts, elapsed / 150.0F);
               float healthFraction = Math.min(Math.max(interpolatedHearts / this.maximumHearts, 0.0F), 1.0F);
               Color targetColor = this.colorFor(this.displayedTarget);
               Color healthColor = this.color.getValue() == 0 ? ColorUtils.healthGradient(healthFraction) : targetColor;
               float advantage = Math.min(Math.max((playerHearts - totalHearts + 1.0F) / 2.0F, 0.0F), 1.0F);
               Color advantageColor = ColorUtils.healthGradient(advantage);
               ScaledResolution resolution = new ScaledResolution(minecraft);
               String nameLabel = ChatColor.formatText(String.format("&r%s&r", EntityUtils.formattedName(this.displayedTarget)));
               int nameWidth = minecraft.fontRendererObj.getStringWidth(nameLabel);
               String healthLabel = ChatColor.formatText(
                  String.format("&r&f%s%s❤&r", heartFormat.format((double)totalHearts), absorptionHearts > 0.0F ? "&6" : "&c")
               );
               int healthWidth = minecraft.fontRendererObj.getStringWidth(healthLabel);
               String resultLabel = ChatColor.formatText(
                  String.format("&r&l%s&r", totalHearts == playerHearts ? "D" : (totalHearts < playerHearts ? "W" : "L"))
               );
               int resultWidth = minecraft.fontRendererObj.getStringWidth(resultLabel);
               String differenceLabel = ChatColor.formatText(
                  String.format("&r%s&r", totalHearts == playerHearts ? "0.0" : differenceFormat.format((double)(playerHearts - totalHearts)))
               );
               int differenceWidth = minecraft.fontRendererObj.getStringWidth(differenceLabel);
               float firstRowWidth = (float)nameWidth + (this.indicator.getValue() ? 2.0F + (float)resultWidth + 2.0F : 0.0F);
               float secondRowWidth = (float)healthWidth + (this.indicator.getValue() ? 2.0F + (float)differenceWidth + 2.0F : 0.0F);
               float contentWidth = Math.max(firstRowWidth, secondRowWidth);
               float headWidth = this.head.getValue() && this.skin != null ? 25.0F : 0.0F;
               float width = Math.max(headWidth + 70.0F, headWidth + 2.0F + contentWidth + 2.0F);
               float height = 27.0F;
               float x = this.offsetX.getValue().floatValue() / this.scale.getValue();
               switch (this.positionX.getValue()) {
                  case 1:
                     x += (float)resolution.getScaledWidth() / this.scale.getValue() / 2.0F - width / 2.0F;
                     break;
                  case 2:
                     x *= -1.0F;
                     x += (float)resolution.getScaledWidth() / this.scale.getValue() - width;
               }

               float y = this.offsetY.getValue().floatValue() / this.scale.getValue();
               switch (this.positionY.getValue()) {
                  case 1:
                     y += (float)resolution.getScaledHeight() / this.scale.getValue() / 2.0F - height / 2.0F;
                     break;
                  case 2:
                     y *= -1.0F;
                     y += (float)resolution.getScaledHeight() / this.scale.getValue() - height;
               }

               GlStateManager.pushMatrix();
               GlStateManager.scale(this.scale.getValue(), this.scale.getValue(), 0.0F);
               GlStateManager.translate(x, y, -450.0F);
               RenderUtils.beginOverlay();
               int backgroundColor = new Color(0.0F, 0.0F, 0.0F, (float)this.background.getValue().intValue() / 100.0F).getRGB();
               int outlineColor = this.outline.getValue() ? targetColor.getRGB() : new Color(0, 0, 0, 0).getRGB();
               RenderUtils.drawBorderedRect(0.0F, 0.0F, width, height, 1.5F, backgroundColor, outlineColor);
               RenderUtils.fillRect(headWidth + 2.0F, 22.0F, width - 2.0F, 25.0F, ColorUtils.scaleBrightness(healthColor, 0.2F).getRGB());
               RenderUtils.fillRect(headWidth + 2.0F, 22.0F, headWidth + 2.0F + healthFraction * (width - 2.0F - headWidth - 2.0F), 25.0F, healthColor.getRGB());
               RenderUtils.endOverlay();
               GlStateManager.disableDepth();
               GlStateManager.enableBlend();
               GlStateManager.blendFunc(770, 771);
               minecraft.fontRendererObj.drawString(nameLabel, headWidth + 2.0F, 2.0F, -1, this.shadow.getValue());
               minecraft.fontRendererObj.drawString(healthLabel, headWidth + 2.0F, 12.0F, -1, this.shadow.getValue());
               if (this.indicator.getValue()) {
                  minecraft.fontRendererObj.drawString(resultLabel, width - 2.0F - (float)resultWidth, 2.0F, advantageColor.getRGB(), this.shadow.getValue());
                  minecraft.fontRendererObj
                     .drawString(
                        differenceLabel,
                        width - 2.0F - (float)differenceWidth,
                        12.0F,
                        ColorUtils.scaleBrightness(advantageColor, 0.8F).getRGB(),
                        this.shadow.getValue()
                     );
               }

               if (this.head.getValue() && this.skin != null) {
                  GlStateManager.color(1.0F, 1.0F, 1.0F);
                  minecraft.getTextureManager().bindTexture(this.skin);
                  Gui.drawScaledCustomSizeModalRect(2, 2, 8.0F, 8.0F, 8, 8, 23, 23, 64.0F, 64.0F);
                  Gui.drawScaledCustomSizeModalRect(2, 2, 40.0F, 8.0F, 8, 8, 23, 23, 64.0F, 64.0F);
                  GlStateManager.color(1.0F, 1.0F, 1.0F);
               }

               GlStateManager.disableBlend();
               GlStateManager.enableDepth();
               GlStateManager.popMatrix();
            }
         }
      }
   }

   @EventHandler
   public void onPacketSend(PacketSendEvent event) {
      boolean attack = this.isEnabled();
      if (attack && !event.isCancelled()) {
         attack = event.getPacket() instanceof C02PacketUseEntity;
         if (attack) {
            Packet raw = event.getPacket();
            C02PacketUseEntity packet = (C02PacketUseEntity)raw;
            if (packet.getAction() == Action.ATTACK) {
               Entity entity = packet.getEntityFromWorld(minecraft.theWorld);
               if (entity instanceof EntityLivingBase && !(entity instanceof EntityArmorStand)) {
                  this.targetTimer.reset();
                  this.lastAttackedTarget = (EntityLivingBase)entity;
               }
            }
         }
      }
   }
}
