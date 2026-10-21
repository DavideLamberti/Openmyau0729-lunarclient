package myau;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.concurrent.CopyOnWriteArraySet;
import myau.mixin.AccessorMinecraft;
import net.minecraft.block.Block;
import net.minecraft.block.BlockMobSpawner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S22PacketMultiBlockChange.BlockUpdateData;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraftforge.common.ForgeModContainer;

public class XrayModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private static final LinkedHashSet<Integer> supportedBlocks = new LinkedHashSet<>(Arrays.asList(56, 14, 15, 16, 73, 74, 21, 129, 52, 83, 115));
   private static final LinkedHashSet<Vec3i> adjacentOffsets = new LinkedHashSet<>(
      Arrays.asList(new Vec3i(0, -1, 0), new Vec3i(1, 0, 0), new Vec3i(0, 0, -1), new Vec3i(0, 0, 1), new Vec3i(-1, 0, 0), new Vec3i(0, 1, 0))
   );
   private static final LinkedHashSet<Vec3i> radiusTwoOffsets = new LinkedHashSet<>(
      Arrays.asList(
         new Vec3i(0, -2, 0),
         new Vec3i(1, -1, 0),
         new Vec3i(0, -1, -1),
         new Vec3i(0, -1, 0),
         new Vec3i(0, -1, 1),
         new Vec3i(-1, -1, 0),
         new Vec3i(2, 0, 0),
         new Vec3i(0, 0, 2),
         new Vec3i(0, 0, -2),
         new Vec3i(-2, 0, 0),
         new Vec3i(1, 0, -1),
         new Vec3i(1, 0, 0),
         new Vec3i(1, 0, 1),
         new Vec3i(0, 0, -1),
         new Vec3i(0, 0, 1),
         new Vec3i(-1, 0, -1),
         new Vec3i(-1, 0, 0),
         new Vec3i(-1, 0, 1),
         new Vec3i(1, 1, 0),
         new Vec3i(0, 1, -1),
         new Vec3i(0, 1, 0),
         new Vec3i(0, 1, 1),
         new Vec3i(-1, 1, 0),
         new Vec3i(0, 2, 0)
      )
   );
   public final CopyOnWriteArraySet<BlockPos> discoveredBlocks = new CopyOnWriteArraySet<>();
   public final CopyOnWriteArraySet<BlockPos> updatedBlocks = new CopyOnWriteArraySet<>();
   public final ModeSetting mode = new ModeSetting("mode", 0, new String[]{"SOFT", "FULL"});
   public final PercentSetting opacity = new PercentSetting("opacity", 50);
   public final IntegerSetting range = new IntegerSetting("range", 64, 16, 512);
   public final BooleanSetting cavesOnly = new BooleanSetting("caves-only", true);
   public final IntegerSetting cavesRadius = new IntegerSetting("caves-radius", 2, 1, 2);
   public final BooleanSetting diamonds = new BooleanSetting("diamonds", true);
   public final BooleanSetting diamondsTracers = new BooleanSetting("diamonds-tracers", true, this.diamonds::getValue);
   public final BooleanSetting gold = new BooleanSetting("gold", true);
   public final BooleanSetting goldTracers = new BooleanSetting("gold-tracers", true, this.gold::getValue);
   public final BooleanSetting iron = new BooleanSetting("iron", false);
   public final BooleanSetting ironTracers = new BooleanSetting("iron-tracers", false, this.iron::getValue);
   public final BooleanSetting coal = new BooleanSetting("coal", false);
   public final BooleanSetting coalTracers = new BooleanSetting("coal-tracers", false, this.coal::getValue);
   public final BooleanSetting redstone = new BooleanSetting("redstone", false);
   public final BooleanSetting redstoneTracers = new BooleanSetting("redstone-tracers", false, this.redstone::getValue);
   public final BooleanSetting lapis = new BooleanSetting("lapis", false);
   public final BooleanSetting lapisTracers = new BooleanSetting("lapis-tracers", false, this.lapis::getValue);
   public final BooleanSetting emeralds = new BooleanSetting("emeralds", false);
   public final BooleanSetting emeraldsTracers = new BooleanSetting("emeralds-tracers", false, this.emeralds::getValue);
   public final BooleanSetting spawners = new BooleanSetting("spawners", false);
   public final BooleanSetting spawnersTracers = new BooleanSetting("spawners-tracers", false, this.spawners::getValue);
   public final BooleanSetting canes = new BooleanSetting("canes", false);
   public final BooleanSetting canesTracers = new BooleanSetting("canes-tracers", false, this.canes::getValue);
   public final BooleanSetting warts = new BooleanSetting("warts", false);
   public final BooleanSetting wartsTracers = new BooleanSetting("warts-tracers", false, this.warts::getValue);

   public XrayModule() {
      super("Xray", false, 0);
   }

   public boolean isSupportedBlock(int blockId) {
      return supportedBlocks.contains(Integer.valueOf(blockId));
   }

   public boolean isBlockEnabled(int blockId) {
      switch (blockId) {
         case 14:
            return this.gold.getValue();
         case 15:
            return this.iron.getValue();
         case 16:
            return this.coal.getValue();
         case 21:
            return this.lapis.getValue();
         case 52:
            return this.spawners.getValue();
         case 56:
            return this.diamonds.getValue();
         case 73:
         case 74:
            return this.redstone.getValue();
         case 83:
            return this.canes.getValue();
         case 115:
            return this.warts.getValue();
         case 129:
            return this.emeralds.getValue();
         default:
            return false;
      }
   }

   private boolean shouldDrawTracer(int blockId) {
      if (!this.isBlockEnabled(blockId)) {
         return false;
      } else {
         switch (blockId) {
            case 14:
               return this.goldTracers.getValue();
            case 15:
               return this.ironTracers.getValue();
            case 16:
               return this.coalTracers.getValue();
            case 21:
               return this.lapisTracers.getValue();
            case 52:
               return this.spawnersTracers.getValue();
            case 56:
               return this.diamondsTracers.getValue();
            case 73:
            case 74:
               return this.redstoneTracers.getValue();
            case 83:
               return this.canesTracers.getValue();
            case 115:
               return this.wartsTracers.getValue();
            case 129:
               return this.emeraldsTracers.getValue();
            default:
               return false;
         }
      }
   }

   private Color colorFor(int blockId) {
      switch (blockId) {
         case 14:
            return new Color(16777045);
         case 15:
            return new Color(16777215);
         case 16:
            return new Color(0);
         case 21:
            return new Color(5592575);
         case 52:
            return new Color(16733695);
         case 56:
            return new Color(5636095);
         case 73:
         case 74:
            return new Color(16733525);
         case 83:
            return new Color(11206570);
         case 115:
            return new Color(11141120);
         case 129:
            return new Color(5635925);
         default:
            return new Color(-1);
      }
   }

   private void drawBlock(BlockPos position, int blockId, Vec3 tracerStart) {
      if (minecraft.thePlayer.getDistance((double)position.getX(), (double)position.getY(), (double)position.getZ())
         <= this.range.getValue().doubleValue()) {
         Color tint = this.colorFor(blockId);
         RenderUtils.outlineBlockBounds(position, 1.0, tint.getRed(), tint.getGreen(), tint.getBlue(), tint.getAlpha(), 1.5F);
         if (this.shouldDrawTracer(blockId)) {
            RenderUtils.drawTracer(
               tracerStart,
               (double)position.getX() + 0.5,
               (double)position.getY() + 0.5,
               (double)position.getZ() + 0.5,
               (float)tint.getRed() / 255.0F,
               (float)tint.getGreen() / 255.0F,
               (float)tint.getBlue() / 255.0F,
               1.0F,
               1.5F
            );
         }
      }
   }

   private boolean isCaveOpening(BlockPos position) {
      if (!minecraft.theWorld.isBlockLoaded(position, false)) {
         return false;
      } else {
         Block block = minecraft.theWorld.getBlockState(position).getBlock();
         return block instanceof BlockMobSpawner || !block.isFullBlock() || !block.getMaterial().isOpaque() || block.canProvidePower();
      }
   }

   public boolean passesCaveCheck(BlockPos position) {
      if (!this.cavesOnly.getValue()) {
         return true;
      } else {
         for (Vec3i offset : this.cavesRadius.getValue() >= 2 ? radiusTwoOffsets : adjacentOffsets) {
            if (this.isCaveOpening(position.add(offset))) {
               return true;
            }
         }

         return false;
      }
   }

   @EventHandler
   public void onRenderWorld(RenderWorldEvent event) {
      if (this.isEnabled()) {
         float partialTicks = ((AccessorMinecraft)minecraft).getTimer().renderPartialTicks;
         Vec3 direction;
         if (minecraft.gameSettings.thirdPersonView == 0) {
            direction = new Vec3(0.0, 0.0, 1.0)
               .rotatePitch(
                  (float)(
                     -Math.toRadians(
                        (double)MathUtils.lerpFloat(minecraft.getRenderViewEntity().prevRotationPitch, minecraft.getRenderViewEntity().rotationPitch, partialTicks)
                     )
                  )
               )
               .rotateYaw(
                  (float)(
                     -Math.toRadians(
                        (double)MathUtils.lerpFloat(minecraft.getRenderViewEntity().prevRotationYaw, minecraft.getRenderViewEntity().rotationYaw, partialTicks)
                     )
                  )
               );
         } else {
            direction = new Vec3(0.0, 0.0, 0.0)
               .rotatePitch(
                  (float)(
                     -Math.toRadians((double)MathUtils.lerpFloat(minecraft.thePlayer.prevCameraPitch, minecraft.thePlayer.cameraPitch, partialTicks))
                  )
               )
               .rotateYaw(
                  (float)(
                     -Math.toRadians((double)MathUtils.lerpFloat(minecraft.thePlayer.prevCameraYaw, minecraft.thePlayer.cameraYaw, partialTicks))
                  )
               );
         }

         Vec3 tracerStart = new Vec3(
            direction.xCoord, direction.yCoord + (double)minecraft.getRenderViewEntity().getEyeHeight(), direction.zCoord
         );
         RenderUtils.beginOverlay();

         for (BlockPos position : this.discoveredBlocks) {
            if (this.updatedBlocks.contains(position)) {
               this.discoveredBlocks.remove(position);
            } else {
               this.drawTrackedBlock(position, tracerStart, this.discoveredBlocks);
            }
         }

         for (BlockPos positionx : this.updatedBlocks) {
            this.drawTrackedBlock(positionx, tracerStart, this.updatedBlocks);
         }

         RenderUtils.endOverlay();
      }
   }

   private void drawTrackedBlock(BlockPos position, Vec3 tracerStart, CopyOnWriteArraySet<BlockPos> source) {
      int blockId = Block.getIdFromBlock(minecraft.theWorld.getBlockState(position).getBlock());
      if (this.isBlockEnabled(blockId)) {
         this.drawBlock(position, blockId, tracerStart);
      } else {
         source.remove(position);
      }
   }

   @EventHandler
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacket() instanceof S22PacketMultiBlockChange) {
         S22PacketMultiBlockChange packet = (S22PacketMultiBlockChange)event.getPacket();

         for (BlockUpdateData update : packet.getChangedBlocks()) {
            int blockId = Block.getIdFromBlock(update.getBlockState().getBlock());
            if (this.isBlockEnabled(blockId)) {
               this.updatedBlocks.add(new BlockPos(update.getPos()));
            }
         }
      } else {
         if (event.getPacket() instanceof S23PacketBlockChange) {
            S23PacketBlockChange packet = (S23PacketBlockChange)event.getPacket();
            int blockId = Block.getIdFromBlock(packet.getBlockState().getBlock());
            if (this.isBlockEnabled(blockId)) {
               this.updatedBlocks.add(new BlockPos(packet.getBlockPosition()));
            }
         }
      }
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      this.discoveredBlocks.clear();
      this.updatedBlocks.clear();
   }

   @Override
   public void onEnable() {
      ForgeModContainer.forgeLightPipelineEnabled = false;
      RenderGlobal renderer = minecraft.renderGlobal;
      if (renderer != null) {
         renderer.loadRenderers();
      }
   }

   @Override
   public void onDisable() {
      ForgeModContainer.forgeLightPipelineEnabled = true;
      RenderGlobal renderer = minecraft.renderGlobal;
      if (renderer != null) {
         renderer.loadRenderers();
      }
   }

   @Override
   public void onSettingChanged(String name) {
      this.discoveredBlocks.clear();
      this.updatedBlocks.clear();
      if (this.isEnabled()) {
         RenderGlobal renderer = minecraft.renderGlobal;
         if (minecraft.renderGlobal == null) {
            return;
         }

         renderer.loadRenderers();
      }
   }
}
