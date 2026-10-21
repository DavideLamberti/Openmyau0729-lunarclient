package myau.mixin;

import myau.ChamsModule;
import myau.MyauClient;
import myau.ViewClipModule;
import myau.XrayModule;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SideOnly(Side.CLIENT)
@Mixin({VisGraph.class})
public abstract class MixinVisGraph {
   @Inject(
      method = {"func_178606_a"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void beforeMarkOpaqueCube(CallbackInfo callbackInfo) {
      if (MyauClient.moduleManager != null
         && (
            MyauClient.moduleManager.modules.get(ChamsModule.class).isEnabled()
               || MyauClient.moduleManager.modules.get(ViewClipModule.class).isEnabled()
               || MyauClient.moduleManager.modules.get(XrayModule.class).isEnabled()
         )) {
         callbackInfo.cancel();
      }
   }

   @Inject(
      method = {"computeVisibility"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void beforeComputeVisibility(CallbackInfoReturnable<SetVisibility> callbackInfo) {
      if (MyauClient.moduleManager != null
         && (
            MyauClient.moduleManager.modules.get(ChamsModule.class).isEnabled()
               || MyauClient.moduleManager.modules.get(ViewClipModule.class).isEnabled()
               || MyauClient.moduleManager.modules.get(XrayModule.class).isEnabled()
         )) {
         SetVisibility visibility = new SetVisibility();
         visibility.setAllVisible(true);
         callbackInfo.setReturnValue(visibility);
      }
   }
}
