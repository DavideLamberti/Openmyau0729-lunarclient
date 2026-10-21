package myau;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import net.minecraft.client.Minecraft;

public class VClipCommand extends Command {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private static final DecimalFormat decimalFormat = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));

   public VClipCommand() {
      super(new ArrayList<>(Collections.singletonList("vclip")));
   }

   @Override
   public void execute(ArrayList<String> arguments) {
      if (arguments.size() < 2) {
         ChatUtils.addFormattedMessage(String.format("%sUsage: .%s <&odistance&r>&r", "&7[&bMyau&7] &r", arguments.get(0).toLowerCase(Locale.ROOT)));
      } else {
         double distance = 0.0;

         try {
            distance = Double.parseDouble(arguments.get(1));
         } catch (NumberFormatException var5) {
         }

         minecraft.thePlayer
            .setPositionAndUpdate(minecraft.thePlayer.posX, minecraft.thePlayer.posY + distance, minecraft.thePlayer.posZ);
         ChatUtils.addFormattedMessage(String.format("%sClipped (%s blocks)", "&7[&bMyau&7] &r", decimalFormat.format(distance)));
      }
   }
}
