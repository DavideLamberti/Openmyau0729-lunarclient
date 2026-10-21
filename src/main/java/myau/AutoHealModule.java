package myau;

import myau.mixin.AccessorPlayerControllerMP;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.potion.Potion;

public class AutoHealModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private final ElapsedTimer healDelayTimer = new ElapsedTimer();
   private boolean wasBelowHealthThreshold;
   private int previousHotbarSlot = -1;
   public final PercentSetting health = new PercentSetting("health", 35);
   public final IntegerSetting delay = new IntegerSetting("delay", 4000, 0, 5000);
   public final BooleanSetting regenCheck = new BooleanSetting("regen-check", false);

   public AutoHealModule() {
      super("AutoHeal", false, 0);
   }

   private int findHealingItemSlot() {
      for (int slot = 0; slot < 9; slot++) {
         ItemStack stack = minecraft.thePlayer.inventory.getStackInSlot(slot);
         if (stack != null && stack.hasDisplayName()) {
            String displayName = stack.getDisplayName();
            if (stack.getItem() instanceof ItemSkull) {
               if (displayName.contains("§6") && displayName.contains("Golden Head")) {
                  return slot;
               }

               if (displayName.matches("\\S+§c's Head")) {
                  return slot;
               }
            } else if (stack.getItem() instanceof ItemSoup
               && displayName.contains("§a")
               && (displayName.contains("Tasty Soup") || displayName.contains("Assist Soup"))) {
               return slot;
            }
         }
      }

      return -1;
   }

   private boolean isRegenerationBlockingHeal() {
      return this.regenCheck.getValue() && minecraft.thePlayer.isPotionActive(Potion.regeneration);
   }

   public boolean hasPendingSlotRestore() {
      return this.previousHotbarSlot != -1;
   }

   @EventHandler(
      priority = 50
   )
   public void onPreTick(PreTickEvent event) {
      if (this.isEnabled()) {
         float currentHealth = (float)Math.ceil((double)(minecraft.thePlayer.getHealth() + minecraft.thePlayer.getAbsorptionAmount()));
         boolean belowHealthThreshold = currentHealth / minecraft.thePlayer.getMaxHealth() <= (float)this.health.getValue().intValue() / 100.0F;
         if (this.wasBelowHealthThreshold
            && belowHealthThreshold
            && !this.isRegenerationBlockingHeal()
            && this.healDelayTimer.hasElapsed((long)this.delay.getValue().intValue())) {
            int healingSlot = this.findHealingItemSlot();
            if (healingSlot != -1) {
               this.previousHotbarSlot = minecraft.thePlayer.inventory.currentItem;
               minecraft.thePlayer.inventory.currentItem = healingSlot;
               ((AccessorPlayerControllerMP)minecraft.playerController).callSyncCurrentPlayItem();
               PacketUtils.send(new C08PacketPlayerBlockPlacement(minecraft.thePlayer.getHeldItem()));
               this.healDelayTimer.reset();
            }
         }

         this.wasBelowHealthThreshold = belowHealthThreshold;
      }
   }

   @EventHandler
   public void onPostTick(PostTickEvent event) {
      if (this.previousHotbarSlot != -1) {
         minecraft.thePlayer.inventory.currentItem = this.previousHotbarSlot;
         this.previousHotbarSlot = -1;
      }
   }

   @EventHandler
   public void onLeftClick(LeftClickEvent event) {
      if (this.isEnabled() && this.hasPendingSlotRestore()) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onRightClick(RightClickEvent event) {
      if (this.isEnabled() && this.hasPendingSlotRestore()) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onBlockClick(BlockClickEvent event) {
      if (this.isEnabled() && this.hasPendingSlotRestore()) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onHotbarChange(HotbarChangeEvent event) {
      if (this.isEnabled() && this.hasPendingSlotRestore()) {
         event.setCancelled(true);
      }
   }
}
