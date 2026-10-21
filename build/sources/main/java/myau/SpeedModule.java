package myau;

import myau.mixin.AccessorEntity;
import net.minecraft.client.Minecraft;

public class SpeedModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   public final FloatSetting multiplier = new FloatSetting("multiplier", 1.0F, 0.0F, 10.0F);
   public final FloatSetting friction = new FloatSetting("friction", 1.0F, 0.0F, 10.0F);
   public final PercentSetting strafe = new PercentSetting("strafe", 0);

   public SpeedModule() {
      super("Speed", false, 0);
   }

   private boolean canApplySpeed() {
      ScaffoldModule conflictingModule = (ScaffoldModule)MyauClient.moduleManager.modules.get(ScaffoldModule.class);
      boolean otherEnabled = conflictingModule.isEnabled();
      if (!otherEnabled && MovementUtils.hasMovementInput()) {
         if (minecraft.thePlayer.getFoodStats().getFoodLevel() <= 6) {
            return false;
         } else {
            return !minecraft.thePlayer.isSneaking() && !minecraft.thePlayer.isInWater() && !minecraft.thePlayer.isInLava()
               ? !((AccessorEntity)minecraft.thePlayer).getIsInWeb()
               : false;
         }
      } else {
         return false;
      }
   }

   private void applyStrafe() {
      double speed = MovementUtils.horizontalSpeed();
      MovementUtils.setHorizontalMotion(speed * (double)((float)(100 - this.strafe.getValue()) / 100.0F), MovementUtils.motionYaw());
      MovementUtils.addHorizontalMotion(speed * (double)((float)this.strafe.getValue().intValue() / 100.0F), MovementUtils.movementYaw());
      MovementUtils.setSpeedAlongMotion(speed);
   }

   @EventHandler(
      priority = -50
   )
   public void onMoveFlying(MoveFlyingEvent event) {
      if (this.isEnabled()) {
         if (this.canApplySpeed()) {
            if (minecraft.thePlayer.onGround) {
               minecraft.thePlayer.motionY = 0.42F;
               MovementUtils.setHorizontalMotion(
                  MovementUtils.boostedMovementSpeed() * (double)this.multiplier.getValue().floatValue(), MovementUtils.movementYaw()
               );
            } else {
               if (this.friction.getValue() != 1.0F) {
                  event.setFriction(event.getFriction() * this.friction.getValue());
               }

               if (this.strafe.getValue() > 0) {
                  this.applyStrafe();
               }
            }
         }
      }
   }

   @EventHandler(
      priority = -50
   )
   public void onLivingUpdate(LivingUpdateEvent event) {
      if (this.isEnabled() && this.canApplySpeed()) {
         minecraft.thePlayer.movementInput.jump = false;
      }
   }
}
