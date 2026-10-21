package myau;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import myau.mixin.AccessorMinecraft;
import net.minecraft.client.Minecraft;

public class ConfigProfile {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
   private final String profileName;
   private final File file;
   public static String activeProfileName;

   public ConfigProfile(String name) {
      this.profileName = name != null && !name.isEmpty() && !name.equals("!") ? name : "predac1";
      this.file = new File("./config/Myau/", String.format("%s.json", this.profileName));

      try {
         Files.createDirectories(this.file.toPath().getParent());
      } catch (Exception var3) {
         ((AccessorMinecraft)minecraft).getLogger().error(var3.getMessage());
      }
   }

   public String fileName() {
      return this.file.getName();
   }

   public boolean load() {
      try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(this.file.toPath()), StandardCharsets.UTF_8)) {
         JsonObject config = new JsonParser().parse(reader).getAsJsonObject();

         for (Module module : MyauClient.moduleManager.modules.values()) {
            List<Setting<?>> settings = MyauClient.settingManager.settingsByModule.get(module.getClass());
            JsonElement entry = config.get(module.getName());
            if (entry == null) {
               resetModule(module, settings);
            } else {
               JsonObject state = entry.getAsJsonObject();
               if (settings != null) {
                  for (Setting<?> setting : settings) {
                     try {
                        if (!setting.readJson(state)) {
                           throw new IllegalArgumentException("Invalid setting value");
                        }
                     } catch (Exception var27) {
                        setting.reset();
                     }
                  }
               }

               try {
                  module.setHidden(state.get("hidden").getAsBoolean());
               } catch (Exception var26) {
                  module.resetHidden();
               }

               try {
                  module.setKeyCode(state.get("key").getAsInt());
               } catch (Exception var25) {
                  module.resetKeyCode();
               }

               try {
                  module.setEnabled(state.get("toggled").getAsBoolean());
               } catch (Exception var24) {
                  module.resetEnabled();
               }
            }
         }

         activeProfileName = this.profileName;
         return true;
      } catch (Exception var30) {
         return false;
      }
   }

   public boolean save() {
      try {
         JsonObject config = new JsonObject();

         for (Module module : MyauClient.moduleManager.modules.values()) {
            JsonObject state = new JsonObject();
            state.addProperty("toggled", module.isEnabled());
            state.addProperty("key", module.getKeyCode());
            state.addProperty("hidden", module.isHidden());
            List<Setting<?>> settings = MyauClient.settingManager.settingsByModule.get(module.getClass());
            if (settings != null) {
               for (Setting<?> setting : settings) {
                  setting.writeJson(state);
               }
            }

            config.add(module.getName(), state);
         }

         try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(this.file.toPath()), StandardCharsets.UTF_8)) {
            GSON.toJson((JsonElement)config, writer);
         }

         activeProfileName = this.profileName;
         return true;
      } catch (Exception var18) {
         return false;
      }
   }

   private static void resetModule(Module module, List<Setting<?>> settings) {
      if (settings != null) {
         for (Setting<?> setting : settings) {
            setting.reset();
         }
      }

      module.resetHidden();
      module.resetKeyCode();
      module.resetEnabled();
   }

   /**
    * Resets all modules to their hardcoded default values (from the Setting/Module constructors),
    * without reading any file. Used on first launch so the bundled preset is not applied.
    */
   public static void resetToDefaults() {
      for (Module module : MyauClient.moduleManager.modules.values()) {
         List<Setting<?>> settings = MyauClient.settingManager.settingsByModule.get(module.getClass());
         resetModule(module, settings);
      }
   }
}
