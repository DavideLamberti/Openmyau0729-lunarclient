package myau;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockEndPortal;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockNote;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlime;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockTripWire;
import net.minecraft.block.BlockTripWireHook;
import net.minecraft.block.BlockVine;
import net.minecraft.block.BlockWall;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

public class BlockUtils {
   private static final Minecraft minecraft = Minecraft.getMinecraft();

   private BlockUtils() {
   }

   public static boolean isReplaceableAt(BlockPos position) {
      return isReplaceable(minecraft.theWorld.getBlockState(position).getBlock());
   }

   public static boolean isReplaceable(Block block) {
      return !block.getMaterial().isReplaceable() ? false : !(block instanceof BlockSnow) || block.getBlockBoundsMaxY() <= 0.125;
   }

   public static boolean isInteractableAt(BlockPos position) {
      return isInteractable(minecraft.theWorld.getBlockState(position).getBlock());
   }

   public static boolean isInteractable(Block block) {
      return block instanceof BlockContainer
         || block instanceof BlockWorkbench
         || block instanceof BlockAnvil
         || block instanceof BlockBed
         || block instanceof BlockDoor && block.getMaterial() != Material.iron
         || block instanceof BlockTrapDoor
         || block instanceof BlockFenceGate
         || block instanceof BlockFence
         || block instanceof BlockButton
         || block instanceof BlockLever
         || block instanceof BlockJukebox
         || block instanceof BlockNote;
   }

   public static boolean isSuitableFullBlock(Block block) {
      return !(block instanceof BlockStairs)
         && !(block instanceof BlockSlab)
         && !(block instanceof BlockEndPortalFrame)
         && !(block instanceof BlockEndPortal)
         && !(block instanceof BlockVine)
         && !(block instanceof BlockPumpkin)
         && !(block instanceof BlockCactus)
         && !(block instanceof BlockBush)
         && !(block instanceof BlockFalling)
         && !(block instanceof BlockWeb)
         && !(block instanceof BlockPane)
         && !(block instanceof BlockCarpet)
         && !(block instanceof BlockSnow)
         && !(block instanceof BlockFence)
         && !(block instanceof BlockFenceGate)
         && !(block instanceof BlockWall)
         && !(block instanceof BlockLadder)
         && !(block instanceof BlockTorch)
         && !(block instanceof BlockRedstoneWire)
         && !(block instanceof BlockRedstoneDiode)
         && !(block instanceof BlockBasePressurePlate)
         && !(block instanceof BlockTripWire)
         && !(block instanceof BlockTripWireHook)
         && !(block instanceof BlockRailBase)
         && !(block instanceof BlockSlime)
         && !(block instanceof BlockTNT);
   }

   public static Vec3 placementHitVector(BlockPos position, EnumFacing face, float yaw, float pitch) {
      MovingObjectPosition rayHit = RotationUtils.traceBlocks(yaw, pitch, (double)minecraft.playerController.getBlockReachDistance(), 1.0F);
      return rayHit != null && rayHit.typeOfHit == MovingObjectType.BLOCK && rayHit.getBlockPos().equals(position) && rayHit.sideHit == face
         ? rayHit.hitVec
         : randomFacePoint(position, face);
   }

   public static Vec3 randomFacePoint(BlockPos position, EnumFacing face) {
      Block block = minecraft.theWorld.getBlockState(position).getBlock();
      Vec3 point = new Vec3(
         (double)position.getX() + Math.min(Math.max(RandomUtils.randomDouble(0.0, 1.0), block.getBlockBoundsMinX()), block.getBlockBoundsMaxX()),
         (double)position.getY() + Math.min(Math.max(RandomUtils.randomDouble(0.0, 1.0), block.getBlockBoundsMinY()), block.getBlockBoundsMaxY()),
         (double)position.getZ() + Math.min(Math.max(RandomUtils.randomDouble(0.0, 1.0), block.getBlockBoundsMinZ()), block.getBlockBoundsMaxZ())
      );
      switch (face) {
         case DOWN:
            return new Vec3(point.xCoord, (double)position.getY() + block.getBlockBoundsMinY(), point.zCoord);
         case UP:
            return new Vec3(point.xCoord, (double)position.getY() + block.getBlockBoundsMaxY(), point.zCoord);
         case NORTH:
            return new Vec3(point.xCoord, point.yCoord, (double)position.getZ() + block.getBlockBoundsMinZ());
         case EAST:
            return new Vec3((double)position.getX() + block.getBlockBoundsMaxX(), point.yCoord, point.zCoord);
         case SOUTH:
            return new Vec3(point.xCoord, point.yCoord, (double)position.getZ() + block.getBlockBoundsMaxZ());
         case WEST:
            return new Vec3((double)position.getX() + block.getBlockBoundsMinX(), point.yCoord, point.zCoord);
         default:
            return point;
      }
   }
}
