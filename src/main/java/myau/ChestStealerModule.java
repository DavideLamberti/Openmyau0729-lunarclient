package myau;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.WorldSettings.GameType;

public class ChestStealerModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private int stealDelayTicks;
   private int openDelayTicks;
   private boolean chestOpen;
   private boolean inventoryFullWarningShown;
   public final IntegerSetting minDelay = new IntegerSetting("min-delay", 1, 0, 20);
   public final IntegerSetting maxDelay = new IntegerSetting("max-delay", 2, 0, 20);
   public final IntegerSetting openDelay = new IntegerSetting("open-delay", 1, 0, 20);
   public final BooleanSetting autoClose = new BooleanSetting("auto-close", false);
   public final BooleanSetting nameCheck = new BooleanSetting("name-check", true);
   public final BooleanSetting skipTrash = new BooleanSetting("skip-trash", true);

   public ChestStealerModule() {
      super("ChestStealer", false, 0);
   }

   private boolean isSupportedGameMode() {
      GameType gameType = minecraft.playerController.getCurrentGameType();
      return gameType == GameType.SURVIVAL || gameType == GameType.ADVENTURE;
   }

   private void shiftClickSlot(int windowId, int slot) {
      minecraft.playerController.windowClick(windowId, slot, 0, 1, minecraft.thePlayer);
   }

   @EventHandler
   public void onRotationUpdate(RotationUpdateEvent event) {
      if (this.stealDelayTicks > 0) {
         this.stealDelayTicks--;
      }

      if (this.openDelayTicks > 0) {
         this.openDelayTicks--;
      }

      if (!(minecraft.currentScreen instanceof GuiChest)) {
         this.chestOpen = false;
      } else {
         Container container = ((GuiChest)minecraft.currentScreen).inventorySlots;
         if (!(container instanceof ContainerChest)) {
            this.chestOpen = false;
         } else {
            if (!this.chestOpen) {
               this.chestOpen = true;
               this.inventoryFullWarningShown = false;
               this.openDelayTicks = this.openDelay.getValue() + 1;
            }

            if (this.openDelayTicks <= 0 && this.stealDelayTicks <= 0 && this.isEnabled() && this.isSupportedGameMode()) {
               IInventory chestInventory = ((ContainerChest)container).getLowerChestInventory();
               if (this.nameCheck.getValue()) {
                  String inventoryName = chestInventory.getName();
                  if (!inventoryName.isEmpty()
                     && !inventoryName.equals(I18n.format("container.chest", new Object[0]))
                     && !inventoryName.equals(I18n.format("container.chestDouble", new Object[0]))) {
                     return;
                  }
               }

               if (minecraft.thePlayer.inventory.getFirstEmptyStack() == -1) {
                  if (!this.inventoryFullWarningShown) {
                     ChatUtils.addFormattedMessage(String.format("%s%s: &cYour inventory is full!&r", "&7[&bMyau&7] &r", this.getName()));
                     this.inventoryFullWarningShown = true;
                  }

                  if (this.autoClose.getValue()) {
                     minecraft.thePlayer.closeScreen();
                  }
               } else if (!this.skipTrash.getValue() || !this.stealBestUpgrade(container, chestInventory)) {
                  for (int slotIndex = 0; slotIndex < chestInventory.getSizeInventory(); slotIndex++) {
                     Slot slot = container.getSlot(slotIndex);
                     if (slot.getHasStack()) {
                        ItemStack stack = slot.getStack();
                        if (!this.skipTrash.getValue() || !ItemUtils.isDiscardableItem(stack)) {
                           this.shiftClickSlot(container.windowId, slotIndex);
                           return;
                        }
                     }
                  }

                  if (this.autoClose.getValue()) {
                     minecraft.thePlayer.closeScreen();
                  }
               }
            }
         }
      }
   }

   private boolean stealBestUpgrade(Container container, IInventory chestInventory) {
      int bestSwordSlot = -1;
      double bestSwordScore = 0.0;
      int[] bestArmorSlots = new int[]{-1, -1, -1, -1};
      double[] bestArmorScores = new double[]{0.0, 0.0, 0.0, 0.0};
      int bestPickaxeSlot = -1;
      float bestPickaxeScore = 1.0F;
      int bestShovelSlot = -1;
      float bestShovelScore = 1.0F;
      int bestAxeSlot = -1;
      float bestAxeScore = 1.0F;

      for (int slotIndex = 0; slotIndex < chestInventory.getSizeInventory(); slotIndex++) {
         Slot slot = container.getSlot(slotIndex);
         if (slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            Item item = stack.getItem();
            if (item instanceof ItemSword) {
               double score = ItemUtils.weaponScore(stack);
               if (bestSwordSlot == -1 || score > bestSwordScore) {
                  bestSwordSlot = slotIndex;
                  bestSwordScore = score;
               }
            } else if (item instanceof ItemArmor) {
               int armorType = ((ItemArmor)item).armorType;
               double score = ItemUtils.armorScore(stack);
               if (bestArmorSlots[armorType] == -1 || score > bestArmorScores[armorType]) {
                  bestArmorSlots[armorType] = slotIndex;
                  bestArmorScores[armorType] = score;
               }
            } else if (item instanceof ItemPickaxe) {
               float score = ItemUtils.toolEfficiencyScore(stack);
               if (bestPickaxeSlot == -1 || score > bestPickaxeScore) {
                  bestPickaxeSlot = slotIndex;
                  bestPickaxeScore = score;
               }
            } else if (item instanceof ItemSpade) {
               float score = ItemUtils.toolEfficiencyScore(stack);
               if (bestShovelSlot == -1 || score > bestShovelScore) {
                  bestShovelSlot = slotIndex;
                  bestShovelScore = score;
               }
            } else if (item instanceof ItemAxe) {
               float score = ItemUtils.toolEfficiencyScore(stack);
               if (bestAxeSlot == -1 || score > bestAxeScore) {
                  bestAxeSlot = slotIndex;
                  bestAxeScore = score;
               }
            }
         }
      }

      int currentSwordSlot = ItemUtils.findBestWeaponSlot(0, true);
      double currentSwordScore = currentSwordSlot != -1 ? ItemUtils.weaponScore(minecraft.thePlayer.inventory.getStackInSlot(currentSwordSlot)) : 0.0;
      if (bestSwordSlot != -1 && bestSwordScore > currentSwordScore) {
         this.shiftClickSlot(container.windowId, bestSwordSlot);
         return true;
      } else {
         for (int armorType = 0; armorType < 4; armorType++) {
            int currentArmorSlot = ItemUtils.findBestArmorSlot(armorType, true);
            double currentArmorScore = currentArmorSlot != -1
               ? ItemUtils.armorScore(minecraft.thePlayer.inventory.getStackInSlot(currentArmorSlot))
               : 0.0;
            if (bestArmorSlots[armorType] != -1 && bestArmorScores[armorType] > currentArmorScore) {
               this.shiftClickSlot(container.windowId, bestArmorSlots[armorType]);
               return true;
            }
         }

         if (isBetterTool("pickaxe", bestPickaxeSlot, bestPickaxeScore)) {
            this.shiftClickSlot(container.windowId, bestPickaxeSlot);
            return true;
         } else if (isBetterTool("shovel", bestShovelSlot, bestShovelScore)) {
            this.shiftClickSlot(container.windowId, bestShovelSlot);
            return true;
         } else if (isBetterTool("axe", bestAxeSlot, bestAxeScore)) {
            this.shiftClickSlot(container.windowId, bestAxeSlot);
            return true;
         } else {
            return false;
         }
      }
   }

   private static boolean isBetterTool(String toolClass, int chestSlot, float chestScore) {
      if (chestSlot == -1) {
         return false;
      } else {
         int currentSlot = ItemUtils.findBestToolSlot(toolClass, 0, true);
         float currentScore = currentSlot != -1 ? ItemUtils.toolEfficiencyScore(minecraft.thePlayer.inventory.getStackInSlot(currentSlot)) : 1.0F;
         return chestScore > currentScore;
      }
   }

   @EventHandler
   public void onWindowClick(WindowClickEvent event) {
      this.stealDelayTicks = org.apache.commons.lang3.RandomUtils.nextInt(this.minDelay.getValue() + 1, this.maxDelay.getValue() + 2);
   }

   @Override
   public void onSettingChanged(String name) {
      if ("min-delay".equals(name) && this.minDelay.getValue() > this.maxDelay.getValue()) {
         this.maxDelay.setValue(this.minDelay.getValue());
      } else if ("max-delay".equals(name) && this.minDelay.getValue() > this.maxDelay.getValue()) {
         this.minDelay.setValue(this.maxDelay.getValue());
      }
   }
}
