package myau;

import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;

public final class ItemUtils {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private static final List<Integer> BENEFICIAL_POTION_IDS = new BeneficialPotionIds();
   private static final int INVENTORY_SEARCH_SIZE = 36;
   private static final int HOTBAR_SIZE = 9;
   private static final int TOTAL_INVENTORY_SLOTS = 40;
   private static final int MINIMUM_TOOL_DURABILITY = 30;

   private ItemUtils() {
   }

   public static boolean isDiscardableItem(ItemStack stack) {
      if (stack == null) {
         return false;
      } else {
         Item item = stack.getItem();
         if (item instanceof ItemBlock) {
            return !isSuitablePlacementBlock((ItemBlock)item);
         } else if (item instanceof ItemPotion) {
            List<PotionEffect> effects = ((ItemPotion)item).getEffects(stack);
            return effects.stream().<Integer>map(PotionEffect::getPotionID).noneMatch(BENEFICIAL_POTION_IDS::contains);
         } else if (item instanceof ItemFishingRod
            || item instanceof ItemSnowball
            || item instanceof ItemEgg
            || item instanceof ItemBucket
            || item instanceof ItemEnderPearl) {
            return false;
         } else {
            return item instanceof ItemFood && item != Items.spider_eye ? false : item != Items.nether_star;
         }
      }
   }

   public static boolean isPlaceableBlockStack(ItemStack stack) {
      return stack != null
         && stack.stackSize >= 1
         && stack.getItem() instanceof ItemBlock
         && isSuitablePlacementBlock((ItemBlock)stack.getItem());
   }

   public static boolean isSuitablePlacementBlock(ItemBlock itemBlock) {
      Block block = itemBlock.getBlock();
      return !BlockUtils.isInteractable(block) && BlockUtils.isSuitableFullBlock(block);
   }

   public static double weaponScore(ItemStack stack) {
      if (stack == null) {
         return 0.0;
      } else {
         double score = 0.0;
         Multimap<String, AttributeModifier> modifiers = stack.getAttributeModifiers();

         for (String attributeName : modifiers.keySet()) {
            if ("generic.attackDamage".equals(attributeName)) {
               Collection<AttributeModifier> attackModifiers = modifiers.get(attributeName);
               if (!attackModifiers.isEmpty()) {
                  score += attackModifiers.iterator().next().getAmount();
               }
            }
         }

         if (stack.isItemEnchanted()) {
            score += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);
            score += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25;
         }

         return score;
      }
   }

   public static float toolEfficiencyScore(ItemStack stack) {
      if (stack != null && stack.getItem() instanceof ItemTool) {
         float score = ((ItemTool)stack.getItem()).getToolMaterial().getEfficiencyOnProperMaterial();
         if (score > 1.0F) {
            int efficiencyLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack);
            if (efficiencyLevel > 0) {
               score += (float)(efficiencyLevel * efficiencyLevel + 1);
            }
         }

         return score;
      } else {
         return 1.0F;
      }
   }

   public static double armorScore(ItemStack stack) {
      if (stack != null && stack.getItem() instanceof ItemArmor) {
         double score = (double)((ItemArmor)stack.getItem()).damageReduceAmount;
         if (stack.isItemEnchanted()) {
            score += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack) * 0.25;
         }

         return score;
      } else {
         return 0.0;
      }
   }

   public static int findBestWeaponSlot(int startSlot, boolean avoidLowDurability) {
      int bestSlot = -1;
      double bestScore = 0.0;

      for (int offset = 0; offset < 36; offset++) {
         int slot = wrapInventorySlot(startSlot + offset);
         ItemStack stackCandidate = minecraft.thePlayer.inventory.getStackInSlot(slot);
         if (stackCandidate != null && stackCandidate.getItem() instanceof ItemSword && (!avoidLowDurability || !hasLowDurability(stackCandidate))) {
            double score = weaponScore(stackCandidate);
            if (score > bestScore) {
               bestSlot = slot;
               bestScore = score;
            }
         }
      }

      return bestSlot;
   }

   public static int findBestToolSlot(String toolClass, int startSlot, boolean avoidLowDurability) {
      int bestSlot = -1;
      float bestScore = 1.0F;

      for (int offset = 0; offset < 36; offset++) {
         int slot = wrapInventorySlot(startSlot + offset);
         ItemStack stack = minecraft.thePlayer.inventory.getStackInSlot(slot);
         if (stack != null
            && stack.getItem() instanceof ItemTool
            && stack.getItem().getToolClasses(stack).contains(toolClass)
            && (!avoidLowDurability || !hasLowDurability(stack))) {
            float score = toolEfficiencyScore(stack);
            if (score > bestScore) {
               bestSlot = slot;
               bestScore = score;
            }
         }
      }

      return bestSlot;
   }

   public static int findBestToolForBlock(int preferredSlot, Block block) {
      int bestSlot = preferredSlot;
      ItemStack preferredStack = minecraft.thePlayer.inventory.getStackInSlot(preferredSlot);
      float bestStrength = preferredStack == null ? 1.0F : preferredStack.getStrVsBlock(block);

      for (int slot = 0; slot < 9; slot++) {
         ItemStack stackSecondary = minecraft.thePlayer.inventory.getStackInSlot(slot);
         if (stackSecondary != null) {
            float strength = stackSecondary.getStrVsBlock(block);
            if (strength > bestStrength) {
               bestSlot = slot;
               bestStrength = strength;
            }
         }
      }

      return bestSlot;
   }

   public static int findBestArmorSlot(int armorType, boolean avoidLowDurability) {
      int bestSlot = -1;
      double bestScore = 0.0;

      for (int slot = 0; slot < 40; slot++) {
         ItemStack stackAlternate = minecraft.thePlayer.inventory.getStackInSlot(slot);
         if (stackAlternate != null && stackAlternate.getItem() instanceof ItemArmor) {
            ItemArmor armor = (ItemArmor)stackAlternate.getItem();
            if (armor.armorType == armorType && (!avoidLowDurability || !hasLowDurability(stackAlternate))) {
               double score = armorScore(stackAlternate);
               if (score >= bestScore) {
                  bestSlot = slot;
                  bestScore = score;
               }
            }
         }
      }

      return bestSlot;
   }

   public static int findLargestBlockStackSlot(int startSlot) {
      int bestSlot = -1;
      int largestStackSize = 0;

      for (int offset = 0; offset < 36; offset++) {
         int slot = wrapInventorySlot(startSlot + offset);
         ItemStack stackResult = minecraft.thePlayer.inventory.getStackInSlot(slot);
         if (isPlaceableBlockStack(stackResult) && stackResult.stackSize > largestStackSize) {
            bestSlot = slot;
            largestStackSize = stackResult.stackSize;
         }
      }

      return bestSlot;
   }

   public static boolean isHoldingWeapon() {
      ItemStack heldItem = minecraft.thePlayer.getHeldItem();
      if (heldItem == null) {
         return false;
      } else {
         if (heldItem.hasTagCompound()) {
            NBTTagCompound tags = heldItem.getTagCompound();
            if (tags.hasKey("ExtraAttributes")) {
               NBTTagCompound extraAttributes = tags.getCompoundTag("ExtraAttributes");
               if (extraAttributes.hasKey("UHCid")) {
                  long itemId = extraAttributes.getLong("UHCid");
                  if (itemId == 50006L || itemId == 50009L) {
                     return true;
                  }
               }
            }

            if (tags.hasKey("HideFlags") && heldItem.getItem() instanceof ItemSpade) {
               ToolMaterial material = ((ItemSpade)heldItem.getItem()).getToolMaterial();
               if (material == ToolMaterial.EMERALD) {
                  return true;
               }
            }

            if (tags.hasKey("ItemModel") && "minecraft:mace".equals(tags.getString("ItemModel"))) {
               return true;
            }
         }

         if (heldItem.getItem() instanceof ItemEnchantedBook) {
            return false;
         } else {
            Map enchantments = EnchantmentHelper.getEnchantments(heldItem);
            return enchantments.containsKey(19) || heldItem.getItem() instanceof ItemSword;
         }
      }
   }

   public static boolean isHoldingSword() {
      ItemStack heldItem = minecraft.thePlayer.getHeldItem();
      return heldItem != null && heldItem.getItem() instanceof ItemSword;
   }

   public static boolean isHoldingTool() {
      ItemStack heldItem = minecraft.thePlayer.getHeldItem();
      return heldItem != null && heldItem.getItem() instanceof ItemTool;
   }

   public static boolean isHoldingConsumable() {
      ItemStack heldItem = minecraft.thePlayer.getHeldItem();
      if (heldItem == null) {
         return false;
      } else if (ItemPotion.isSplash(heldItem.getItem().getMetadata(heldItem))) {
         return false;
      } else {
         EnumAction action = heldItem.getItemUseAction();
         return action == EnumAction.EAT || action == EnumAction.DRINK;
      }
   }

   public static boolean isHoldingBow() {
      ItemStack heldItem = minecraft.thePlayer.getHeldItem();
      return heldItem != null && heldItem.getItem() instanceof ItemBow;
   }

   public static boolean isHoldingBlocks() {
      ItemStack heldItem = minecraft.thePlayer.getHeldItem();
      return heldItem != null && heldItem.stackSize >= 1 && heldItem.getItem() instanceof ItemBlock;
   }

   public static boolean isHoldingPlaceableBlock() {
      return isPlaceableBlockStack(minecraft.thePlayer.getHeldItem());
   }

   public static boolean isHoldingFireball() {
      ItemStack heldItem = minecraft.thePlayer.getHeldItem();
      return heldItem != null && heldItem.stackSize >= 1 && heldItem.getItem() instanceof ItemFireball;
   }

   private static int wrapInventorySlot(int slot) {
      return (slot % 36 + 36) % 36;
   }

   private static boolean hasLowDurability(ItemStack stackValue) {
      return stackValue.isItemDamaged() && stackValue.getMaxDamage() - stackValue.getItemDamage() < 30;
   }
}
