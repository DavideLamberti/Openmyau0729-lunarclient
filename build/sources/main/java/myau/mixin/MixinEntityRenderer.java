package myau.mixin;

import java.util.List;
import myau.AntiDebuffModule;
import myau.BlockReachEvent;
import myau.EntityReachEvent;
import myau.GhostHandModule;
import myau.KillAuraModule;
import myau.MutableValue;
import myau.MyauClient;
import myau.NoHurtCamModule;
import myau.ScaffoldModule;
import myau.ViewClipModule;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SideOnly(Side.CLIENT)
@Mixin({EntityRenderer.class})
public abstract class MixinEntityRenderer {
   @Unique
   private MutableValue<Integer> spoofedHotbarSlot = null;
   @Unique
   private MutableValue<ItemStack> previousItemInUse = null;
   @Unique
   private MutableValue<Integer> previousItemUseCount = null;
   @Shadow
   private Minecraft mc;
   @Shadow
   private float thirdPersonDistance;

   @Inject(
      method = {"updateCameraAndRender"},
      at = {@At("HEAD")}
   )
   private void beforeUpdateCameraAndRender(float partialTicks, long nanoTime, CallbackInfo callbackInfo) {
      if (this.mc.thePlayer != null) {
         ScaffoldModule scaffold = (ScaffoldModule)MyauClient.moduleManager.modules.get(ScaffoldModule.class);
         if (scaffold.isEnabled() && scaffold.itemSpoof.getValue()) {
            int spoofedSlot = scaffold.getPreviousHotbarSlot();
            if (spoofedSlot >= 0) {
               this.spoofedHotbarSlot = new MutableValue<>(this.mc.thePlayer.inventory.currentItem);
               this.mc.thePlayer.inventory.currentItem = spoofedSlot;
            }
         }

         KillAuraModule killAura = (KillAuraModule)MyauClient.moduleManager.modules.get(KillAuraModule.class);
         if (killAura.isEnabled() && killAura.shouldRenderBlocking()) {
            this.previousItemInUse = new MutableValue<>(((AccessorEntityPlayer)this.mc.thePlayer).getItemInUse());
            ((AccessorEntityPlayer)this.mc.thePlayer).setItemInUse(this.mc.thePlayer.inventory.getCurrentItem());
            this.previousItemUseCount = new MutableValue<>(((AccessorEntityPlayer)this.mc.thePlayer).getItemInUseCount());
            ((AccessorEntityPlayer)this.mc.thePlayer).setItemInUseCount(69000);
         }
      }
   }

   @Inject(
      method = {"updateCameraAndRender"},
      at = {@At("RETURN")}
   )
   private void afterUpdateCameraAndRender(float partialTicks, long nanoTime, CallbackInfo callbackInfo) {
      if (this.spoofedHotbarSlot != null) {
         this.mc.thePlayer.inventory.currentItem = this.spoofedHotbarSlot.value;
         this.spoofedHotbarSlot = null;
      }

      if (this.previousItemInUse != null) {
         ((AccessorEntityPlayer)this.mc.thePlayer).setItemInUse(this.previousItemInUse.value);
         this.previousItemInUse = null;
      }

      if (this.previousItemUseCount != null) {
         ((AccessorEntityPlayer)this.mc.thePlayer).setItemInUseCount(this.previousItemUseCount.value);
         this.previousItemUseCount = null;
      }
   }

   @Inject(
      method = {"updateRenderer"},
      at = {@At("HEAD")}
   )
   private void beforeUpdateRenderer(CallbackInfo callbackInfo) {
      ScaffoldModule scaffold = (ScaffoldModule)MyauClient.moduleManager.modules.get(ScaffoldModule.class);
      if (scaffold.isEnabled() && scaffold.itemSpoof.getValue()) {
         int spoofedSlot = scaffold.getPreviousHotbarSlot();
         if (spoofedSlot >= 0) {
            this.spoofedHotbarSlot = new MutableValue<>(this.mc.thePlayer.inventory.currentItem);
            this.mc.thePlayer.inventory.currentItem = spoofedSlot;
         }
      }
   }

   @Inject(
      method = {"updateRenderer"},
      at = {@At("RETURN")}
   )
   private void afterUpdateRenderer(CallbackInfo callbackInfo) {
      if (this.spoofedHotbarSlot != null) {
         this.mc.thePlayer.inventory.currentItem = this.spoofedHotbarSlot.value;
         this.spoofedHotbarSlot = null;
      }
   }

   @ModifyConstant(
      method = {"hurtCameraEffect"},
      constant = {@Constant(
         floatValue = 14.0F,
         ordinal = 0
      )}
   )
   private float modifyHurtCameraStrength(float strength) {
      if (MyauClient.moduleManager == null) {
         return strength;
      } else {
         NoHurtCamModule noHurtCam = (NoHurtCamModule)MyauClient.moduleManager.modules.get(NoHurtCamModule.class);
         return noHurtCam.isEnabled() ? strength * (float)noHurtCam.multiplier.getValue().intValue() / 100.0F : strength;
      }
   }

   @ModifyConstant(
      method = {"getMouseOver"},
      constant = {@Constant(
         doubleValue = 3.0,
         ordinal = 1
      )}
   )
   private double modifyEntityReach(double reachDistance) {
      if (MyauClient.eventBus != null) {
         EntityReachEvent event = new EntityReachEvent(reachDistance);
         MyauClient.eventBus.post(event);
         return event.getReachDistance();
      } else {
         return reachDistance;
      }
   }

   @ModifyVariable(
      method = {"getMouseOver"},
      at = @At("STORE"),
      name = {"d0"}
   )
   private double modifyBlockReach(double reachDistance) {
      if (MyauClient.eventBus != null) {
         BlockReachEvent blockReachEvent = new BlockReachEvent(reachDistance);
         MyauClient.eventBus.post(blockReachEvent);
         return blockReachEvent.getReachDistance();
      } else {
         return reachDistance;
      }
   }

   @Redirect(
      method = {"getMouseOver"},
      at = @At(
         value = "INVOKE",
         target = "Ljava/util/List;size()I",
         ordinal = 0,
         remap = false
      )
   )
   private int filterMouseOverEntities(List<Entity> entities) {
      if (MyauClient.moduleManager != null) {
         GhostHandModule ghostHand = (GhostHandModule)MyauClient.moduleManager.modules.get(GhostHandModule.class);
         if (ghostHand.isEnabled()) {
            entities.removeIf(ghostHand::shouldIgnoreEntity);
         }
      }

      return entities.size();
   }

   @Redirect(
      method = {"orientCamera"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/util/Vec3;distanceTo(Lnet/minecraft/util/Vec3;)D"
      )
   )
   private double redirectOrientCamera(Vec3 cameraPosition, Vec3 collisionPosition) {
      if (MyauClient.moduleManager == null) {
         return cameraPosition.distanceTo(collisionPosition);
      } else {
         ViewClipModule viewClip = (ViewClipModule)MyauClient.moduleManager.modules.get(ViewClipModule.class);
         return viewClip.isEnabled() ? (double)this.thirdPersonDistance : cameraPosition.distanceTo(collisionPosition);
      }
   }

   @Redirect(
      method = {"setupFog"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/block/Block;getMaterial()Lnet/minecraft/block/material/Material;"
      )
   )
   private Material redirectCameraBlockMaterial(Block block) {
      if (MyauClient.moduleManager == null) {
         return block.getMaterial();
      } else {
         ViewClipModule viewClip = (ViewClipModule)MyauClient.moduleManager.modules.get(ViewClipModule.class);
         return viewClip.isEnabled() ? Material.air : block.getMaterial();
      }
   }

   @Redirect(
      method = {"updateFogColor"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/EntityLivingBase;isPotionActive(Lnet/minecraft/potion/Potion;)Z"
      )
   )
   private boolean redirectBlindnessForFogColor(EntityLivingBase target, Potion potion) {
      if (potion == Potion.blindness && MyauClient.moduleManager != null) {
         AntiDebuffModule antiDebuff = (AntiDebuffModule)MyauClient.moduleManager.modules.get(AntiDebuffModule.class);
         if (antiDebuff.isEnabled() && antiDebuff.blindness.getValue()) {
            return false;
         }
      }

      return ((AccessorEntityLivingBase)target).getActivePotionsMap().containsKey(potion.id);
   }

   @Redirect(
      method = {"setupFog"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/EntityLivingBase;isPotionActive(Lnet/minecraft/potion/Potion;)Z"
      )
   )
   private boolean redirectBlindnessForFogSetup(EntityLivingBase livingEntity, Potion potion) {
      if (potion == Potion.blindness && MyauClient.moduleManager != null) {
         AntiDebuffModule antiDebuff = (AntiDebuffModule)MyauClient.moduleManager.modules.get(AntiDebuffModule.class);
         if (antiDebuff.isEnabled() && antiDebuff.blindness.getValue()) {
            return false;
         }
      }

      return ((AccessorEntityLivingBase)livingEntity).getActivePotionsMap().containsKey(potion.id);
   }

   @Redirect(
      method = {"setupCameraTransform"},
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
