package myau;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;

public class EagleModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private int delayTicksRemaining = 0;
   public final IntegerSetting minDelay = new IntegerSetting("min-delay", 2, 0, 10);
   public final IntegerSetting maxDelay = new IntegerSetting("max-delay", 3, 0, 10);
   public final PercentSetting offset = new PercentSetting("offset", 100);
   public final BooleanSetting align = new BooleanSetting("align", true);
   public final BooleanSetting directionCheck = new BooleanSetting("direction-check", true);
   public final BooleanSetting pitchCheck = new BooleanSetting("pitch-check", true);
   public final BooleanSetting blocksOnly = new BooleanSetting("blocks-only", true);

   public EagleModule() {
      super("Eagle", false, 0);
   }

   private boolean isNearEdge() {
      double[] movement = MovementUtils.inputAcceleration();
      return PlayerUtils.isNarrowOffsetCollisionFree(
         minecraft.thePlayer.motionX + movement[0],
         -1.0,
         minecraft.thePlayer.motionZ + movement[1],
         (float)this.offset.getValue().intValue() / 100.0F
      );
   }

   private boolean canActivate() {
      if (this.directionCheck.getValue() && minecraft.gameSettings.keyBindForward.isKeyDown()) {
         return false;
      } else if (this.pitchCheck.getValue() && minecraft.thePlayer.rotationPitch < 69.0F) {
         return false;
      } else {
         return this.blocksOnly.getValue() && !ItemUtils.isHoldingPlaceableBlock() ? false : minecraft.thePlayer.onGround;
      }
   }

   @EventHandler
   public void onPreTick(PreTickEvent event) {
      if (this.isEnabled()) {
         if (this.delayTicksRemaining > 0) {
            this.delayTicksRemaining--;
         }

         if (this.delayTicksRemaining == 0) {
            if (this.isNearEdge()) {
               this.delayTicksRemaining = org.apache.commons.lang3.RandomUtils.nextInt(this.minDelay.getValue(), this.maxDelay.getValue() + 1);
            }
         }
      }
   }

   @EventHandler
   public void onRightClick(RightClickEvent event) {
      if (this.isEnabled()
         && minecraft.currentScreen == null
         && this.align.getValue()
         && !minecraft.gameSettings.keyBindSneak.isKeyDown()
         && this.canActivate()) {
         if (this.delayTicksRemaining > 0 || this.isNearEdge()) {
            float movementYaw = MathUtils.toRadians(
               MovementUtils.movementYawForInput(minecraft.thePlayer.rotationYaw, (float)MovementUtils.forwardInput(), (float)MovementUtils.strafeInput())
                  - 180.0F
            );
            float xDirection = -MathHelper.sin(movementYaw);
            float zDirection = MathHelper.cos(movementYaw);
            if (!(Math.abs(xDirection) < 0.1F) && !(Math.abs(zDirection) < 0.1F)) {
               AxisAlignedBB bounds = minecraft.thePlayer.getEntityBoundingBox();
               double xEdge = xDirection >= 0.0F ? bounds.maxX : bounds.minX;
               double zEdge = zDirection >= 0.0F ? bounds.maxZ : bounds.minZ;
               double xAlignment = Math.abs(xEdge - Math.rint(xEdge));
               double zAlignment = Math.abs(zEdge - Math.rint(zEdge));
               if (Math.abs(xAlignment - zAlignment) > 0.125) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = -100
   )
   public void onMovementInput(MovementInputEvent event) {
      if (this.isEnabled() && minecraft.currentScreen == null && !minecraft.thePlayer.movementInput.sneak && this.canActivate()) {
         if (this.delayTicksRemaining > 0 || this.isNearEdge()) {
            minecraft.thePlayer.movementInput.sneak = true;
            minecraft.thePlayer.movementInput.moveStrafe *= 0.3F;
            minecraft.thePlayer.movementInput.moveForward *= 0.3F;
         }
      }
   }

   @Override
   public void onDisable() {
      this.delayTicksRemaining = 0;
   }

   @Override
   public void onSettingChanged(String settingName) {
      if ("min-delay".equals(settingName)) {
         if (this.minDelay.getValue() > this.maxDelay.getValue()) {
            this.maxDelay.setValue(this.minDelay.getValue());
         }
      } else if ("max-delay".equals(settingName) && this.minDelay.getValue() > this.maxDelay.getValue()) {
         this.minDelay.setValue(this.maxDelay.getValue());
      }
   }

   @Override
   public String[] getHudInfo() {
      return Objects.equals(this.minDelay.getValue(), this.maxDelay.getValue())
         ? new String[]{this.minDelay.getValue().toString()}
         : new String[]{String.format("%d-%d", this.minDelay.getValue(), this.maxDelay.getValue())};
   }
}
