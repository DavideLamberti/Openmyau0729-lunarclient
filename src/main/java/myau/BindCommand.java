package myau;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class BindCommand extends Command {
   public BindCommand() {
      super(new ArrayList<>(Arrays.asList("bind", "b")));
   }

   @Override
   public void execute(ArrayList<String> arguments) {
      if (arguments.size() < 3) {
         if (arguments.size() == 2 && (arguments.get(1).equalsIgnoreCase("list") || arguments.get(1).equalsIgnoreCase("l"))) {
            List<Module> boundModules = MyauClient.moduleManager
               .modules
               .values()
               .stream()
               .filter(modulex -> modulex.getKeyCode() != 0)
               .collect(Collectors.toList());
            if (boundModules.isEmpty()) {
               ChatUtils.addFormattedMessage(String.format("%sNo binds&r", "&7[&bMyau&7] &r"));
            } else {
               ChatUtils.addFormattedMessage(String.format("%sBinds:&r", "&7[&bMyau&7] &r"));

               for (Module module : boundModules) {
                  ChatUtils.addFormattedMessage(String.format("%s»&r %s&r", module.isHidden() ? "&8" : "&7", module.getDisplayText()));
               }
            }
         } else {
            String command = arguments.get(0).toLowerCase(Locale.ROOT);
            ChatUtils.addFormattedMessage(String.format("%sUsage: .%s <&omodule&r> <&okey&r>&r | .%s &olist&r", "&7[&bMyau&7] &r", command, command));
         }
      } else {
         int keyCode = Keyboard.getKeyIndex(arguments.get(2).toUpperCase());
         if (keyCode == 0) {
            int mouseButton = Mouse.getButtonIndex(arguments.get(2).toUpperCase());
            if (mouseButton != -1) {
               keyCode = mouseButton - 100;
            }
         }

         String moduleName = arguments.get(1);
         if (!moduleName.equals("*")) {
            Module module = MyauClient.moduleManager.getModule(moduleName);
            if (module == null) {
               ChatUtils.addFormattedMessage(String.format("%sModule not found (&o%s&r)&r", "&7[&bMyau&7] &r", moduleName));
            } else {
               module.setKeyCode(keyCode);
               ChatUtils.addFormattedMessage(String.format("%sBound &o%s&r to &l[%s]&r", "&7[&bMyau&7] &r", module.getName(), InputUtils.getKeyName(keyCode)));
            }
         } else {
            for (Module module : MyauClient.moduleManager.modules.values()) {
               module.setKeyCode(keyCode);
            }

            ChatUtils.addFormattedMessage(String.format("%sBound all modules to &l[%s]&r", "&7[&bMyau&7] &r", InputUtils.getKeyName(keyCode)));
         }
      }
   }

   @Override
   public ArrayList<String> completeArguments(ArrayList<String> arguments) {
      if (arguments.size() == 2) {
         ArrayList<String> completions = MyauClient.moduleManager
            .modules
            .values()
            .stream()
            .map(module -> module.getName().toLowerCase(Locale.ROOT))
            .collect(Collectors.toCollection(ArrayList::new));
         completions.add("list");
         completions.add("l");
         return completions;
      } else if (arguments.size() != 3) {
         return new ArrayList<>();
      } else {
         String moduleName = arguments.get(1);
         if (!moduleName.equals("*") && MyauClient.moduleManager.getModule(moduleName) == null) {
            return new ArrayList<>();
         } else {
            ArrayList<String> completions = new ArrayList<>();

            for (int keyCode = 0; keyCode < 256; keyCode++) {
               String keyName = Keyboard.getKeyName(keyCode);
               if (keyName != null) {
                  completions.add(keyName.toLowerCase(Locale.ROOT));
               }
            }

            for (int button = 0; button < Mouse.getButtonCount(); button++) {
               String buttonName = Mouse.getButtonName(button);
               if (buttonName != null) {
                  completions.add(buttonName.toLowerCase(Locale.ROOT));
               }
            }

            return completions;
         }
      }
   }
}
