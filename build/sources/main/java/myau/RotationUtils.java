package myau;

import myau.mixin.AccessorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class RotationUtils {
   private static final Minecraft minecraft = Minecraft.getMinecraft();

   public static float nearestEquivalentAngle(float targetAngle, float referenceAngle) {
      return referenceAngle + MathHelper.wrapAngleTo180_float(targetAngle - referenceAngle);
   }

   public static float clampAngleChange(float change, float maximumChange) {
      maximumChange = MathHelper.clamp_float(maximumChange, 0.0F, 180.0F);
      if (change > maximumChange) {
         return maximumChange;
      } else {
         if (change < -maximumChange) {
            change = -maximumChange;
         }

         return change;
      }
   }

   public static float scaleAngleChange(float change, float factor) {
      return change * (0.5F + 0.5F * (1.0F - MathHelper.clamp_float(factor, 0.0F, 1.0F)));
   }

   public static float snapToMouseSensitivity(float targetAngle, float referenceAngle) {
      float change = MathHelper.wrapAngleTo180_float(targetAngle - referenceAngle);
      float sensitivity = minecraft.gameSettings.mouseSensitivity * 0.6F + 0.2F;
      float increment = sensitivity * sensitivity * sensitivity * 8.0F;
      double roundedChange = (double)((float)Math.round((double)change / ((double)increment * 0.15)) * increment) * 0.15;
      return (float)((double)referenceAngle + roundedChange);
   }

   public static float[] aimAtBounds(AxisAlignedBB bounds) {
      Vec3 eyePosition = minecraft.thePlayer.getPositionEyes(1.0F);
      Vec3 center = boundsCenter(bounds);
      Vec3 closestPoint = closestPoint(eyePosition, bounds);
      double deltaX = MathUtils.lerpDouble(center.xCoord, closestPoint.xCoord, 0.875) - eyePosition.xCoord;
      double deltaY = closestPoint.yCoord - eyePosition.yCoord;
      double deltaZ = MathUtils.lerpDouble(center.zCoord, closestPoint.zCoord, 0.875) - eyePosition.zCoord;
      float[] rotation = rotationsFromDelta(deltaX, deltaY, deltaZ);
      rotation[1] += 10.0F
         * MathHelper.clamp_float((float)((eyePosition.yCoord - center.yCoord) / (center.yCoord - bounds.minY)), -0.5F, 0.5F);
      return rotation;
   }

   public static float[] rotationsFromDelta(double deltaX, double deltaY, double deltaZ) {
      double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
      float yaw = (float)(Math.atan2(deltaZ, deltaX) * 180.0 / Math.PI) - 90.0F;
      float pitch = (float)(-Math.atan2(deltaY, horizontalDistance) * 180.0 / Math.PI);
      return new float[]{yaw, pitch};
   }

   public static Vec3 boundsCenter(AxisAlignedBB bounds) {
      return new Vec3(
         (bounds.minX + bounds.maxX) / 2.0,
         (bounds.minY + bounds.maxY) / 2.0,
         (bounds.minZ + bounds.maxZ) / 2.0
      );
   }

   public static Vec3 closestPoint(Vec3 position, AxisAlignedBB bounds) {
      double[] coordinates = new double[]{position.xCoord, position.yCoord, position.zCoord};
      double[] minimum = new double[]{bounds.minX, bounds.minY, bounds.minZ};
      double[] maximum = new double[]{bounds.maxX, bounds.maxY, bounds.maxZ};

      for (int axis = 0; axis < 3; axis++) {
         if (coordinates[axis] > maximum[axis]) {
            coordinates[axis] = maximum[axis];
         } else if (coordinates[axis] < minimum[axis]) {
            coordinates[axis] = minimum[axis];
         }
      }

      return new Vec3(coordinates[0], coordinates[1], coordinates[2]);
   }

   public static double distanceToEntity(Entity entity) {
      float collisionBorder = entity.getCollisionBorderSize();
      AxisAlignedBB bounds = entity.getEntityBoundingBox().expand((double)collisionBorder, (double)collisionBorder, (double)collisionBorder);
      return distanceToBounds(bounds);
   }

   public static double distanceToEntityFrom(Entity entity, Vec3 position) {
      float collisionBorder = entity.getCollisionBorderSize();
      AxisAlignedBB bounds = entity.getEntityBoundingBox().expand((double)collisionBorder, (double)collisionBorder, (double)collisionBorder);
      return distanceToBoundsFrom(bounds, position);
   }

   public static double distanceToBounds(AxisAlignedBB bounds) {
      return distanceToBoundsFrom(bounds, minecraft.thePlayer.getPositionEyes(1.0F));
   }

   public static double distanceToBoundsFrom(AxisAlignedBB bounds, Vec3 position) {
      if (bounds.isVecInside(position)) {
         return 0.0;
      } else {
         Vec3 closestPoint = closestPoint(position, bounds);
         double deltaX = closestPoint.xCoord - position.xCoord;
         double deltaY = closestPoint.yCoord - position.yCoord;
         double deltaZ = closestPoint.zCoord - position.zCoord;
         return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
      }
   }

   public static float entityAimError(Entity entity) {
      Vec3 eyePosition = minecraft.thePlayer.getPositionEyes(1.0F);
      float collisionBorder = entity.getCollisionBorderSize();
      AxisAlignedBB bounds = entity.getEntityBoundingBox().expand((double)collisionBorder, (double)collisionBorder, (double)collisionBorder);
      return boundsAimError(bounds, eyePosition);
   }

   public static float boundsAimError(AxisAlignedBB bounds, Vec3 position) {
      if (bounds.isVecInside(position)) {
         return 0.0F;
      } else {
         double traceDistance = 30.0
            + bounds.maxX
            - bounds.minX
            + bounds.maxZ
            - bounds.minZ
            + bounds.maxY
            - bounds.minY;
         MovingObjectPosition hit = intersectBoundsFrom(
            bounds, position, minecraft.thePlayer.rotationYaw, minecraft.thePlayer.rotationPitch, traceDistance
         );
         if (hit != null) {
            return 0.0F;
         } else {
            float[] rotation = aimAtBounds(bounds);
            float yawError = Math.abs(MathHelper.wrapAngleTo180_float(rotation[0] - minecraft.thePlayer.rotationYaw));
            float pitchError = Math.abs(rotation[1] - minecraft.thePlayer.rotationPitch);
            return Math.max(yawError * 2.0F, pitchError * 2.0F);
         }
      }
   }

   public static float horizontalYawError(double startX, double startZ, double endX, double endZ) {
      float yaw = (float)(Math.atan2(endZ - startZ, endX - startX) * 180.0 / Math.PI) - 90.0F;
      return MathHelper.wrapAngleTo180_float(yaw - minecraft.thePlayer.rotationYaw);
   }

   public static MovingObjectPosition traceBlocks(float yaw, float pitch, double distance, float partialTicks) {
      Vec3 eyePosition = minecraft.thePlayer.getPositionEyes(partialTicks);
      return traceBlocksFrom(eyePosition, yaw, pitch, distance);
   }

   public static MovingObjectPosition traceBlocksFrom(Vec3 position, float yaw, float pitch, double distance) {
      Vec3 direction = ((AccessorEntity)minecraft.thePlayer).callGetVectorForRotation(pitch, yaw);
      Vec3 end = position.addVector(direction.xCoord * distance, direction.yCoord * distance, direction.zCoord * distance);
      return minecraft.theWorld.rayTraceBlocks(position, end);
   }

   public static MovingObjectPosition traceToEntity(Entity entity) {
      Vec3 eyePosition = minecraft.thePlayer.getPositionEyes(1.0F);
      float collisionBorder = entity.getCollisionBorderSize();
      Vec3 closestPoint = closestPoint(
         eyePosition, entity.getEntityBoundingBox().expand((double)collisionBorder, (double)collisionBorder, (double)collisionBorder)
      );
      return minecraft.theWorld.rayTraceBlocks(eyePosition, closestPoint);
   }

   public static MovingObjectPosition intersectBounds(AxisAlignedBB bounds, float yaw, float pitch, double distance) {
      Vec3 eyePosition = minecraft.thePlayer.getPositionEyes(1.0F);
      return intersectBoundsFrom(bounds, eyePosition, yaw, pitch, distance);
   }

   public static MovingObjectPosition intersectBoundsFrom(AxisAlignedBB bounds, Vec3 position, float yaw, float pitch, double distance) {
      Vec3 direction = ((AccessorEntity)minecraft.thePlayer).callGetVectorForRotation(pitch, yaw);
      Vec3 end = position.addVector(direction.xCoord * distance, direction.yCoord * distance, direction.zCoord * distance);
      return bounds.calculateIntercept(position, end);
   }

   public static boolean isBoundsRayObstructed(AxisAlignedBB bounds, Vec3 position, float yaw, float pitch, double distance) {
      if (bounds.isVecInside(position)) {
         return false;
      } else {
         MovingObjectPosition boxHit = intersectBoundsFrom(bounds, position, yaw, pitch, distance);
         if (boxHit == null) {
            return true;
         } else {
            MovingObjectPosition blockHit = traceBlocksFrom(position, yaw, pitch, distance);
            return blockHit == null ? false : position.distanceTo(boxHit.hitVec) > position.distanceTo(blockHit.hitVec);
         }
      }
   }
}
