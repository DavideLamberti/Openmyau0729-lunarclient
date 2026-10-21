package myau;

import java.util.ArrayList;
import java.util.Arrays;

public class HelpCommand extends Command {
   public HelpCommand() {
      super(new ArrayList<>(Arrays.asList("help", "commands")));
   }

   @Override
   public void execute(ArrayList<String> arguments) {
      if (!MyauClient.moduleManager.modules.isEmpty()) {
         ChatUtils.addFormattedMessage(String.format("%sCommands:&r", "&7[&bMyau&7] &r"));

         for (Command command : MyauClient.commandManager.commands) {
            if (!(command instanceof ModuleSettingsCommand)) {
               ChatUtils.addFormattedMessage(String.format("&7»&r .%s&r", String.join(" &7/&r .", command.aliases)));
            }
         }
      }
   }
}
