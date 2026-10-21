package myau;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

public class MCFModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private static final int MIDDLE_MOUSE_KEY = -98;

   public MCFModule() {
      super("MCF", false, 0, true);
   }

   @EventHandler
   public void onKeyPress(KeyPressEvent event) {
      if (this.isEnabled() && event.getKeyCode() == -98) {
         if (minecraft.objectMouseOver != null && minecraft.objectMouseOver.typeOfHit == MovingObjectType.ENTITY) {
            if (minecraft.objectMouseOver.entityHit instanceof EntityPlayer) {
               String playerName = minecraft.objectMouseOver.entityHit.getName();
               if (MyauClient.friendList.contains(playerName)) {
                  MyauClient.friendList.remove(playerName);
                  ChatUtils.addFormattedMessage(String.format("%sRemoved &o%s&r from your friend list&r", "&7[&bMyau&7] &r", playerName));
               } else {
                  MyauClient.friendList.add(playerName);
                  ChatUtils.addFormattedMessage(String.format("%sAdded &o%s&r to your friend list&r", "&7[&bMyau&7] &r", playerName));
               }
            }
         }
      }
   }
}
