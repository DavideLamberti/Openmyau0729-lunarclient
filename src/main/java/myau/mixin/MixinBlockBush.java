package myau.mixin;

import myau.MyauClient;
import myau.XrayModule;
import net.minecraft.block.BlockBush;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SideOnly(Side.CLIENT)
@Mixin({BlockBush.class})
public abstract class MixinBlockBush {
   @Inject(
      method = {"getBlockLayer"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetBlockLayer(CallbackInfoReturnable<EnumWorldBlockLayer> callbackInfo) {
      if (MyauClient.moduleManager != null) {
         XrayModule xray = (XrayModule)MyauClient.moduleManager.modules.get(XrayModule.class);
         if (xray.isEnabled()) {
            callbackInfo.setReturnValue(EnumWorldBlockLayer.TRANSLUCENT);
         }
      }
   }
}
