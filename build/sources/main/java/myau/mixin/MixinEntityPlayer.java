package myau.mixin;

import myau.KeepSprintModule;
import myau.MyauClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@SideOnly(Side.CLIENT)
@Mixin({EntityPlayer.class})
public abstract class MixinEntityPlayer extends MixinEntityLivingBase {
   @ModifyConstant(
      method = {"attackTargetEntityWithCurrentItem"},
      constant = {@Constant(
         doubleValue = 0.6
      )}
   )
   private double modifyAttackSlowdown(double slowdownMultiplier) {
      if (MyauClient.moduleManager == null) {
         return slowdownMultiplier;
      } else {
         KeepSprintModule keepSprint = (KeepSprintModule)MyauClient.moduleManager.modules.get(KeepSprintModule.class);
         return keepSprint.isEnabled() && keepSprint.shouldPreserveSprint()
            ? slowdownMultiplier + (1.0 - slowdownMultiplier) * (1.0 - keepSprint.slowdown.getValue().doubleValue() / 100.0)
            : slowdownMultiplier;
      }
   }

   @Redirect(
      method = {"attackTargetEntityWithCurrentItem"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/player/EntityPlayer;setSprinting(Z)V"
      )
   )
   private void redirectAttackSprintReset(EntityPlayer player, boolean sprinting) {
      if (MyauClient.moduleManager != null) {
         KeepSprintModule keepSprint = (KeepSprintModule)MyauClient.moduleManager.modules.get(KeepSprintModule.class);
         if (!keepSprint.isEnabled() || !keepSprint.shouldPreserveSprint()) {
            player.setSprinting(sprinting);
         }
      }
   }
}
