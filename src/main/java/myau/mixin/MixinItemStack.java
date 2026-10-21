package myau.mixin;

import myau.ESPModule;
import myau.MyauClient;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SideOnly(Side.CLIENT)
@Mixin({ItemStack.class})
public abstract class MixinItemStack {
   @Inject(
      method = {"hasEffect"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onHasEffect(CallbackInfoReturnable<Boolean> callbackInfo) {
      if (MyauClient.moduleManager != null) {
         ESPModule eSP = (ESPModule)MyauClient.moduleManager.modules.get(ESPModule.class);
         if (eSP.isEnabled() && !eSP.isEnchantmentGlintAllowed()) {
            callbackInfo.setReturnValue(false);
         }
      }
   }
}
