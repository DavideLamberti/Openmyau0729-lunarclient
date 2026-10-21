package myau;

import java.awt.Color;
import java.util.stream.Collectors;
import myau.mixin.AccessorMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class TracersModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   public final ModeSetting color = new ModeSetting("color", 0, new String[]{"DEFAULT", "TEAMS", "HUD"});
   public final BooleanSetting lines = new BooleanSetting("lines", true);
   public final BooleanSetting arrows = new BooleanSetting("arrows", false);
   public final PercentSetting opacity = new PercentSetting("opacity", 100);
   public final BooleanSetting players = new BooleanSetting("players", true);
   public final BooleanSetting friends = new BooleanSetting("friends", true);
   public final BooleanSetting enemies = new BooleanSetting("enemies", true);
   public final BooleanSetting bots = new BooleanSetting("bots", false);

   public TracersModule() {
      super("Tracers", false, 0);
   }

   private boolean shouldTrackPlayer(EntityPlayer player) {
      if (player.deathTime > 0) {
         return false;
      } else if (minecraft.getRenderViewEntity().getDistanceToEntity(player) > 512.0F) {
         return false;
      } else if (EntityUtils.isLocalOrViewPlayer(player)) {
         return false;
      } else if (EntityUtils.isFriend(player)) {
         return this.friends.getValue();
      } else if (EntityUtils.isEnemy(player)) {
         return this.enemies.getValue();
      } else {
         return EntityUtils.isBot(player) ? this.bots.getValue() : this.players.getValue();
      }
   }

   private boolean shouldTrackEntity(Entity entity) {
      return entity instanceof EntityPlayer && this.shouldTrackPlayer((EntityPlayer)entity);
   }

   private Color colorFor(EntityPlayer player, float alpha) {
      if (EntityUtils.isFriend(player)) {
         return withAlpha(MyauClient.friendList.color(), alpha);
      } else if (EntityUtils.isEnemy(player)) {
         return withAlpha(MyauClient.enemyList.color(), alpha);
      } else {
         switch (this.color.getValue()) {
            case 0:
               return EntityUtils.teamColor(player, alpha);
            case 1:
               boolean teammate = EntityUtils.isTeammate(player);
               byte teamColor = (byte)(teammate ? ChatColor.BLUE.getRgb() : ChatColor.RED.getRgb());
               return new Color(teamColor & 16777215 | (int)(alpha * 255.0F) << 24, true);
            case 2:
               int hudColor = ((HUDModule)MyauClient.moduleManager.modules.get(HUDModule.class)).getColor(System.currentTimeMillis()).getRGB();
               return new Color(hudColor & 16777215 | (int)(alpha * 255.0F) << 24, true);
            default:
               return new Color(1.0F, 1.0F, 1.0F, alpha);
         }
      }
   }

   private static Color withAlpha(Color color, float alpha) {
      return new Color((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, alpha);
   }

   @EventHandler
   public void onRenderWorld(RenderWorldEvent event) {
      if (this.isEnabled() && this.lines.getValue()) {
         RenderUtils.beginOverlay();
         int thirdPersonView = minecraft.gameSettings.thirdPersonView;
         Vec3 direction;
         if (thirdPersonView == 0) {
            direction = new Vec3(0.0, 0.0, 1.0)
               .rotatePitch(
                  (float)(
                     -Math.toRadians(
                        (double)MathUtils.lerpFloat(
                           minecraft.getRenderViewEntity().prevRotationPitch,
                           minecraft.getRenderViewEntity().rotationPitch,
                           ((AccessorMinecraft)minecraft).getTimer().renderPartialTicks
                        )
                     )
                  )
               )
               .rotateYaw(
                  (float)(
                     -Math.toRadians(
                        (double)MathUtils.lerpFloat(
                           minecraft.getRenderViewEntity().prevRotationYaw,
                           minecraft.getRenderViewEntity().rotationYaw,
                           ((AccessorMinecraft)minecraft).getTimer().renderPartialTicks
                        )
                     )
                  )
               );
         } else {
            direction = new Vec3(0.0, 0.0, 0.0)
               .rotatePitch(
                  (float)(
                     -Math.toRadians(
                        (double)MathUtils.lerpFloat(
                           minecraft.thePlayer.prevCameraPitch,
                           minecraft.thePlayer.cameraPitch,
                           ((AccessorMinecraft)minecraft).getTimer().renderPartialTicks
                        )
                     )
                  )
               )
               .rotateYaw(
                  (float)(
                     -Math.toRadians(
                        (double)MathUtils.lerpFloat(
                           minecraft.thePlayer.prevCameraYaw,
                           minecraft.thePlayer.cameraYaw,
                           ((AccessorMinecraft)minecraft).getTimer().renderPartialTicks
                        )
                     )
                  )
               );
         }

         Vec3 start = new Vec3(direction.xCoord, direction.yCoord + (double)minecraft.getRenderViewEntity().getEyeHeight(), direction.zCoord);

         for (EntityPlayer player : EntityUtils.entitiesBackToFront()
            .stream()
            .filter(this::shouldTrackEntity)
            .map(EntityPlayer.class::cast)
            .collect(Collectors.toList())) {
            Color lineColor = this.colorFor(player, (float)this.opacity.getValue().intValue() / 100.0F);
            double x = MathUtils.lerpDouble(player.lastTickPosX, player.posX, (double)event.getPartialTicks());
            double y = MathUtils.lerpDouble(player.lastTickPosY, player.posY, (double)event.getPartialTicks())
               - (player.isSneaking() ? 0.125 : 0.0);
            double z = MathUtils.lerpDouble(player.lastTickPosZ, player.posZ, (double)event.getPartialTicks());
            RenderUtils.drawTracer(
               start,
               x,
               y + (double)player.getEyeHeight(),
               z,
               (float)lineColor.getRed() / 255.0F,
               (float)lineColor.getGreen() / 255.0F,
               (float)lineColor.getBlue() / 255.0F,
               (float)lineColor.getAlpha() / 255.0F,
               1.5F
            );
         }

         RenderUtils.endOverlay();
      }
   }

   @EventHandler
   public void onRenderOverlay(RenderOverlayEvent event) {
      boolean drawArrows = this.isEnabled();
      if (drawArrows) {
         drawArrows = this.arrows.getValue();
         if (drawArrows) {
            for (EntityPlayer player : EntityUtils.entitiesBackToFront()
               .stream()
               .filter(this::shouldTrackEntity)
               .map(EntityPlayer.class::cast)
               .collect(Collectors.toList())) {
               float radius = 40.0F;
               float angle = RotationUtils.horizontalYawError(
                  MathUtils.lerpDouble(minecraft.thePlayer.prevPosX, minecraft.thePlayer.posX, (double)event.getPartialTicks()),
                  MathUtils.lerpDouble(minecraft.thePlayer.prevPosZ, minecraft.thePlayer.posZ, (double)event.getPartialTicks()),
                  MathUtils.lerpDouble(player.prevPosX, player.posX, (double)event.getPartialTicks()),
                  MathUtils.lerpDouble(player.prevPosZ, player.posZ, (double)event.getPartialTicks())
               );
               if (minecraft.gameSettings.thirdPersonView == 2) {
                  angle += 180.0F;
               }

               float directionX = (float)Math.sin(Math.toRadians((double)angle));
               float directionY = (float)Math.cos(Math.toRadians((double)angle)) * -1.0F;
               float alpha = this.opacity.getValue().floatValue() / 100.0F;
               float angleFromCenter = Math.abs(MathHelper.wrapAngleTo180_float(angle));
               if (angleFromCenter < 30.0F) {
                  alpha = 0.0F;
               } else if (angleFromCenter < 60.0F) {
                  alpha *= (angleFromCenter - 30.0F) / 30.0F;
               }

               HUDModule hud = (HUDModule)MyauClient.moduleManager.modules.get(HUDModule.class);
               GlStateManager.pushMatrix();
               GlStateManager.scale(hud.scale.getValue(), hud.scale.getValue(), 0.0F);
               GlStateManager.translate(
                  (float)new ScaledResolution(minecraft).getScaledWidth() / 2.0F / hud.scale.getValue(),
                  (float)new ScaledResolution(minecraft).getScaledHeight() / 2.0F / hud.scale.getValue(),
                  0.0F
               );
               GlStateManager.pushMatrix();
               GlStateManager.translate((radius + 15.0F) * directionX + 1.0F, (radius + 15.0F) * directionY + 1.0F, -100.0F);
               RenderUtils.beginOverlay();
               RenderUtils.fillArrow(
                  0.0F, 0.0F, (float)(Math.atan2((double)directionY, (double)directionX) + Math.PI), 10.0F, this.colorFor(player, alpha).getRGB()
               );
               RenderUtils.endOverlay();
               GlStateManager.popMatrix();
               GlStateManager.popMatrix();
            }
         }
      }
   }
}
