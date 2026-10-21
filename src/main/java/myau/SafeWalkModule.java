package myau;

import net.minecraft.client.Minecraft;

public class SafeWalkModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   public final FloatSetting motion = new FloatSetting("motion", 1.0F, 0.5F, 1.0F);
   public final FloatSetting speedMotion = new FloatSetting("speed-motion", 1.0F, 0.5F, 1.5F);
   public final BooleanSetting air = new BooleanSetting("air", false);
   public final BooleanSetting directionCheck = new BooleanSetting("direction-check", true);
   public final BooleanSetting pitchCheck = new BooleanSetting("pitch-check", true);
   public final BooleanSetting requirePress = new BooleanSetting("require-press", false);
   public final BooleanSetting blocksOnly = new BooleanSetting("blocks-only", true);

   public SafeWalkModule() {
      super("SafeWalk", false, 0);
   }

   private boolean shouldApplySafeWalk() {
      ScaffoldModule conflictingModule = (ScaffoldModule)MyauClient.moduleManager.modules.get(ScaffoldModule.class);
      boolean otherEnabled = conflictingModule.isEnabled();
      if (otherEnabled) {
         return false;
      } else if (this.directionCheck.getValue() && minecraft.gameSettings.keyBindForward.isKeyDown()) {
         return false;
      } else if (this.pitchCheck.getValue() && minecraft.thePlayer.rotationPitch < 69.0F) {
         return false;
      } else if (this.blocksOnly.getValue() && !ItemUtils.isHoldingPlaceableBlock()) {
         return false;
      } else if (this.requirePress.getValue() && !minecraft.gameSettings.keyBindUseItem.isKeyDown()) {
         return false;
      } else {
         return minecraft.thePlayer.onGround
               && PlayerUtils.isOffsetCollisionFree(minecraft.thePlayer.motionX, -1.0, minecraft.thePlayer.motionZ)
            ? true
            : this.air.getValue() && PlayerUtils.isOffsetCollisionFree(minecraft.thePlayer.motionX, -2.0, minecraft.thePlayer.motionZ);
      }
   }

   @EventHandler
   public void onSafeWalk(SafeWalkEvent event) {
      if (this.isEnabled() && this.shouldApplySafeWalk()) {
         event.setSafeWalk(true);
      }
   }

   @EventHandler
   public void onRotationUpdate(RotationUpdateEvent event) {
      if (this.isEnabled()) {
         if (minecraft.thePlayer.onGround && MovementUtils.hasMovementInput() && this.shouldApplySafeWalk()) {
            if (MovementUtils.speedPotionLevel() <= 0) {
               if (this.motion.getValue() != 1.0F) {
                  MovementUtils.setSpeedAlongMotion(MovementUtils.horizontalSpeed() * (double)this.motion.getValue().floatValue());
               }
            } else if (this.speedMotion.getValue() != 1.0F) {
               MovementUtils.setSpeedAlongMotion(MovementUtils.horizontalSpeed() * (double)this.speedMotion.getValue().floatValue());
            }
         }
      }
   }
}
