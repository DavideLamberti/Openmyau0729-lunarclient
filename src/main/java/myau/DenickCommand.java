package myau;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

public class DenickCommand extends Command {
   private static final Minecraft minecraft = Minecraft.getMinecraft();

   public DenickCommand() {
      super(new ArrayList<>(Collections.singletonList("denick")));
   }

   @Override
   public void execute(ArrayList<String> arguments) {
      if (arguments.size() < 2) {
         ChatUtils.addFormattedMessage(String.format("%sUsage: .%s <&oname&r>&r", "&7[&bMyau&7] &r", arguments.get(0).toLowerCase(Locale.ROOT)));
      } else {
         String requestedName = arguments.get(1);
         NetworkPlayerInfo playerInfo = minecraft.getNetHandler().getPlayerInfo(ChatColor.formatText(requestedName));
         if (playerInfo == null) {
            ChatUtils.addRawMessage(String.format(ChatColor.formatText("%sNo entity with name &o%s&r"), ChatColor.formatText("&7[&bMyau&7] &r"), requestedName));
         } else {
            GameProfile profile = playerInfo.getGameProfile();
            Property textures = (Property)Iterables.getFirst(profile.getProperties().get("textures"), null);
            if (textures == null) {
               ChatUtils.addRawMessage(
                  String.format(ChatColor.formatText("%sNo textures for entity with name &o%s&r"), ChatColor.formatText("&7[&bMyau&7] &r"), requestedName)
               );
            } else {
               String payload = new String(Base64.getDecoder().decode(textures.getValue().getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
               String profileName = extractJsonLikeField(payload, "profileName");
               String profileId = extractJsonLikeField(payload, "profileId");
               ChatUtils.addRawMessage(
                  String.format(
                     ChatColor.formatText("%s%s&r -> %s (&o%s&r)&r"),
                     ChatColor.formatText("&7[&bMyau&7] &r"),
                     profile.getName().replace("§", "&"),
                     profileName,
                     profileId
                  )
               );
               if (!profileId.isEmpty() && !profileId.equals("?")) {
                  Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(profileId), null);
               }
            }
         }
      }
   }

   private static String extractJsonLikeField(String payload, String fieldName) {
      String marker = fieldName + "\" : \"";
      return !payload.contains(marker) ? "?" : payload.split(marker, 2)[1].split("\"", 2)[0];
   }

   @Override
   public ArrayList<String> completeArguments(ArrayList<String> arguments) {
      return arguments.size() == 2 ? null : new ArrayList<>();
   }
}
