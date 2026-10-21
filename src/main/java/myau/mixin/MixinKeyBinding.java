package myau.mixin;

import myau.HotbarChangeEvent;
import myau.MyauClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SideOnly(Side.CLIENT)
@Mixin({KeyBinding.class})
public abstract class MixinKeyBinding {
   @Final
   @Shadow
   private String keyDescription;

   @Inject(
      method = {"isPressed"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onIsPressed(CallbackInfoReturnable<Boolean> callbackInfo) {
      if (callbackInfo.getReturnValue() && MyauClient.eventBus != null) {
         Minecraft minecraft = Minecraft.getMinecraft();

         for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            if (minecraft.gameSettings.keyBindsHotbar[hotbarSlot].getKeyDescription().equals(this.keyDescription)) {
               HotbarChangeEvent event = new HotbarChangeEvent(hotbarSlot, 0);
               MyauClient.eventBus.post(event);
               if (event.isCancelled()) {
                  callbackInfo.setReturnValue(false);
               }
            }
         }
      }
   }
}
