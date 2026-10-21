package myau;

import java.util.ArrayList;
import java.util.Locale;

public abstract class Command {
   public final ArrayList<String> aliases;

   public Command(ArrayList<String> list) {
      this.aliases = new ArrayList<>(list.size());

      for (String text : list) {
         this.aliases.add(text.toLowerCase(Locale.ROOT));
      }
   }

   public abstract void execute(ArrayList<String> var1);

   public ArrayList<String> completeArguments(ArrayList<String> arguments) {
      return new ArrayList<>();
   }
}
