package myau;

import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class ItemNameCommand extends Command {
   private static final Minecraft minecraft = Minecraft.getMinecraft();

   public ItemNameCommand() {
      super(new ArrayList<>(Arrays.asList("itemname", "item")));
   }

   @Override
   public void execute(ArrayList<String> arguments) {
      ItemStack stack = minecraft.thePlayer.inventory.getCurrentItem();
      if (stack != null) {
         String displayName = stack.getDisplayName().replace('§', '&');
         String registryName = stack.getItem().getRegistryName();
         String nbt = stack.hasTagCompound() ? stack.getTagCompound().toString().replace('§', '&') : "";
         ChatUtils.addRawMessage(String.format("%s%s (%s) %s", ChatColor.formatText("&7[&bMyau&7] &r"), displayName, registryName, nbt));
      }
   }
}
