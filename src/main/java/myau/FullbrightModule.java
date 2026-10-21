package myau;

import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class FullbrightModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private float savedGamma = Float.NaN;
   private boolean removeNightVision;
   public final ModeSetting mode = new ModeSetting("mode", 0, new String[]{"GAMMA", "EFFECT"});

   public FullbrightModule() {
      super("Fullbright", true, 0, true);
   }

   @EventHandler
   public void onPostTick(PostTickEvent event) {
      if (this.isEnabled()) {
         int selectedMode = this.mode.getValue();
         switch (selectedMode) {
            case 0:
               minecraft.gameSettings.gammaSetting = 1000.0F;
               break;
            case 1:
               minecraft.thePlayer.addPotionEffect(new PotionEffect(Potion.nightVision.id, 25940, 0));
         }
      }
   }

   @Override
   public void onEnable() {
      switch (this.mode.getValue()) {
         case 0:
            this.savedGamma = minecraft.gameSettings.gammaSetting;
            return;
         case 1:
            this.removeNightVision = true;
      }
   }

   @Override
   public void onDisable() {
      boolean shouldRemoveEffect = Float.isNaN(this.savedGamma);
      if (!shouldRemoveEffect) {
         minecraft.gameSettings.gammaSetting = this.savedGamma;
         this.savedGamma = Float.NaN;
      }

      shouldRemoveEffect = this.removeNightVision;
      if (shouldRemoveEffect) {
         if (minecraft.thePlayer != null) {
            minecraft.thePlayer.removePotionEffectClient(Potion.nightVision.id);
         }

         this.removeNightVision = false;
      }
   }

   @Override
   public void onSettingChanged(String settingName) {
      if (this.isEnabled()) {
         this.onDisable();
         this.onEnable();
      }
   }
}
