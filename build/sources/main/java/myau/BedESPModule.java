package myau;

import java.awt.Color;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArraySet;
import myau.mixin.AccessorRenderManager;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockBed.EnumPartType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class BedESPModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   public final CopyOnWriteArraySet<BlockPos> bedHeads = new CopyOnWriteArraySet<>();
   public final ModeSetting mode = new ModeSetting("mode", 0, new String[]{"DEFAULT", "FULL"});
   public final ModeSetting color = new ModeSetting("color", 0, new String[]{"CUSTOM", "HUD"});
   public final ColorSetting customColor = new ColorSetting("custom-color", 16733525, this::showCustomColor);
   public final PercentSetting opacity = new PercentSetting("opacity", 25);
   public final BooleanSetting outline = new BooleanSetting("outline", false);
   public final BooleanSetting obsidian = new BooleanSetting("obsidian", true);

   public BedESPModule() {
      super("BedESP", false, 0);
   }

   public double getBedHeight() {
      return this.mode.getValue() == 1 ? 1.0 : 0.5625;
   }

   private Color bedColor() {
      switch (this.color.getValue()) {
         case 0:
            return new Color(this.customColor.getValue());
         case 1:
            return ((HUDModule)MyauClient.moduleManager.modules.get(HUDModule.class)).getColor(System.currentTimeMillis());
         default:
            return new Color(-1);
      }
   }

   private boolean showCustomColor() {
      int selection = this.color.getValue();
      return selection == 0;
   }

   private void drawObsidianBox(AxisAlignedBB bounds) {
      if (this.outline.getValue()) {
         RenderUtils.outlineBounds(bounds, 170, 0, 170, 255, 1.5F);
      }

      RenderUtils.fillBounds(bounds, 170, 0, 170, (int)((float)this.opacity.getValue().intValue() / 100.0F * 255.0F));
   }

   private void drawObsidianBlock(BlockPos position) {
      if (this.outline.getValue()) {
         RenderUtils.outlineBlockBounds(position, 1.0, 170, 0, 170, 255, 1.5F);
      }

      RenderUtils.fillBlockBounds(position, 1.0, 170, 0, 170, (int)((float)this.opacity.getValue().intValue() / 100.0F * 255.0F));
   }

   private AxisAlignedBB pairedBounds(BlockPos first, BlockPos second, double height) {
      return new AxisAlignedBB(
            (double)Math.min(first.getX(), second.getX()),
            (double)first.getY(),
            (double)Math.min(first.getZ(), second.getZ()),
            Math.max((double)first.getX() + 1.0, (double)second.getX() + 1.0),
            (double)first.getY() + height,
            Math.max((double)first.getZ() + 1.0, (double)second.getZ() + 1.0)
         )
         .offset(
            -((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosX(),
            -((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosY(),
            -((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosZ()
         );
   }

   @EventHandler
   public void onRenderWorld(RenderWorldEvent event) {
      if (this.isEnabled()) {
         RenderUtils.beginOverlay();

         for (BlockPos head : this.bedHeads) {
            IBlockState headState = minecraft.theWorld.getBlockState(head);
            if (headState.getBlock() instanceof BlockBed && headState.getValue(BlockBed.PART) == EnumPartType.HEAD) {
               BlockPos foot = head.offset(((EnumFacing)headState.getValue(BlockBed.FACING)).getOpposite());
               IBlockState footState = minecraft.theWorld.getBlockState(foot);
               if (footState.getBlock() instanceof BlockBed && footState.getValue(BlockBed.PART) == EnumPartType.FOOT) {
                  if (this.obsidian.getValue()) {
                     for (EnumFacing side : Arrays.asList(EnumFacing.UP, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST)) {
                        BlockPos nearHead = head.offset(side);
                        BlockPos nearFoot = foot.offset(side);
                        boolean headCovered = minecraft.theWorld.getBlockState(nearHead).getBlock() instanceof BlockObsidian;
                        boolean footCovered = minecraft.theWorld.getBlockState(nearFoot).getBlock() instanceof BlockObsidian;
                        if (headCovered && footCovered) {
                           this.drawObsidianBox(this.pairedBounds(nearHead, nearFoot, 1.0));
                        } else if (headCovered) {
                           this.drawObsidianBlock(nearHead);
                        } else if (footCovered) {
                           this.drawObsidianBlock(nearFoot);
                        }
                     }
                  }

                  AxisAlignedBB bounds = this.pairedBounds(head, foot, this.getBedHeight());
                  Color selected = this.bedColor();
                  if (this.outline.getValue()) {
                     RenderUtils.outlineBounds(bounds, selected.getRed(), selected.getGreen(), selected.getBlue(), 255, 1.5F);
                  }

                  RenderUtils.fillBounds(
                     bounds, selected.getRed(), selected.getGreen(), selected.getBlue(), (int)((float)this.opacity.getValue().intValue() / 100.0F * 255.0F)
                  );
               }
            } else {
               this.bedHeads.remove(head);
            }
         }

         RenderUtils.endOverlay();
      }
   }

   @Override
   public void onEnable() {
      if (minecraft.renderGlobal != null) {
         minecraft.renderGlobal.loadRenderers();
      }
   }
}
