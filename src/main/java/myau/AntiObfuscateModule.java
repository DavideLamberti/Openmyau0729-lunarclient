package myau;

public class AntiObfuscateModule extends Module {
   public AntiObfuscateModule() {
      super("AntiObfuscate", false, 0, true);
   }

   public String formatText(String text) {
      return text.replaceAll("§k", "");
   }
}
