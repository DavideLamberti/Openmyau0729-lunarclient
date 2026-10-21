package myau.mixin;

import myau.JesusModule;
import myau.MoveFlyingEvent;
import myau.MyauClient;
import myau.RotationState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@SideOnly(Side.CLIENT)
@Mixin({EntityLivingBase.class})
public abstract class MixinEntityLivingBase extends MixinEntity {
   @ModifyVariable(
      method = {"jump"},
      at = @At("STORE"),
      ordinal = 0
   )
   private float modifyJumpYawRadians(float yawRadians) {
      return (Object)this instanceof EntityPlayerSP && RotationState.isActive() ? RotationState.movementYaw() * (float) (Math.PI / 180.0) : yawRadians;
   }

   @Redirect(
      method = {"moveEntityWithHeading"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/EntityLivingBase;moveFlying(FFF)V"
      )
   )
   private void redirectMoveFlying(EntityLivingBase target, float strafe, float forward, float friction) {
      if ((Object)this instanceof EntityPlayerSP && MyauClient.eventBus != null) {
         MoveFlyingEvent event = new MoveFlyingEvent(strafe, forward, friction);
         MyauClient.eventBus.post(event);
         strafe = event.getStrafe();
         forward = event.getForward();
         friction = event.getFriction();
         boolean rotationSpoofActive = RotationState.isActive();
         float yaw = this.rotationYaw;
         if (rotationSpoofActive) {
            this.rotationYaw = RotationState.movementYaw();
         }

         target.moveFlying(strafe, forward, friction);
         if (rotationSpoofActive) {
            this.rotationYaw = yaw;
         }
      } else {
         target.moveFlying(strafe, forward, friction);
      }
   }

   @ModifyVariable(
      method = {"moveEntityWithHeading"},
      name = {"f3"},
      at = @At("STORE")
   )
   private float modifyDepthStriderLevel(float depthStriderLevel) {
      if ((Object)this instanceof EntityPlayerSP && depthStriderLevel == (float)EnchantmentHelper.getDepthStriderModifier((EntityLivingBase)(Object)this)) {
         if (MyauClient.moduleManager == null) {
            return depthStriderLevel;
         }

         JesusModule jesus = (JesusModule)MyauClient.moduleManager.modules.get(JesusModule.class);
         if (jesus.isEnabled() && (!jesus.groundOnly.getValue() || this.onGround)) {
            return Math.max(depthStriderLevel, jesus.speed.getValue());
         }
      }

      return depthStriderLevel;
   }
}
