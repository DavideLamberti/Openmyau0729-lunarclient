package myau.mixin;

import myau.BlockClickEvent;
import myau.FramebufferResizeEvent;
import myau.HotbarChangeEvent;
import myau.KeyPressEvent;
import myau.LeftClickEvent;
import myau.MyauClient;
import myau.NoHitDelayModule;
import myau.PostTickEvent;
import myau.PreTickEvent;
import myau.RightClickEvent;
import myau.WorldLoadEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@SideOnly(Side.CLIENT)
@Mixin({Minecraft.class})
public abstract class MixinMinecraft {
   @Shadow
   private int leftClickCounter;
   @Shadow
   public PlayerControllerMP playerController;
   @Shadow
   public WorldClient theWorld;
   @Shadow
   public EntityPlayerSP thePlayer;
   @Shadow
   public GuiScreen currentScreen;

   @Inject(
      method = {"runGameLoop"},
      at = {@At("RETURN")}
   )
   private void afterRunGameLoop(CallbackInfo callbackInfo) {
      if (MyauClient.moduleManager != null) {
         MyauClient.moduleManager.update();
      }
   }

   @Inject(
      method = {"runTick"},
      at = {@At("HEAD")}
   )
   private void beforeRunTick(CallbackInfo callbackInfo) {
      if (this.theWorld != null && this.thePlayer != null && MyauClient.eventBus != null) {
         MyauClient.eventBus.post(new PreTickEvent());
      }
   }

   @Inject(
      method = {"runTick"},
      at = {@At("RETURN")}
   )
   private void afterRunTick(CallbackInfo callbackInfo) {
      if (this.theWorld != null && this.thePlayer != null && MyauClient.eventBus != null) {
         MyauClient.eventBus.post(new PostTickEvent());
      }
   }

   @Inject(
      method = {"loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V"},
      at = {@At("HEAD")}
   )
   private void beforeLoadWorld(WorldClient world, String loadingMessage, CallbackInfo callbackInfo) {
      if (MyauClient.eventBus != null) {
         MyauClient.eventBus.post(new WorldLoadEvent());
      }
   }

   @Inject(
      method = {"updateFramebufferSize"},
      at = {@At("RETURN")}
   )
   private void afterFramebufferResize(CallbackInfo callbackInfo) {
      if (MyauClient.eventBus != null) {
         MyauClient.eventBus.post(new FramebufferResizeEvent());
      }
   }

   @Inject(
      method = {"clickMouse"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void beforeLeftClick(CallbackInfo callbackInfo) {
      if (MyauClient.moduleManager != null && MyauClient.moduleManager.modules.get(NoHitDelayModule.class).isEnabled()) {
         this.leftClickCounter = 0;
      }

      if (MyauClient.eventBus != null) {
         LeftClickEvent event = new LeftClickEvent();
         MyauClient.eventBus.post(event);
         if (event.isCancelled()) {
            callbackInfo.cancel();
         }
      }
   }

   @Inject(
      method = {"rightClickMouse"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void beforeRightClick(CallbackInfo callbackInfo) {
      if (MyauClient.eventBus != null) {
         RightClickEvent rightClickEvent = new RightClickEvent();
         MyauClient.eventBus.post(rightClickEvent);
         if (rightClickEvent.isCancelled()) {
            callbackInfo.cancel();
         }
      }
   }

   @Inject(
      method = {"sendClickBlockToController"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void beforeBlockClick(CallbackInfo callbackInfo) {
      if (MyauClient.eventBus != null) {
         BlockClickEvent blockClickEvent = new BlockClickEvent();
         MyauClient.eventBus.post(blockClickEvent);
         if (blockClickEvent.isCancelled()) {
            callbackInfo.cancel();
            this.playerController.resetBlockRemoving();
         }
      }
   }

   @Inject(
      method = {"runTick"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/settings/KeyBinding;setKeyBindState(IZ)V",
         shift = At.Shift.AFTER
      ),
      locals = LocalCapture.CAPTURE_FAILSOFT
   )
   private void afterKeyStateChange(CallbackInfo ci, int keyCode, boolean pressed) {
      if (pressed && this.currentScreen == null && MyauClient.eventBus != null) {
         MyauClient.eventBus.post(new KeyPressEvent(keyCode));
      }
   }

   @Redirect(
      method = {"runTick"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/player/InventoryPlayer;changeCurrentItem(I)V"
      )
   )
   private void redirectHotbarScroll(InventoryPlayer inventoryPlayer, int scrollDirection) {
      if (MyauClient.eventBus != null) {
         HotbarChangeEvent hotbarChangeEvent = new HotbarChangeEvent(-1, scrollDirection);
         MyauClient.eventBus.post(hotbarChangeEvent);
         if (!hotbarChangeEvent.isCancelled()) {
            inventoryPlayer.changeCurrentItem(scrollDirection);
         }
      }
   }
}
