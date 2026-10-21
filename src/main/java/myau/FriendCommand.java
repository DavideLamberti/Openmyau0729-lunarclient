package myau;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class FriendCommand extends Command {
   public FriendCommand() {
      super(new ArrayList<>(Arrays.asList("friend", "f")));
   }

   @Override
   public void execute(ArrayList<String> arguments) {
      if (arguments.size() < 2) {
         this.showUsage(arguments.get(0));
      } else {
         String var2 = arguments.get(1).toLowerCase(Locale.ROOT);
         switch (var2) {
            case "add":
               if (arguments.size() < 3) {
                  this.showNameUsage(arguments.get(0), "add");
                  return;
               }

               String addedName = MyauClient.friendList.add(arguments.get(2));
               if (addedName == null) {
                  ChatUtils.addFormattedMessage(String.format("%s&o%s&r is already in your friend list&r", "&7[&bMyau&7] &r", arguments.get(2)));
               } else {
                  ChatUtils.addFormattedMessage(String.format("%sAdded &o%s&r to your friend list&r", "&7[&bMyau&7] &r", addedName));
               }

               return;
            case "remove":
               if (arguments.size() < 3) {
                  this.showNameUsage(arguments.get(0), "remove");
                  return;
               }

               String removedName = MyauClient.friendList.remove(arguments.get(2));
               if (removedName == null) {
                  ChatUtils.addFormattedMessage(String.format("%s&o%s&r is not in your friend list&r", "&7[&bMyau&7] &r", arguments.get(2)));
               } else {
                  ChatUtils.addFormattedMessage(String.format("%sRemoved &o%s&r from your friend list&r", "&7[&bMyau&7] &r", removedName));
               }

               return;
            case "list":
               ArrayList<String> entries = MyauClient.friendList.entries();
               if (entries.isEmpty()) {
                  ChatUtils.addFormattedMessage(String.format("%sNo friends&r", "&7[&bMyau&7] &r"));
                  return;
               }

               ChatUtils.addFormattedMessage(String.format("%sFriends:&r", "&7[&bMyau&7] &r"));

               for (String entry : entries) {
                  ChatUtils.addRawMessage(String.format(ChatColor.formatText("   &o%s&r"), entry));
               }

               return;
            case "clear":
               MyauClient.friendList.clear();
               ChatUtils.addFormattedMessage(String.format("%sCleared your friend list&r", "&7[&bMyau&7] &r"));
               return;
            default:
               this.showUsage(arguments.get(0));
         }
      }
   }

   private void showUsage(String alias) {
      ChatUtils.addFormattedMessage(String.format("%sUsage: .%s <&oadd&r/&oremove&r/&olist&r/&oclear&r>&r", "&7[&bMyau&7] &r", alias.toLowerCase(Locale.ROOT)));
   }

   private void showNameUsage(String alias, String action) {
      ChatUtils.addFormattedMessage(String.format("%sUsage: .%s %s <&oname&r>&r", "&7[&bMyau&7] &r", alias.toLowerCase(Locale.ROOT), action));
   }

   @Override
   public ArrayList<String> completeArguments(ArrayList<String> arguments) {
      if (arguments.size() == 2) {
         return new ArrayList<>(Arrays.asList("add", "remove", "list", "clear"));
      } else {
         if (arguments.size() == 3) {
            String action = arguments.get(1).toLowerCase(Locale.ROOT);
            if (action.equals("add")) {
               return null;
            }

            if (action.equals("remove")) {
               return MyauClient.friendList.entries();
            }
         }

         return new ArrayList<>();
      }
   }
}
