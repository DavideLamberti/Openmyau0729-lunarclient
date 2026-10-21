package myau.mixin;

import myau.MyauClient;
import myau.XrayModule;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SideOnly(Side.CLIENT)
@Mixin({BlockModelRenderer.class})
public abstract class MixinBlockModelRenderer {
   @Shadow
   public boolean renderModelAmbientOcclusion(IBlockAccess blockAccess, IBakedModel model, Block block, BlockPos position, WorldRenderer renderer, boolean checkSides) {
      return false;
   }

   @Inject(
      method = {"renderModel(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/resources/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockPos;Lnet/minecraft/client/renderer/WorldRenderer;Z)Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void beforeRenderModel(
      IBlockAccess blockAccess,
      IBakedModel model,
      IBlockState blockState,
      BlockPos position,
      WorldRenderer renderer,
      boolean checkSides,
      CallbackInfoReturnable<Boolean> callbackInfo
   ) {
      if (MyauClient.moduleManager != null && MyauClient.moduleManager.modules.get(XrayModule.class).isEnabled()) {
         callbackInfo.setReturnValue(this.renderModelAmbientOcclusion(blockAccess, model, blockState.getBlock(), position, renderer, checkSides));
      }
   }
}
