package myau;

import com.google.common.base.CaseFormat;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import myau.mixin.AccessorC0DPacketCloseWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.client.C16PacketClientStatus.EnumState;

public class InvWalkModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private final Queue<C0EPacketClickWindow> queuedWindowClicks = new ConcurrentLinkedQueue<>();
   private boolean movementKeysUpdated = false;
   private C16PacketClientStatus pendingInventoryOpenPacket = null;
   private int clickReleaseDelayTicks = 0;
   public final ModeSetting mode = new ModeSetting("mode", 1, new String[]{"VANILLA", "LEGIT", "HYPIXEL"});

   public InvWalkModule() {
      super("InvWalk", false, 0);
   }

   public void updateMovementKeys() {
      KeyBinding[] movementKeys = new KeyBinding[]{
         minecraft.gameSettings.keyBindForward,
         minecraft.gameSettings.keyBindLeft,
         minecraft.gameSettings.keyBindBack,
         minecraft.gameSettings.keyBindRight,
         minecraft.gameSettings.keyBindJump,
         minecraft.gameSettings.keyBindSprint
      };

      for (KeyBinding binding : movementKeys) {
         InputUtils.updateKeyState(binding.getKeyCode());
      }

      if (MyauClient.moduleManager.modules.get(SprintModule.class).isEnabled()) {
         InputUtils.setKeyState(minecraft.gameSettings.keyBindSprint.getKeyCode(), true);
      }

      this.movementKeysUpdated = true;
   }

   public boolean canUpdateMovementKeys() {
      if (minecraft.currentScreen instanceof myau.ui.ClickGui) {
         return true;
      }
      if (minecraft.currentScreen instanceof GuiContainer && !(minecraft.currentScreen instanceof GuiContainerCreative)) {
         switch (this.mode.getValue()) {
            case 1:
               return minecraft.currentScreen instanceof GuiInventory && this.pendingInventoryOpenPacket != null && this.queuedWindowClicks.isEmpty();
            case 2:
               return minecraft.currentScreen instanceof GuiInventory && this.queuedWindowClicks.isEmpty();
            default:
               return true;
         }
      } else {
         return false;
      }
   }

   @EventHandler(
      priority = -100
   )
   public void onPreTick(PreTickEvent event) {
      C0EPacketClickWindow queuedClick;
      while ((queuedClick = this.queuedWindowClicks.poll()) != null) {
         PacketUtils.sendWithoutSendEvent(queuedClick);
      }
   }

   @EventHandler(
      priority = -100
   )
   public void onRotationUpdate(RotationUpdateEvent event) {
      if (this.isEnabled()) {
         if (this.canUpdateMovementKeys() && this.clickReleaseDelayTicks == 0) {
            this.updateMovementKeys();
         } else {
            if (this.movementKeysUpdated) {
               if (minecraft.currentScreen != null) {
                  KeyBinding.unPressAllKeys();
               }

               this.movementKeysUpdated = false;
            }

            if (this.pendingInventoryOpenPacket != null) {
               PacketUtils.sendWithoutSendEvent(this.pendingInventoryOpenPacket);
               this.pendingInventoryOpenPacket = null;
            }

            if (this.clickReleaseDelayTicks > 0) {
               this.clickReleaseDelayTicks--;
            }
         }
      }
   }

   @EventHandler
   public void onPacketSend(PacketSendEvent event) {
      if (this.isEnabled()) {
         if (event.getPacket() instanceof C16PacketClientStatus) {
            if (this.mode.getValue() == 1) {
               C16PacketClientStatus statusPacket = (C16PacketClientStatus)event.getPacket();
               if (statusPacket.getStatus() == EnumState.OPEN_INVENTORY_ACHIEVEMENT) {
                  event.setCancelled(true);
                  this.pendingInventoryOpenPacket = statusPacket;
               }
            }
         } else if (!(event.getPacket() instanceof C0EPacketClickWindow)) {
            if (event.getPacket() instanceof C0DPacketCloseWindow) {
               C0DPacketCloseWindow closePacket = (C0DPacketCloseWindow)event.getPacket();
               if (this.pendingInventoryOpenPacket != null && ((AccessorC0DPacketCloseWindow)closePacket).getWindowId() == 0) {
                  this.pendingInventoryOpenPacket = null;
                  event.setCancelled(true);
               }
            }
         } else {
            C0EPacketClickWindow clickPacket = (C0EPacketClickWindow)event.getPacket();
            switch (this.mode.getValue()) {
               case 1:
                  if (clickPacket.getWindowId() == 0) {
                     if ((clickPacket.getMode() == 3 || clickPacket.getMode() == 4) && clickPacket.getSlotId() == -999) {
                        event.setCancelled(true);
                        return;
                     }

                     if (this.pendingInventoryOpenPacket != null) {
                        KeyBinding.unPressAllKeys();
                        event.setCancelled(true);
                        this.queuedWindowClicks.offer(clickPacket);
                     }
                  }
                  break;
               case 2:
                  if (clickPacket.getWindowId() == 0) {
                     if ((clickPacket.getMode() == 3 || clickPacket.getMode() == 4) && clickPacket.getSlotId() == -999) {
                        event.setCancelled(true);
                     } else {
                        KeyBinding.unPressAllKeys();
                        event.setCancelled(true);
                        this.queuedWindowClicks.offer(clickPacket);
                        this.clickReleaseDelayTicks = 8;
                     }
                  }
            }

            if (this.pendingInventoryOpenPacket != null) {
               PacketUtils.sendWithoutSendEvent(this.pendingInventoryOpenPacket);
               this.pendingInventoryOpenPacket = null;
            }
         }
      }
   }

   @Override
   public void onDisable() {
      if (this.movementKeysUpdated) {
         if (minecraft.currentScreen != null) {
            KeyBinding.unPressAllKeys();
         }

         this.movementKeysUpdated = false;
      }

      if (this.pendingInventoryOpenPacket != null) {
         PacketUtils.sendWithoutSendEvent(this.pendingInventoryOpenPacket);
         this.pendingInventoryOpenPacket = null;
      }

      this.clickReleaseDelayTicks = 0;
   }

   @Override
   public String[] getHudInfo() {
      return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeName())};
   }
}
