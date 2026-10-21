package myau;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;

public class TargetStrafeModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private EntityLivingBase strafeTarget;
   private float strafeYaw = Float.NaN;
   private int strafeDirection = 1;
   public final FloatSetting radius = new FloatSetting("radius", 1.0F, 0.0F, 6.0F);
   public final IntegerSetting points = new IntegerSetting("points", 6, 3, 24);
   public final BooleanSetting requirePress = new BooleanSetting("require-press", true);
   public final BooleanSetting speedOnly = new BooleanSetting("speed-only", true);
   public final ModeSetting showTarget = new ModeSetting("show-target", 1, new String[]{"NONE", "DEFAULT", "HUD"});

   public TargetStrafeModule() {
      super("TargetStrafe", false, 0);
   }

   private boolean canStrafe() {
      if (this.speedOnly.getValue()) {
         SpeedModule speed = (SpeedModule)MyauClient.moduleManager.modules.get(SpeedModule.class);
         FlyModule fly = (FlyModule)MyauClient.moduleManager.modules.get(FlyModule.class);
         LongJumpModule longJump = (LongJumpModule)MyauClient.moduleManager.modules.get(LongJumpModule.class);
         boolean movementModuleActive = speed.isEnabled() || fly.isEnabled() || longJump.isEnabled() && longJump.hasJumpStarted();
         if (!movementModuleActive) {
            return false;
         }
      }

      return !this.requirePress.getValue() || PlayerUtils.isJumpKeyDown();
   }

   private EntityLivingBase findTarget() {
      KillAuraModule killAura = (KillAuraModule)MyauClient.moduleManager.modules.get(KillAuraModule.class);
      if (killAura.isEnabled() && killAura.isCombatInputAllowed()) {
         EntityLivingBase target = killAura.getTargetEntity();
         return EntityUtils.isLoaded(target) ? target : null;
      } else {
         return null;
      }
   }

   private Color targetColor(EntityLivingBase target) {
      if (target instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)target;
         if (EntityUtils.isFriend(player)) {
            return MyauClient.friendList.color();
         }

         if (EntityUtils.isEnemy(player)) {
            return MyauClient.enemyList.color();
         }
      }

      switch (this.showTarget.getValue()) {
         case 1:
            return target instanceof EntityPlayer ? EntityUtils.teamColor((EntityPlayer)target, 1.0F) : Color.WHITE;
         case 2:
            HUDModule hud = (HUDModule)MyauClient.moduleManager.modules.get(HUDModule.class);
            return new Color(hud.getColor(System.currentTimeMillis()).getRGB());
         default:
            return Color.WHITE;
      }
   }

   private boolean isPointOverVoid(double x, double z) {
      return PlayerUtils.isBoundsOverVoid(
         new AxisAlignedBB(
            x - 0.015,
            minecraft.thePlayer.posY,
            z - 0.015,
            x + 0.015,
            minecraft.thePlayer.posY + (double)minecraft.thePlayer.height,
            z + 0.015
         )
      );
   }

   private static int wrapPointIndex(int index, int size) {
      if (index < 0) {
         return size - 1;
      } else {
         return index >= size ? 0 : index;
      }
   }

   public float getStrafeYaw() {
      return this.strafeYaw;
   }

   @EventHandler(
      priority = 100
   )
   public void onRotationUpdate(RotationUpdateEvent event) {
      if (this.isEnabled()) {
         boolean leftPressed = PlayerUtils.isLeftKeyDown();
         boolean rightPressed = PlayerUtils.isRightKeyDown();
         if (leftPressed ^ rightPressed) {
            this.strafeDirection = leftPressed ? 1 : -1;
         }

         if (!this.canStrafe()) {
            this.strafeTarget = null;
            this.strafeYaw = Float.NaN;
         } else {
            this.strafeTarget = this.findTarget();
            if (this.strafeTarget == null) {
               this.strafeYaw = Float.NaN;
            } else {
               List<StrafeOffset> orbitPoints = new ArrayList<>();

               for (int index = 0; index < this.points.getValue(); index++) {
                  double angle = (double)index * ((Math.PI * 2) / (double)this.points.getValue().intValue());
                  orbitPoints.add(
                     new StrafeOffset(
                        (double)this.radius.getValue().floatValue() * Math.cos(angle), (double)this.radius.getValue().floatValue() * Math.sin(angle)
                     )
                  );
               }

               if (orbitPoints.isEmpty()) {
                  this.strafeTarget = null;
                  this.strafeYaw = Float.NaN;
               } else {
                  double nearestDistance = 0.0;
                  int nearestIndex = -1;

                  for (int index = 0; index < orbitPoints.size(); index++) {
                     StrafeOffset point = orbitPoints.get(index);
                     double distance = minecraft.thePlayer
                        .getDistance(
                           this.strafeTarget.posX + point.x(), minecraft.thePlayer.posY, this.strafeTarget.posZ + point.z()
                        );
                     if (nearestIndex == -1 || distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestIndex = index;
                     }
                  }

                  if (minecraft.thePlayer.isCollidedHorizontally) {
                     this.strafeDirection *= -1;
                  }

                  int nextIndex = wrapPointIndex(nearestIndex + this.strafeDirection, orbitPoints.size());
                  StrafeOffset nextPoint = orbitPoints.get(nextIndex);
                  double targetX = this.strafeTarget.posX + nextPoint.x();
                  double targetZ = this.strafeTarget.posZ + nextPoint.z();
                  if (this.isPointOverVoid(targetX, targetZ)) {
                     this.strafeDirection *= -1;
                     nextIndex = wrapPointIndex(nearestIndex + this.strafeDirection, orbitPoints.size());
                     nextPoint = orbitPoints.get(nextIndex);
                     targetX = this.strafeTarget.posX + nextPoint.x();
                     targetZ = this.strafeTarget.posZ + nextPoint.z();
                  }

                  this.strafeYaw = RotationUtils.rotationsFromDelta(
                     targetX - minecraft.thePlayer.posX, 0.0, targetZ - minecraft.thePlayer.posZ
                  )[0];
                  event.setMovementYaw(this.strafeYaw, 10);
               }
            }
         }
      }
   }

   @EventHandler
   public void onMoveFlying(MoveFlyingEvent event) {
      if (this.isEnabled() && !Float.isNaN(this.strafeYaw) && MovementUtils.hasMovementInput()) {
         event.setStrafe(0.0F);
         event.setForward(1.0F);
      }
   }

   @EventHandler
   public void onRenderWorld(RenderWorldEvent event) {
      if (this.isEnabled() && EntityUtils.isLoaded(this.strafeTarget) && this.showTarget.getValue() != 0) {
         Color color = this.targetColor(this.strafeTarget);
         RenderUtils.beginOverlay();
         RenderUtils.drawEntityCircle(
            this.strafeTarget, (double)this.radius.getValue().floatValue(), this.points.getValue(), 3.0F, ColorUtils.scaleBrightness(color, 0.2F).getRGB()
         );
         RenderUtils.drawEntityCircle(this.strafeTarget, (double)this.radius.getValue().floatValue(), this.points.getValue(), 1.5F, color.getRGB());
         RenderUtils.endOverlay();
      }
   }

   @Override
   public void onDisable() {
      this.strafeTarget = null;
      this.strafeYaw = Float.NaN;
   }
}
