package myau.mixin;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@SideOnly(Side.CLIENT)
@Mixin({EntityPlayer.class})
public interface AccessorEntityPlayer {
   @Accessor
   ItemStack getItemInUse();

   @Accessor
   void setItemInUse(ItemStack var1);

   @Accessor
   int getItemInUseCount();

   @Accessor
   void setItemInUseCount(int var1);
}
