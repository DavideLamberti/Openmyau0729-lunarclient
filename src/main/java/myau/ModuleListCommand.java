package myau;

import java.util.ArrayList;
import java.util.Arrays;

public class ModuleListCommand extends Command {
   public ModuleListCommand() {
      super(new ArrayList<>(Arrays.asList("list", "l", "modules", "myau")));
   }

   @Override
   public void execute(ArrayList<String> arguments) {
      if (!MyauClient.moduleManager.modules.isEmpty()) {
         ChatUtils.addFormattedMessage(String.format("%sModules:&r", "&7[&bMyau&7] &r"));

         for (Module module : MyauClient.moduleManager.modules.values()) {
            ChatUtils.addFormattedMessage(String.format("%s»&r %s&r", module.isHidden() ? "&8" : "&7", module.getDisplayText()));
         }
      }
   }
}
