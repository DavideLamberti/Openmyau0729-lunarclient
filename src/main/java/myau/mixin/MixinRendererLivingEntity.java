package myau.mixin;

import myau.ChamsModule;
import myau.ESPModule;
import myau.MyauClient;
import myau.NameTagsModule;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SideOnly(Side.CLIENT)
@Mixin(
   value = {RendererLivingEntity.class},
   priority = 991
)
public abstract class MixinRendererLivingEntity<T extends EntityLivingBase> extends Render<T> {
   @Unique
   private boolean chamsPolygonOffsetActive = false;

   protected MixinRendererLivingEntity(RenderManager renderManager) {
      super(renderManager);
   }

   @Inject(
      method = {"doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V"},
      at = {@At("HEAD")}
   )
   private void beforeDoRender(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo callbackInfo) {
      if (MyauClient.moduleManager != null) {
         ChamsModule chams = (ChamsModule)MyauClient.moduleManager.modules.get(ChamsModule.class);
         if (chams.isEnabled() && chams.shouldRenderEntity(entity)) {
            GL11.glEnable(32823);
            GL11.glPolygonOffset(1.0F, -2500000.0F);
            this.chamsPolygonOffsetActive = true;
         }
      }
   }

   @Inject(
      method = {"doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V"},
      at = {@At("RETURN")}
   )
   private void afterDoRender(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo callbackInfo) {
      if (this.chamsPolygonOffsetActive) {
         GL11.glPolygonOffset(1.0F, 2500000.0F);
         GL11.glDisable(32823);
         this.chamsPolygonOffsetActive = false;
      }
   }

   @Inject(
      method = {"canRenderName(Lnet/minecraft/entity/EntityLivingBase;)Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onCanRenderName(T entity, CallbackInfoReturnable<Boolean> callbackInfo) {
      if (MyauClient.moduleManager != null) {
         NameTagsModule nameTags = (NameTagsModule)MyauClient.moduleManager.modules.get(NameTagsModule.class);
         if (nameTags.isEnabled() && nameTags.shouldRenderNameTag(entity)) {
            callbackInfo.setReturnValue(false);
         } else {
            ESPModule eSP = (ESPModule)MyauClient.moduleManager.modules.get(ESPModule.class);
            if (eSP.isEnabled() && !eSP.areNameTagsAllowed()) {
               callbackInfo.setReturnValue(false);
            }
         }
      }
   }
}
