package myau;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class AntiObbyTrapModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   public final BooleanSetting setAir = new BooleanSetting("set-air", true);

   public AntiObbyTrapModule() {
      super("AntiObbyTrap", false, 0);
   }

   public boolean shouldIgnoreBlock(World world, BlockPos position) {
      IBlockState state = world.getBlockState(position);
      Block block = state.getBlock();
      boolean solid = block.getMaterial().isSolid();
      if (solid && block.isFullBlock()) {
         Vec3 eyes = new Vec3(
            minecraft.thePlayer.posX,
            minecraft.thePlayer.posY + (double)minecraft.thePlayer.getEyeHeight(),
            minecraft.thePlayer.posZ
         );
         return block.getCollisionBoundingBox(minecraft.theWorld, position, state).isVecInside(eyes);
      } else {
         return false;
      }
   }
}
