package myau;

import java.util.Objects;

class DroppedItemGroup {
   public final int itemId;
   public final double x;
   public final double y;
   public final double z;
   private final int hashCode;

   public DroppedItemGroup(int itemId, double x, double y, double z) {
      this.itemId = itemId;
      this.x = x;
      this.y = y;
      this.z = z;
      this.hashCode = Objects.hash(itemId, (int)x, (int)y, (int)z);
   }

   @Override
   public boolean equals(Object other) {
      if (this == other) {
         return true;
      } else if (other != null && this.getClass() == other.getClass()) {
         DroppedItemGroup group = (DroppedItemGroup)other;
         return this.itemId == group.itemId && (int)this.x == (int)group.x && (int)this.y == (int)group.y && (int)this.z == (int)group.z;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.hashCode;
   }
}
