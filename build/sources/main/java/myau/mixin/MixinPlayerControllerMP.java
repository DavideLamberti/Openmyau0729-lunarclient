package myau.mixin;

import myau.MyauClient;
import myau.StopUsingItemEvent;
import myau.WindowClickEvent;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SideOnly(Side.CLIENT)
@Mixin({PlayerControllerMP.class})
public abstract class MixinPlayerControllerMP {
   @Inject(
      method = {"windowClick"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onWindowClick(int windowId, int slotId, int mouseButton, int clickMode, EntityPlayer player, CallbackInfoReturnable<ItemStack> callbackInfo) {
      if (MyauClient.eventBus != null) {
         WindowClickEvent event = new WindowClickEvent(windowId, slotId, mouseButton, clickMode);
         MyauClient.eventBus.post(event);
         if (event.isCancelled()) {
            callbackInfo.cancel();
         }
      }
   }

   @Inject(
      method = {"onStoppedUsingItem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void beforeStopUsingItem(CallbackInfo callbackInfo) {
      if (MyauClient.eventBus != null) {
         StopUsingItemEvent stopUsingItemEvent = new StopUsingItemEvent();
         MyauClient.eventBus.post(stopUsingItemEvent);
         if (stopUsingItemEvent.isCancelled()) {
            callbackInfo.cancel();
         }
      }
   }
}
