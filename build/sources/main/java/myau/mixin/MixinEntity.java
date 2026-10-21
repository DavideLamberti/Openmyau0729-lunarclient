package myau.mixin;

import myau.MyauClient;
import myau.SafeWalkEvent;
import myau.VelocityEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SideOnly(Side.CLIENT)
@Mixin({Entity.class})
public abstract class MixinEntity {
   @Shadow
   public World worldObj;
   @Shadow
   public double posX;
   @Shadow
   public double posY;
   @Shadow
   public double posZ;
   @Shadow
   public double motionX;
   @Shadow
   public double motionY;
   @Shadow
   public double motionZ;
   @Shadow
   public float rotationYaw;
   @Shadow
   public float rotationPitch;
   @Shadow
   public float prevRotationYaw;
   @Shadow
   public float prevRotationPitch;
   @Shadow
   public boolean onGround;

   @Inject(
      method = {"setVelocity"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void beforeSetVelocity(double velocityX, double velocityY, double velocityZ, CallbackInfo callbackInfo) {
      if ((Object)this instanceof EntityPlayerSP && MyauClient.eventBus != null) {
         VelocityEvent event = new VelocityEvent(velocityX, velocityY, velocityZ);
         MyauClient.eventBus.post(event);
         if (event.isCancelled()) {
            callbackInfo.cancel();
            this.motionX = event.getVelocityX();
            this.motionY = event.getVelocityY();
            this.motionZ = event.getVelocityZ();
         }
      }
   }

   @Inject(
      method = {"setAngles"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void beforeSetAngles(CallbackInfo callbackInfo) {
      if ((Object)this instanceof EntityPlayerSP && MyauClient.rotationSmoother != null && MyauClient.rotationSmoother.isRequestActive()) {
         callbackInfo.cancel();
      }
   }

   @ModifyVariable(
      method = {"moveEntity"},
      ordinal = 0,
      at = @At("STORE"),
      name = {"flag"}
   )
   private boolean modifySafeWalkFlag(boolean safeWalk) {
      if ((Object)this instanceof EntityPlayerSP && MyauClient.eventBus != null) {
         SafeWalkEvent safeWalkEvent = new SafeWalkEvent(safeWalk);
         MyauClient.eventBus.post(safeWalkEvent);
         return safeWalkEvent.isSafeWalk();
      } else {
         return safeWalk;
      }
   }
}
