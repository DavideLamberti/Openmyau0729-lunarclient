package myau.mixin;

import myau.MyauClient;
import myau.SprintModule;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SideOnly(Side.CLIENT)
@Mixin({AbstractClientPlayer.class})
public abstract class MixinAbstractClientPlayer {
   @Redirect(
      method = {"getFovModifier"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/ai/attributes/IAttributeInstance;getAttributeValue()D"
      )
   )
   private double redirectMovementSpeedAttribute(IAttributeInstance movementSpeedAttribute) {
      double attributeValue = movementSpeedAttribute.getAttributeValue();
      if ((Object)this instanceof EntityPlayerSP && MyauClient.moduleManager != null) {
         SprintModule sprint = (SprintModule)MyauClient.moduleManager.modules.get(SprintModule.class);
         return sprint.isEnabled() && sprint.shouldCompensateFov(movementSpeedAttribute) ? attributeValue * 1.300000011920929 : attributeValue;
      } else {
         return attributeValue;
      }
   }
}
