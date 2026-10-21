package myau;

import java.util.regex.Matcher;
import net.minecraft.client.Minecraft;

public class NickHiderModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   public final StringSetting nameSetting = new StringSetting("name", "You");
   public final BooleanSetting scoreboard = new BooleanSetting("scoreboard", true);
   public final BooleanSetting level = new BooleanSetting("level", true);

   public NickHiderModule() {
      super("NickHider", false, 0, true);
   }

   public String formatText(String text) {
      if (text != null && minecraft.thePlayer != null) {
         if (this.scoreboard.getValue() && text.matches("§7\\d{2}/\\d{2}/\\d{2}(?:\\d{2})?  ?§8.*")) {
            text = text.replaceAll("§8", "§8§k").replaceAll("[^\\x00-\\x7F§]", "?");
         }

         return text.replaceAll(minecraft.thePlayer.getName(), Matcher.quoteReplacement(ChatColor.formatText(this.nameSetting.getValue())));
      } else {
         return text;
      }
   }
}
