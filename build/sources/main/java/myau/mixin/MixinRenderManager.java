package myau.mixin;

import myau.RotationState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SideOnly(Side.CLIENT)
@Mixin({RenderManager.class})
public abstract class MixinRenderManager {
   @Unique
   private float originalPrevRenderYawOffset;
   @Unique
   private float originalRenderYawOffset;
   @Unique
   private float originalPrevYawHead;
   @Unique
   private float originalYawHead;
   @Unique
   private float originalPrevPitch;
   @Unique
   private float originalPitch;

   @Inject(
      method = {"renderEntityStatic"},
      at = {@At("HEAD")}
   )
   private void beforeRenderEntityStatic(Entity entity, float partialTicks, boolean hideDebugBox, CallbackInfoReturnable<Boolean> callbackInfo) {
      if (entity instanceof EntityPlayerSP && RotationState.isActiveWithinTicks(1)) {
         EntityPlayerSP player = (EntityPlayerSP)entity;
         this.originalPrevRenderYawOffset = player.prevRenderYawOffset;
         this.originalRenderYawOffset = player.renderYawOffset;
         this.originalPrevYawHead = player.prevRotationYawHead;
         this.originalYawHead = player.rotationYawHead;
         this.originalPrevPitch = player.prevRotationPitch;
         this.originalPitch = player.rotationPitch;
         player.prevRenderYawOffset = RotationState.previousBodyYaw();
         player.renderYawOffset = RotationState.bodyYaw();
         player.prevRotationYawHead = RotationState.previousHeadYaw();
         player.rotationYawHead = RotationState.headYaw();
         player.prevRotationPitch = RotationState.previousPitch();
         player.rotationPitch = RotationState.pitch();
      }
   }

   @Inject(
      method = {"renderEntityStatic"},
      at = {@At("RETURN")}
   )
   private void afterRenderEntityStatic(Entity entity, float partialTicks, boolean hideDebugBox, CallbackInfoReturnable<Boolean> callbackInfo) {
      if (entity instanceof EntityPlayerSP && RotationState.isActiveWithinTicks(1)) {
         EntityPlayerSP player = (EntityPlayerSP)entity;
         player.prevRenderYawOffset = this.originalPrevRenderYawOffset;
         player.renderYawOffset = this.originalRenderYawOffset;
         player.prevRotationYawHead = this.originalPrevYawHead;
         player.rotationYawHead = this.originalYawHead;
         player.prevRotationPitch = this.originalPrevPitch;
         player.rotationPitch = this.originalPitch;
      }
   }
}
