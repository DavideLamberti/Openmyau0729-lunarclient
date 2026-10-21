package myau;

import java.awt.Color;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class IndicatorsModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   public final FloatSetting scale = new FloatSetting("scale", 1.0F, 0.5F, 1.5F);
   public final FloatSetting offset = new FloatSetting("offset", 50.0F, 0.0F, 255.0F);
   public final BooleanSetting directionCheck = new BooleanSetting("direction-check", true);
   public final BooleanSetting fireballs = new BooleanSetting("fireballs", true);
   public final BooleanSetting pearls = new BooleanSetting("pearls", true);
   public final BooleanSetting arrows = new BooleanSetting("arrows", true);

   public IndicatorsModule() {
      super("Indicators", false, 0, true);
   }

   private boolean shouldIndicate(Entity entity) {
      double approach = (entity.posX - entity.lastTickPosX) * (minecraft.thePlayer.posX - entity.posX)
         + (entity.posY - entity.lastTickPosY)
            * (
               minecraft.thePlayer.posY
                  + (double)minecraft.thePlayer.getEyeHeight()
                  - entity.posY
                  - (double)entity.height / 2.0
            )
         + (entity.posZ - entity.lastTickPosZ) * (minecraft.thePlayer.posZ - entity.posZ);
      if (approach != 0.0 && (!(approach < 0.0) || !this.directionCheck.getValue())) {
         if (this.fireballs.getValue() && entity instanceof EntityFireball) {
            return true;
         } else {
            return this.pearls.getValue() && entity instanceof EntityEnderPearl ? true : this.arrows.getValue() && entity instanceof EntityArrow;
         }
      } else {
         return false;
      }
   }

   private Item iconFor(Entity entity) {
      if (entity instanceof EntityFireball) {
         return Items.fire_charge;
      } else if (entity instanceof EntityEnderPearl) {
         return Items.ender_pearl;
      } else {
         return entity instanceof EntityArrow ? Items.arrow : new Item();
      }
   }

   private Color colorFor(Entity entity) {
      if (entity instanceof EntityFireball) {
         return new Color(12676363);
      } else {
         return entity instanceof EntityEnderPearl ? new Color(2458740) : new Color(entity instanceof EntityArrow ? 9868950 : -1);
      }
   }

   @EventHandler(
      priority = 50
   )
   public void onRenderOverlay(RenderOverlayEvent event) {
      if (this.isEnabled()) {
         for (Entity entity : EntityUtils.entitiesBackToFront().stream().filter(this::shouldIndicate).collect(Collectors.toList())) {
            float radius = 10.0F + this.offset.getValue();
            float angle = RotationUtils.horizontalYawError(
               MathUtils.lerpDouble(minecraft.thePlayer.prevPosX, minecraft.thePlayer.posX, (double)event.getPartialTicks()),
               MathUtils.lerpDouble(minecraft.thePlayer.prevPosZ, minecraft.thePlayer.posZ, (double)event.getPartialTicks()),
               MathUtils.lerpDouble(entity.prevPosX, entity.posX, (double)event.getPartialTicks()),
               MathUtils.lerpDouble(entity.prevPosZ, entity.posZ, (double)event.getPartialTicks())
            );
            if (minecraft.gameSettings.thirdPersonView == 2) {
               angle += 180.0F;
            }

            float directionX = (float)Math.sin(Math.toRadians((double)angle));
            float directionY = (float)Math.cos(Math.toRadians((double)angle)) * -1.0F;
            GlStateManager.pushMatrix();
            GlStateManager.disableDepth();
            GlStateManager.scale(this.scale.getValue(), this.scale.getValue(), 0.0F);
            GlStateManager.translate(
               (float)new ScaledResolution(minecraft).getScaledWidth() / 2.0F / this.scale.getValue(),
               (float)new ScaledResolution(minecraft).getScaledHeight() / 2.0F / this.scale.getValue(),
               0.0F
            );
            GlStateManager.pushMatrix();
            GlStateManager.translate((radius + 0.0F) * directionX - 8.0F, (radius + 0.0F) * directionY - 8.0F, -300.0F);
            minecraft.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(this.iconFor(entity)), 0, 0);
            GlStateManager.popMatrix();
            String distanceLabel = String.format("%dm", (int)minecraft.thePlayer.getDistanceToEntity(entity));
            GlStateManager.pushMatrix();
            GlStateManager.translate(
               (radius + 0.0F) * directionX - (float)minecraft.fontRendererObj.getStringWidth(distanceLabel) / 2.0F + 1.0F,
               (radius + 0.0F) * directionY + 1.0F,
               -100.0F
            );
            minecraft.fontRendererObj.drawStringWithShadow(distanceLabel, 0.0F, 0.0F, ChatColor.GRAY.getRgb() & 16777215 | -1090519040);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.translate((radius + 15.0F) * directionX + 1.0F, (radius + 15.0F) * directionY + 1.0F, -100.0F);
            RenderUtils.beginOverlay();
            RenderUtils.drawChevron(
               0.0F, 0.0F, (float)(Math.atan2((double)directionY, (double)directionX) + Math.PI), 7.5F, 1.5F, this.colorFor(entity).getRGB()
            );
            RenderUtils.endOverlay();
            GlStateManager.popMatrix();
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
         }
      }
   }
}
