package myau.mixin;

import myau.BedESPModule;
import myau.MyauClient;
import myau.XrayModule;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockBed.EnumPartType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SideOnly(Side.CLIENT)
@Mixin({BlockRendererDispatcher.class})
public abstract class MixinBlockRendererDispatcher {
   @Inject(
      method = {"renderBlock"},
      at = {@At("HEAD")}
   )
   private void onRenderBlock(
      IBlockState blockState, BlockPos position, IBlockAccess iBlockAccess, WorldRenderer worldRenderer, CallbackInfoReturnable<Boolean> callbackInfo
   ) {
      if (MyauClient.moduleManager != null) {
         BedESPModule bedESP = (BedESPModule)MyauClient.moduleManager.modules.get(BedESPModule.class);
         if (bedESP.isEnabled() && blockState.getBlock() instanceof BlockBed && blockState.getValue(BlockBed.PART) == EnumPartType.HEAD) {
            bedESP.bedHeads.add(new BlockPos(position));
         }

         XrayModule xray = (XrayModule)MyauClient.moduleManager.modules.get(XrayModule.class);
         if (xray.isEnabled() && xray.isBlockEnabled(Block.getIdFromBlock(blockState.getBlock()))) {
            if (xray.passesCaveCheck(position)) {
               xray.discoveredBlocks.add(new BlockPos(position));
            } else {
               xray.discoveredBlocks.remove(position);
            }
         }
      }
   }
}
