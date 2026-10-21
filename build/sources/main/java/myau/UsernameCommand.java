package myau;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraft.util.StringUtils;

public class UsernameCommand extends Command {
   private static final Minecraft minecraft = Minecraft.getMinecraft();

   public UsernameCommand() {
      super(new ArrayList<>(Arrays.asList("username", "name", "ign")));
   }

   @Override
   public void execute(ArrayList<String> arguments) {
      Session session = minecraft.getSession();
      if (session != null && !StringUtils.isNullOrEmpty(session.getUsername())) {
         String username = session.getUsername();

         try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(username), null);
            ChatUtils.addFormattedMessage(String.format("%sYour username has been copied to the clipboard (&o%s&r)&r", "&7[&bMyau&7] &r", username));
         } catch (Exception var5) {
            ChatUtils.addFormattedMessage(String.format("%sFailed to copy&r", "&7[&bMyau&7] &r"));
         }
      }
   }
}
