package myau;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class SettingManager {
   public final LinkedHashMap<Class<?>, ArrayList<Setting<?>>> settingsByModule = new LinkedHashMap<>();

   public Setting<?> findSetting(Module module, String name) {
      ArrayList<Setting<?>> settings = this.settingsByModule.get(module.getClass());
      if (settings == null) {
         return null;
      } else {
         String normalizedName = name.replace("-", "");
         return settings.stream().filter(setting -> normalizedName.equalsIgnoreCase(setting.getName().replace("-", ""))).findFirst().orElse(null);
      }
   }
}
