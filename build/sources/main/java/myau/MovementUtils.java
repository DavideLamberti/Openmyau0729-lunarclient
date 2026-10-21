package myau;

import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovementInput;

public final class MovementUtils {
   private static final Minecraft minecraft = Minecraft.getMinecraft();

   private MovementUtils() {
   }

   public static boolean hasMovementInput() {
      return minecraft.gameSettings.keyBindForward.isKeyDown() != minecraft.gameSettings.keyBindBack.isKeyDown()
         || minecraft.gameSettings.keyBindLeft.isKeyDown() != minecraft.gameSettings.keyBindRight.isKeyDown();
   }

   public static int forwardInput() {
      int input = 0;
      if (minecraft.gameSettings.keyBindForward.isKeyDown()) {
         input++;
      }

      if (minecraft.gameSettings.keyBindBack.isKeyDown()) {
         input--;
      }

      return input;
   }

   public static int strafeInput() {
      int input = 0;
      if (minecraft.gameSettings.keyBindLeft.isKeyDown()) {
         input++;
      }

      if (minecraft.gameSettings.keyBindRight.isKeyDown()) {
         input--;
      }

      return input;
   }

   public static float movementYaw() {
      float baseYaw = RotationState.isActive() ? RotationState.movementYaw() : minecraft.thePlayer.rotationYaw;
      return movementYawForInput(baseYaw, minecraft.thePlayer.movementInput.moveForward, minecraft.thePlayer.movementInput.moveStrafe);
   }

   public static float movementYawForInput(float yaw, float forward, float strafe) {
      TargetStrafeModule targetStrafe = (TargetStrafeModule)MyauClient.moduleManager.modules.get(TargetStrafeModule.class);
      if (targetStrafe != null && targetStrafe.isEnabled()) {
         float strafeYaw = targetStrafe.getStrafeYaw();
         if (!Float.isNaN(strafeYaw)) {
            return strafeYaw;
         }
      }

      if (forward < 0.0F) {
         yaw += 180.0F;
      }

      if (strafe != 0.0F) {
         float forwardFactor = forward == 0.0F ? 1.0F : 0.5F * Math.signum(forward);
         yaw += -90.0F * forwardFactor * Math.signum(strafe);
      }

      return MathHelper.wrapAngleTo180_float(yaw);
   }

   public static float motionYaw() {
      return horizontalSpeed() == 0.0
         ? MathHelper.wrapAngleTo180_float(minecraft.thePlayer.rotationYaw)
         : MathHelper.wrapAngleTo180_float((float)Math.toDegrees(Math.atan2(minecraft.thePlayer.motionZ, minecraft.thePlayer.motionX)) - 90.0F);
   }

   public static double baseMovementSpeed() {
      double speed = 0.28015;
      if (speedPotionTicks() > 0) {
         speed *= 1.0 + 0.15 * (double)speedPotionLevel();
      }

      return speed;
   }

   public static double boostedSpeedForPotionLevel(int potionLevel) {
      double speed = 0.452;
      if (potionLevel == 1) {
         return speed * 1.1;
      } else {
         if (potionLevel >= 2) {
            speed *= 1.2;
         }

         return speed;
      }
   }

   public static double boostedMovementSpeed() {
      int potionLevel = speedPotionTicks() > 0 ? speedPotionLevel() : 0;
      return boostedSpeedForPotionLevel(potionLevel);
   }

   public static double horizontalSpeed() {
      return horizontalMagnitude(minecraft.thePlayer.motionX, minecraft.thePlayer.motionZ);
   }

   public static double horizontalMagnitude(double motionX, double motionZ) {
      return Math.hypot(motionX, motionZ);
   }

   public static void setSpeedAlongMotion(double speed) {
      setHorizontalMotion(speed, motionYaw());
   }

   public static void setHorizontalMotion(double speed, float yaw) {
      minecraft.thePlayer.motionX = -Math.sin(Math.toRadians((double)yaw)) * speed;
      minecraft.thePlayer.motionZ = Math.cos(Math.toRadians((double)yaw)) * speed;
   }

   public static void addHorizontalMotion(double speed, float yaw) {
      minecraft.thePlayer.motionX = minecraft.thePlayer.motionX + -Math.sin(Math.toRadians((double)yaw)) * speed;
      minecraft.thePlayer.motionZ = minecraft.thePlayer.motionZ + Math.cos(Math.toRadians((double)yaw)) * speed;
   }

   public static int speedPotionLevel() {
      return !minecraft.thePlayer.isPotionActive(Potion.moveSpeed) ? 0 : minecraft.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1;
   }

   public static int speedPotionTicks() {
      return !minecraft.thePlayer.isPotionActive(Potion.moveSpeed) ? 0 : minecraft.thePlayer.getActivePotionEffect(Potion.moveSpeed).getDuration();
   }

   public static float groundAcceleration() {
      float friction = minecraft.thePlayer
            .worldObj
            .getBlockState(
               new BlockPos(
                  MathHelper.floor_double(minecraft.thePlayer.posX),
                  MathHelper.floor_double(minecraft.thePlayer.getEntityBoundingBox().minY) - 1,
                  MathHelper.floor_double(minecraft.thePlayer.posZ)
               )
            )
            .getBlock()
            .slipperiness
         * 0.91F;
      return minecraft.thePlayer.getAIMoveSpeed() * (0.16277136F / (friction * friction * friction));
   }

   public static double[] inputAcceleration() {
      float strafe = (float)strafeInput() * 0.98F;
      float forward = (float)forwardInput() * 0.98F;
      float magnitudeSquared = strafe * strafe + forward * forward;
      if (magnitudeSquared < 1.0E-4F) {
         return new double[]{0.0, 0.0};
      } else {
         float magnitude = MathHelper.sqrt_float(magnitudeSquared);
         if (magnitude < 1.0F) {
            magnitude = 1.0F;
         }

         float acceleration = groundAcceleration() / magnitude;
         strafe *= acceleration;
         forward *= acceleration;
         float sinYaw = MathHelper.sin(minecraft.thePlayer.rotationYaw * (float) Math.PI / 180.0F);
         float cosYaw = MathHelper.cos(minecraft.thePlayer.rotationYaw * (float) Math.PI / 180.0F);
         return new double[]{(double)(strafe * cosYaw - forward * sinYaw), (double)(forward * cosYaw + strafe * sinYaw)};
      }
   }

   public static void correctMovementForYaw(float yaw) {
      float relativeYaw = MathHelper.wrapAngleTo180_float(
         movementYawForInput(minecraft.thePlayer.rotationYaw, (float)forwardInput(), (float)strafeInput()) - yaw + 22.5F
      );
      int direction = (int)(relativeYaw + 180.0F) / 45 % 8;
      switch (direction) {
         case 0:
            setMovementInput(-1.0F, 0.0F);
            break;
         case 1:
            setMovementInput(-1.0F, 1.0F);
            break;
         case 2:
            setMovementInput(0.0F, 1.0F);
            break;
         case 3:
            setMovementInput(1.0F, 1.0F);
            break;
         case 4:
            setMovementInput(1.0F, 0.0F);
            break;
         case 5:
            setMovementInput(1.0F, -1.0F);
            break;
         case 6:
            setMovementInput(0.0F, -1.0F);
            break;
         case 7:
            setMovementInput(-1.0F, -1.0F);
      }

      if (minecraft.thePlayer.movementInput.sneak) {
         minecraft.thePlayer.movementInput.moveForward *= 0.3F;
         minecraft.thePlayer.movementInput.moveStrafe *= 0.3F;
      }
   }

   private static void setMovementInput(float forward, float strafe) {
      MovementInput input = minecraft.thePlayer.movementInput;
      input.moveForward = forward;
      input.moveStrafe = strafe;
   }
}
