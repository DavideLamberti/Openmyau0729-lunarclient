package myau;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

public final class RotationState {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private static int ticksSinceActive = -1;
   private static float previousBodyYaw;
   private static float bodyYaw;
   private static float previousHeadYaw;
   private static float headYaw;
   private static float previousPitch;
   private static float pitch;
   private static float movementYaw;
   private static int movementPriority;

   private RotationState() {
   }

   private static float computeBodyYaw(float requestedHeadYaw, float currentBodyYaw) {
      float targetBodyYaw = currentBodyYaw;
      double deltaX = minecraft.thePlayer.posX - minecraft.thePlayer.prevPosX;
      double deltaZ = minecraft.thePlayer.posZ - minecraft.thePlayer.prevPosZ;
      if ((float)(deltaX * deltaX + deltaZ * deltaZ) > 0.0025000002F) {
         targetBodyYaw = (float)MathHelper.atan2(deltaZ, deltaX) * 180.0F / (float) Math.PI - 90.0F;
      }

      if (minecraft.thePlayer.swingProgress > 0.0F) {
         targetBodyYaw = requestedHeadYaw;
      }

      currentBodyYaw += MathHelper.wrapAngleTo180_float(targetBodyYaw - currentBodyYaw) * 0.3F;
      float headBodyDifference = MathHelper.wrapAngleTo180_float(requestedHeadYaw - currentBodyYaw);
      if (headBodyDifference < -75.0F) {
         headBodyDifference = -75.0F;
      }

      if (headBodyDifference >= 75.0F) {
         headBodyDifference = 75.0F;
      }

      float result = requestedHeadYaw - headBodyDifference;
      if (headBodyDifference * headBodyDifference > 2500.0F) {
         result += headBodyDifference * 0.2F;
      }

      return result;
   }

   public static void update(boolean active, float requestedYaw, float requestedPitch, float requestedMovementYaw, int requestedMovementPriority) {
      ticksSinceActive = active ? 0 : ticksSinceActive + 1;
      previousBodyYaw = bodyYaw;
      bodyYaw = active ? computeBodyYaw(requestedYaw, bodyYaw) : minecraft.thePlayer.renderYawOffset;
      previousHeadYaw = headYaw;
      headYaw = active ? requestedYaw : minecraft.thePlayer.rotationYawHead;
      previousPitch = pitch;
      pitch = active ? requestedPitch : minecraft.thePlayer.rotationPitch;
      movementYaw = requestedMovementYaw;
      movementPriority = requestedMovementPriority;
   }

   public static boolean isActive() {
      return isActiveWithinTicks(0);
   }

   public static boolean isActiveWithinTicks(int maximumInactiveTicks) {
      return ticksSinceActive >= 0 && ticksSinceActive <= maximumInactiveTicks;
   }

   public static float previousBodyYaw() {
      return previousBodyYaw;
   }

   public static float bodyYaw() {
      return bodyYaw;
   }

   public static float previousHeadYaw() {
      return previousHeadYaw;
   }

   public static float headYaw() {
      return headYaw;
   }

   public static float previousPitch() {
      return previousPitch;
   }

   public static float pitch() {
      return pitch;
   }

   public static float movementYaw() {
      return movementYaw;
   }

   public static float movementPriority() {
      return (float)movementPriority;
   }
}
