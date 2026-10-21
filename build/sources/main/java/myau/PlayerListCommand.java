package myau;

import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

public class PlayerListCommand extends Command {
   private static final Minecraft minecraft = Minecraft.getMinecraft();

   public PlayerListCommand() {
      super(new ArrayList<>(Arrays.asList("playerlist", "players")));
   }

   @Override
   public void execute(ArrayList<String> arguments) {
      ArrayList<String> players = new ArrayList<>();

      for (NetworkPlayerInfo playerInfo : minecraft.getNetHandler().getPlayerInfoMap()) {
         players.add(playerInfo.getGameProfile().getName().replace("§", "&"));
      }

      if (players.isEmpty()) {
         ChatUtils.addFormattedMessage(String.format("%sNo players&r", "&7[&bMyau&7] &r"));
      } else {
         ChatUtils.addRawMessage(String.format(ChatColor.formatText("%sPlayers:&r %s"), ChatColor.formatText("&7[&bMyau&7] &r"), String.join(", ", players)));
      }
   }
}
