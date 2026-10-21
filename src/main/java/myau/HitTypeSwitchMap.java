package myau;

import net.minecraft.util.MovingObjectPosition.MovingObjectType;

class HitTypeSwitchMap {
   static final int[] n = new int[MovingObjectType.values().length];

   static {
      try {
         n[MovingObjectType.BLOCK.ordinal()] = 1;
      } catch (NoSuchFieldError var2) {
      }

      try {
         n[MovingObjectType.ENTITY.ordinal()] = 2;
      } catch (NoSuchFieldError var1) {
      }
   }
}
