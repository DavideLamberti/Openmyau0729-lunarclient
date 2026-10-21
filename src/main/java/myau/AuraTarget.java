package myau;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;

class AuraTarget {
   public final EntityLivingBase entity;
   public final AxisAlignedBB bounds;
   public final double x;
   public final double y;
   public final double z;
   public final float yaw;
   public final float pitch;
   public final double distance;
   public final float angularDistance;
   public final boolean hasUsableAim;

   public AuraTarget(
      EntityLivingBase entity,
      AxisAlignedBB bounds,
      double x,
      double y,
      double z,
      float yaw,
      float pitch,
      double distance,
      float angularDistance,
      boolean hasUsableAim
   ) {
      this.entity = entity;
      this.bounds = bounds;
      this.x = x;
      this.y = y;
      this.z = z;
      this.yaw = yaw;
      this.pitch = pitch;
      this.distance = distance;
      this.angularDistance = angularDistance;
      this.hasUsableAim = hasUsableAim;
   }
}
