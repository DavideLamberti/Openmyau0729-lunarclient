package myau;

import net.minecraft.util.MathHelper;

public class RotationUpdateEvent extends Event {
   private final float previousYaw;
   private final float previousPitch;
   private float yaw;
   private float pitch;
   private float movementYaw;
   private int rotationPriority = -1;
   private int movementPriority = -1;
   private boolean modified;

   public RotationUpdateEvent(float previousYaw, float previousPitch, float yaw, float pitch) {
      this.previousYaw = previousYaw;
      this.previousPitch = previousPitch;
      this.yaw = yaw;
      this.pitch = pitch;
      this.movementYaw = yaw;
   }

   public float getPreviousYaw() {
      return this.previousYaw;
   }

   public float getPreviousPitch() {
      return this.previousPitch;
   }

   public float getYaw() {
      return this.yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public float getMovementYaw() {
      return this.movementYaw;
   }

   public int getMovementPriority() {
      return this.movementPriority;
   }

   public boolean isModified() {
      return this.modified;
   }

   public void setRotation(float yaw, float pitch, int priority) {
      if (this.rotationPriority <= priority) {
         this.yaw = yaw;
         this.pitch = MathHelper.clamp_float(pitch, -90.0F, 90.0F);
         this.rotationPriority = priority;
         this.modified = true;
      }
   }

   public void setMovementYaw(float movementYaw, int priority) {
      if (this.movementPriority < priority) {
         this.movementYaw = movementYaw;
         this.movementPriority = priority;
         this.modified = true;
      }
   }
}
