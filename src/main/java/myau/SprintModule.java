package myau;

import myau.mixin.AccessorEntityLivingBase;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

public class SprintModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private boolean wasSprinting;
   public final BooleanSetting fovFix = new BooleanSetting("fov-fix", true);

   public SprintModule() {
      super("Sprint", true, 0, true);
   }

   public boolean shouldCompensateFov(IAttributeInstance movementAttribute) {
      boolean fixEnabled = this.fovFix.getValue();
      if (!fixEnabled) {
         return false;
      } else {
         AttributeModifier boost = ((AccessorEntityLivingBase)minecraft.thePlayer).getSprintingSpeedBoostModifier();
         return movementAttribute.getModifier(boost.getID()) == null && this.wasSprinting;
      }
   }

   public boolean shouldCompensateFov(boolean sprintModifierPresent) {
      boolean fixEnabled = this.fovFix.getValue();
      return fixEnabled && !sprintModifierPresent && this.wasSprinting;
   }

   @EventHandler
   public void onPreTick(PreTickEvent event) {
      boolean enabled = this.isEnabled();
      if (enabled) {
         InputUtils.setKeyState(minecraft.gameSettings.keyBindSprint.getKeyCode(), true);
      }
   }

   @EventHandler
   public void onPostTick(PostTickEvent event) {
      if (this.isEnabled()) {
         this.wasSprinting = minecraft.thePlayer.isSprinting();
      }
   }

   @Override
   public void onDisable() {
      this.wasSprinting = false;
      InputUtils.updateKeyState(minecraft.gameSettings.keyBindSprint.getKeyCode());
   }
}
