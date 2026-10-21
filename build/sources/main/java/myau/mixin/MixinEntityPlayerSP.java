package myau.mixin;

import myau.AntiDebuffModule;
import myau.KillAuraModule;
import myau.LivingUpdateEvent;
import myau.MovementInputEvent;
import myau.MyauClient;
import myau.NoSlowModule;
import myau.PostPlayerUpdateEvent;
import myau.PreWalkingUpdateEvent;
import myau.RotationState;
import myau.RotationUpdateEvent;
import myau.RotationUtils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SideOnly(Side.CLIENT)
@Mixin({EntityPlayerSP.class})
public abstract class MixinEntityPlayerSP extends MixinEntityPlayer {
   @Unique
   private float spoofedYaw = Float.NaN;
   @Unique
   private float spoofedPitch = Float.NaN;
   @Unique
   private float originalYaw;
   @Unique
   private float originalPitch;
   @Shadow
   private float lastReportedYaw;
   @Shadow
   private float lastReportedPitch;
   @Shadow
   public float renderArmYaw;
   @Shadow
   public float prevRenderArmYaw;

   @Inject(
      method = {"onUpdate"},
      at = {@At("HEAD")}
   )
   private void beforePlayerUpdate(CallbackInfo callbackInfo) {
      if (this.worldObj.isBlockLoaded(new BlockPos(this.posX, 0.0, this.posZ)) && MyauClient.eventBus != null) {
         RotationUpdateEvent event = new RotationUpdateEvent(this.lastReportedYaw, this.lastReportedPitch, this.rotationYaw, this.rotationPitch);
         MyauClient.eventBus.post(event);
         RotationState.update(
            event.isModified() && !((Entity)(Object)this).isRiding(), event.getYaw(), event.getPitch(), event.getMovementYaw(), event.getMovementPriority()
         );
         if (event.isModified()) {
            this.originalYaw = this.rotationYaw;
            this.originalPitch = this.rotationPitch;
            this.spoofedYaw = event.getYaw();
            this.spoofedPitch = event.getPitch();
         } else {
            this.originalYaw = Float.NaN;
            this.originalPitch = Float.NaN;
            this.spoofedYaw = Float.NaN;
            this.spoofedPitch = Float.NaN;
         }
      }
   }

   @Inject(
      method = {"onUpdate"},
      at = {@At("RETURN")}
   )
   private void afterPlayerUpdate(CallbackInfo callbackInfo) {
      if (this.worldObj.isBlockLoaded(new BlockPos(this.posX, 0.0, this.posZ))) {
         if (!Float.isNaN(this.originalYaw) && !Float.isNaN(this.originalPitch)) {
            this.lastReportedYaw = this.rotationYaw;
            this.lastReportedPitch = this.rotationPitch;
            this.rotationYaw = RotationUtils.nearestEquivalentAngle(this.originalYaw, this.rotationYaw);
            this.rotationPitch = this.originalPitch;
            this.prevRotationYaw = this.rotationYaw;
            this.prevRotationPitch = this.rotationPitch;
            this.prevRenderArmYaw = this.rotationYaw - (this.renderArmYaw - this.prevRenderArmYaw) * 2.0F;
            this.renderArmYaw = this.rotationYaw;
         }

         if (MyauClient.eventBus != null) {
            MyauClient.eventBus.post(new PostPlayerUpdateEvent());
         }
      }
   }

   @Redirect(
      method = {"onUpdate"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/entity/EntityPlayerSP;isRiding()Z"
      )
   )
   private boolean applySpoofedRotationBeforeRidingCheck(EntityPlayerSP player) {
      if (!Float.isNaN(this.spoofedYaw) && !Float.isNaN(this.spoofedPitch)) {
         this.rotationYaw = this.spoofedYaw;
         this.rotationPitch = this.spoofedPitch;
      }

      return player.isRiding();
   }

   @Inject(
      method = {"onUpdate"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/entity/EntityPlayerSP;onUpdateWalkingPlayer()V"
      )}
   )
   private void beforeWalkingPlayerUpdate(CallbackInfo callbackInfo) {
      if (MyauClient.eventBus != null) {
         MyauClient.eventBus.post(new PreWalkingUpdateEvent());
      }
   }

   @Inject(
      method = {"onLivingUpdate"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/entity/AbstractClientPlayer;onLivingUpdate()V"
      )}
   )
   private void beforeLivingUpdate(CallbackInfo callbackInfo) {
      if (MyauClient.eventBus != null) {
         MyauClient.eventBus.post(new LivingUpdateEvent());
      }
   }

   @Inject(
      method = {"onLivingUpdate"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/util/MovementInput;updatePlayerMoveState()V",
         shift = At.Shift.AFTER
      )}
   )
   private void afterMovementInputUpdate(CallbackInfo callbackInfo) {
      if (MyauClient.eventBus != null) {
         MyauClient.eventBus.post(new MovementInputEvent());
      }
   }

   @Redirect(
      method = {"onLivingUpdate"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/entity/EntityPlayerSP;isUsingItem()Z"
      )
   )
   private boolean redirectUsingItemForNoSlow(EntityPlayerSP player) {
      KillAuraModule killAura = (KillAuraModule)MyauClient.moduleManager.modules.get(KillAuraModule.class);
      if (killAura.isAutoBlockActive() && killAura.autoBlockNoSlow.getValue()) {
         return false;
      } else {
         NoSlowModule noSlow = (NoSlowModule)MyauClient.moduleManager.modules.get(NoSlowModule.class);
         return noSlow.isEnabled() && noSlow.shouldSuppressItemSlowdown() ? false : player.isUsingItem();
      }
   }

   @Redirect(
      method = {"onLivingUpdate"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/entity/EntityPlayerSP;isPotionActive(Lnet/minecraft/potion/Potion;)Z"
      )
   )
   private boolean redirectNauseaCheck(EntityPlayerSP player, Potion potion) {
      if (potion == Potion.confusion && MyauClient.moduleManager != null) {
         AntiDebuffModule antiDebuff = (AntiDebuffModule)MyauClient.moduleManager.modules.get(AntiDebuffModule.class);
         if (antiDebuff.isEnabled() && antiDebuff.nausea.getValue()) {
            return false;
         }
      }

      return ((AccessorEntityLivingBase)player).getActivePotionsMap().containsKey(potion.id);
   }
}
