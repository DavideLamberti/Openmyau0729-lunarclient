package myau;

import java.util.ArrayList;
import java.util.Map;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;

public final class EnchantmentText {
   private static final Map<Integer, EnchantmentLabel> ENCHANTMENT_LABELS = new EnchantmentLabels();

   private EnchantmentText() {
   }

   private static ChatColor colorForLevel(int level, int maximumLevel) {
      if (level > maximumLevel) {
         return ChatColor.LIGHT_PURPLE;
      } else if (level == maximumLevel) {
         return ChatColor.RED;
      } else {
         switch (level) {
            case 1:
               return ChatColor.AQUA;
            case 2:
               return ChatColor.GREEN;
            case 3:
               return ChatColor.YELLOW;
            case 4:
               return ChatColor.GOLD;
            default:
               return ChatColor.GRAY;
         }
      }
   }

   public static ArrayList<String> labels(ItemStack stack) {
      ArrayList<String> labels = new ArrayList<>();
      NBTTagList enchantments = stack.getItem() == Items.enchanted_book ? Items.enchanted_book.getEnchantments(stack) : stack.getEnchantmentTagList();
      if (enchantments == null) {
         return labels;
      } else {
         for (int index = 0; index < enchantments.tagCount(); index++) {
            int enchantmentId = enchantments.getCompoundTagAt(index).getInteger("id");
            EnchantmentLabel label = ENCHANTMENT_LABELS.get(enchantmentId);
            if (label != null) {
               short level = enchantments.getCompoundTagAt(index).getShort("lvl");
               labels.add(ChatColor.formatText(String.format("&r%s%s%d&r", label.abbreviation, colorForLevel(level, label.maxLevel), level)));
            }
         }

         return labels;
      }
   }
}
