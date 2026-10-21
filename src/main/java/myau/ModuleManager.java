package myau;

import java.util.LinkedHashMap;
import net.minecraft.client.Minecraft;

public class ModuleManager {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private boolean toggleSoundPending;
   public final LinkedHashMap<Class<?>, Module> modules = new LinkedHashMap<>();

   public Module getModule(String name) {
      return this.modules.values().stream().filter(module -> module.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
   }

   @SuppressWarnings("unchecked")
   public <T extends Module> T getModule(Class<T> clazz) {
      return (T) this.modules.get(clazz);
   }

   public boolean toggle(Module module) {
      boolean requestedState = !module.isEnabled();
      module.setEnabled(requestedState);
      return module.isEnabled() == requestedState;
   }

   public void update() {
      if (minecraft.theWorld != null && minecraft.thePlayer != null && minecraft.currentScreen == null) {
         HUDModule hud = (HUDModule)this.modules.get(HUDModule.class);

         for (Module module : this.modules.values()) {
            int keyCode = module.getKeyCode();
            if (keyCode == 0) {
               module.setPreviousKeyState(null);
            } else {
               boolean keyDown = InputUtils.isKeyDown(keyCode);
               MutableValue<Boolean> previousKeyState = module.getPreviousKeyState();
               if (previousKeyState != null && !previousKeyState.value && keyDown && this.toggle(module)) {
                  if (hud != null && hud.toggleAlerts.getValue()) {
                     ChatUtils.addFormattedMessage(String.format("%s%s: %s&r", "&7[&bMyau&7] &r", module.getName(), module.isEnabled() ? "&a&lON" : "&c&lOFF"));
                  }

                  if (hud != null && hud.toggleSounds.getValue()) {
                     this.toggleSoundPending = true;
                  }
               }

               module.setPreviousKeyState(new MutableValue<>(keyDown));
            }
         }

         if (this.toggleSoundPending) {
            this.toggleSoundPending = false;
            SoundUtils.play("random.click");
         }
      } else {
         this.resetKeyStates();
      }
   }

   private void resetKeyStates() {
      for (Module module : this.modules.values()) {
         module.setPreviousKeyState(null);
      }

      this.toggleSoundPending = false;
   }
}
