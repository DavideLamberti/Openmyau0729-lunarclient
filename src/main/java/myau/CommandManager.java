package myau;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import myau.mixin.AccessorC14PacketTabComplete;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C14PacketTabComplete;

public class CommandManager {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private static final char COMMAND_PREFIX = '.';
   private String[] pendingCompletions;
   public final ArrayList<Command> commands = new ArrayList<>();

   private void executeCommand(String message) {
      ArrayList<String> arguments = new ArrayList<>(Arrays.asList(message.substring(1).trim().split("\\s+")));
      if (arguments.isEmpty()) {
         ChatUtils.addFormattedMessage(String.format("%sUnknown command&r", "&7[&bMyau&7] &r"));
      } else {
         String commandName = arguments.get(0).toLowerCase(Locale.ROOT);
         if (commandName.isEmpty()) {
            ChatUtils.addFormattedMessage(String.format("%sUnknown command&r", "&7[&bMyau&7] &r"));
         } else {
            for (Command command : this.commands) {
               for (String alias : command.aliases) {
                  if (commandName.equals(alias)) {
                     command.execute(arguments);
                     return;
                  }
               }
            }

            ChatUtils.addFormattedMessage(String.format("%sUnknown command (&o%s&r)&r", "&7[&bMyau&7] &r", commandName));
         }
      }
   }

   private String[] completeCommand(String message) {
      ArrayList<String> arguments = new ArrayList<>(Arrays.asList(message.substring(1).split("\\s+", -1)));
      String commandName = arguments.get(0).toLowerCase(Locale.ROOT);
      if (arguments.size() == 1) {
         ArrayList<String> matches = new ArrayList<>();

         for (Command command : this.commands) {
            for (String alias : command.aliases) {
               if (alias.startsWith(commandName)) {
                  matches.add(String.format(".%s", alias));
               }
            }
         }

         return matches.toArray(new String[0]);
      } else {
         for (Command command : this.commands) {
            for (String aliasx : command.aliases) {
               if (commandName.equals(aliasx)) {
                  ArrayList<String> completions = command.completeArguments(arguments);
                  if (completions == null) {
                     return null;
                  }

                  String prefix = arguments.get(arguments.size() - 1).toLowerCase(Locale.ROOT);
                  return completions.stream().filter(completion -> completion.toLowerCase(Locale.ROOT).startsWith(prefix)).toArray(String[]::new);
               }
            }
         }

         return new String[0];
      }
   }

   public boolean isClientCommand(String message) {
      return message != null && message.length() >= 2 && message.charAt(0) == '.' && Character.isLetterOrDigit(message.charAt(1));
   }

   @EventHandler(
      priority = 150
   )
   public void onPacketSend(PacketSendEvent event) {
      if (!event.isCancelled()) {
         if (event.getPacket() instanceof C01PacketChatMessage) {
            C01PacketChatMessage packet = (C01PacketChatMessage)event.getPacket();
            String message = packet.getMessage();
            if (this.isClientCommand(message)) {
               event.setCancelled(true);
               this.executeCommand(message);
            }
         } else if (event.getPacket() instanceof C14PacketTabComplete) {
            C14PacketTabComplete packet = (C14PacketTabComplete)event.getPacket();
            String message = packet.getMessage();
            if (this.isClientCommand(message)) {
               this.pendingCompletions = this.completeCommand(message);
               if (this.pendingCompletions != null) {
                  event.setCancelled(true);
               } else {
                  String[] words = message.split("\\s+", -1);
                  String lastWord = words[words.length - 1];
                  ((AccessorC14PacketTabComplete)packet).setMessage(lastWord.isEmpty() ? " " : lastWord);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = -150
   )
   public void onPostTick(PostTickEvent event) {
      if (this.pendingCompletions != null) {
         try {
            if (minecraft.currentScreen instanceof GuiChat) {
               ((GuiChat)minecraft.currentScreen).onAutocompleteResponse(this.pendingCompletions);
            }
         } catch (Exception var6) {
         } finally {
            this.pendingCompletions = null;
         }
      }
   }
}
