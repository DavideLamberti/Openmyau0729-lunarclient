package myau.mixin;

import myau.MyauClient;
import myau.NickHiderModule;
import myau.RenderOverlayEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SideOnly(Side.CLIENT)
@Mixin(
   value = {GuiIngameForge.class},
   remap = false
)
public abstract class MixinGuiIngameForge {
   @Inject(
      method = {"renderGameOverlay(F)V", "func_175180_a(F)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraftforge/client/GuiIngameForge;renderTitle(IIF)V",
         shift = At.Shift.AFTER,
         remap = false
      )},
      remap = false
   )
   private void afterRenderTitle(float partialTicks, CallbackInfo callbackInfo) {
      if (MyauClient.eventBus != null) {
         MyauClient.eventBus.post(new RenderOverlayEvent(partialTicks));
      }
   }

   @Redirect(
      method = {"renderExperience(II)V"},
      at = @At(
         value = "FIELD",
         target = "Lnet/minecraft/client/entity/EntityPlayerSP;experience:F",
         remap = true
      ),
      remap = false
   )
   private float redirectRenderExperience(EntityPlayerSP player) {
      if (MyauClient.moduleManager == null) {
         return player.experience;
      } else {
         NickHiderModule nickHider = (NickHiderModule)MyauClient.moduleManager.modules.get(NickHiderModule.class);
         return nickHider.isEnabled() && nickHider.level.getValue() ? 0.0F : player.experience;
      }
   }

   @Redirect(
      method = {"renderExperience(II)V"},
      at = @At(
         value = "FIELD",
         target = "Lnet/minecraft/client/entity/EntityPlayerSP;experienceLevel:I",
         remap = true
      ),
      remap = false
   )
   private int redirectExperienceLevel(EntityPlayerSP player) {
      if (MyauClient.moduleManager == null) {
         return player.experienceLevel;
      } else {
         NickHiderModule nickHider = (NickHiderModule)MyauClient.moduleManager.modules.get(NickHiderModule.class);
         return nickHider.isEnabled() && nickHider.level.getValue() ? 0 : player.experienceLevel;
      }
   }
}
