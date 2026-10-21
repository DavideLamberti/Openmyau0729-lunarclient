package myau;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

class BlockPlacementTarget {
   private final BlockPos position;
   private final EnumFacing face;

   public BlockPlacementTarget(BlockPos position, EnumFacing face) {
      this.position = position;
      this.face = face;
   }

   public BlockPos position() {
      return this.position;
   }

   public EnumFacing face() {
      return this.face;
   }
}
