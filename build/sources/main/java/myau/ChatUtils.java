package myau;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public final class ChatUtils {
   private static final Minecraft minecraft = Minecraft.getMinecraft();

   private ChatUtils() {
   }

   public static void addComponent(IChatComponent component) {
      if (minecraft.thePlayer != null) {
         minecraft.thePlayer.addChatMessage(component);
      }
   }

   public static void addFormattedMessage(String message) {
      addComponent(new ChatComponentText(ChatColor.formatText(message)));
   }

   public static void addRawMessage(String message) {
      addComponent(new ChatComponentText(message));
   }

   public static void sendMessage(String message) {
      if (minecraft.thePlayer != null) {
         minecraft.thePlayer.sendChatMessage(message);
      }
   }
}
