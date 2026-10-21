package myau.mixin;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@SideOnly(Side.CLIENT)
@Mixin({PlayerControllerMP.class})
public interface AccessorPlayerControllerMP {
   @Accessor
   float getCurBlockDamageMP();

   @Accessor
   void setCurBlockDamageMP(float var1);

   @Accessor
   int getBlockHitDelay();

   @Accessor
   void setBlockHitDelay(int var1);

   @Accessor
   boolean getIsHittingBlock();

   @Accessor
   int getCurrentPlayerItem();

   @Accessor
   void setCurrentPlayerItem(int var1);

   @Invoker
   void callSyncCurrentPlayItem();
}
