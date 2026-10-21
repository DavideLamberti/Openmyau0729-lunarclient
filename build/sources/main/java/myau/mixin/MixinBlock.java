package myau.mixin;

import myau.MyauClient;
import myau.XrayModule;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SideOnly(Side.CLIENT)
@Mixin({Block.class})
public abstract class MixinBlock {
   @Inject(
      method = {"shouldSideBeRendered"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onShouldSideBeRendered(IBlockAccess iBlockAccess, BlockPos position, EnumFacing facing, CallbackInfoReturnable<Boolean> callbackInfo) {
      if (MyauClient.moduleManager != null) {
         XrayModule xray = (XrayModule)MyauClient.moduleManager.modules.get(XrayModule.class);
         if (xray.isEnabled() && xray.mode.getValue() == 1 && xray.isSupportedBlock(Block.getIdFromBlock((Block)(Object)this))) {
            int neighborX = position.getX() - facing.getDirectionVec().getX();
            int neighborY = position.getY() - facing.getDirectionVec().getY();
            int neighborZ = position.getZ() - facing.getDirectionVec().getZ();
            if (xray.passesCaveCheck(new BlockPos(neighborX, neighborY, neighborZ))) {
               callbackInfo.setReturnValue(true);
            }
         }
      }
   }

   @Inject(
      method = {"getBlockLayer"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetBlockLayer(CallbackInfoReturnable<EnumWorldBlockLayer> callbackInfo) {
      if (MyauClient.moduleManager != null) {
         XrayModule xray = (XrayModule)MyauClient.moduleManager.modules.get(XrayModule.class);
         if (xray.isEnabled()) {
            int blockId = Block.getIdFromBlock((Block)(Object)this);
            if (!xray.isSupportedBlock(blockId) || xray.mode.getValue() == 0 && !xray.isBlockEnabled(blockId)) {
               callbackInfo.setReturnValue(EnumWorldBlockLayer.TRANSLUCENT);
            }
         }
      }
   }
}
