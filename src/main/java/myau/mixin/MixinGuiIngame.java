package myau.mixin;

import myau.MyauClient;
import myau.ScaffoldModule;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SideOnly(Side.CLIENT)
@Mixin({GuiIngame.class})
public abstract class MixinGuiIngame {
   @Redirect(
      method = {"updateTick"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/player/InventoryPlayer;getCurrentItem()Lnet/minecraft/item/ItemStack;"
      )
   )
   private ItemStack redirectCurrentHotbarItem(InventoryPlayer inventoryPlayer) {
      if (MyauClient.moduleManager == null) {
         return inventoryPlayer.getCurrentItem();
      } else {
         ScaffoldModule scaffold = (ScaffoldModule)MyauClient.moduleManager.modules.get(ScaffoldModule.class);
         if (scaffold != null && scaffold.isEnabled() && scaffold.itemSpoof.getValue()) {
            int spoofedSlot = scaffold.getPreviousHotbarSlot();
            if (spoofedSlot >= 0) {
               return inventoryPlayer.getStackInSlot(spoofedSlot);
            }
         }

         return inventoryPlayer.getCurrentItem();
      }
   }
}
