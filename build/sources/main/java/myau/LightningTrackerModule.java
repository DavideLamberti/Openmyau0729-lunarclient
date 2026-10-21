package myau;

import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity;

public class LightningTrackerModule extends Module {
   private static Minecraft minecraft;
   private static final int MESSAGE_ARGUMENT_COUNT = 6;

   private String getCompassDirection(double playerX, double playerZ, double strikeX, double strikeZ) {
      double dx = strikeX - playerX;
      double dz = strikeZ - playerZ;
      if (dx == 0.0 && dz == 0.0) {
         return "?";
      } else {
         double angle = Math.toDegrees(Math.atan2(dz, dx));
         if (angle < 0.0) {
            angle += 360.0;
         }

         if (angle < 22.5 || angle >= 337.5) {
            return "E";
         } else if (angle < 67.5) {
            return "SE";
         } else if (angle < 112.5) {
            return "S";
         } else if (angle < 157.5) {
            return "SW";
         } else if (angle < 202.5) {
            return "W";
         } else if (angle < 247.5) {
            return "NW";
         } else {
            return angle < 292.5 ? "N" : "NE";
         }
      }
   }

   public LightningTrackerModule() {
      super("LightningTracker", false, 0, true);
   }

   @EventHandler
   public void onPacketReceive(PacketReceiveEvent event) {
      boolean processPacket = this.isEnabled();
      if (processPacket) {
         processPacket = event.getPacket() instanceof S2CPacketSpawnGlobalEntity;
         if (processPacket) {
            S2CPacketSpawnGlobalEntity lightning = (S2CPacketSpawnGlobalEntity)event.getPacket();
            int xCoordinate = lightning.func_149053_g();
            if (xCoordinate != 1) {
               return;
            }

            xCoordinate = lightning.func_149051_d();
            double x = (double)xCoordinate / 32.0;
            double y = (double)lightning.func_149050_e() / 32.0;
            double z = (double)lightning.func_149049_f() / 32.0;
            double distance = minecraft.thePlayer.getDistance(x, y, z);
            String direction = this.getCompassDirection(minecraft.thePlayer.posX, minecraft.thePlayer.posZ, x, z);
            String messageFormat = "&8[&e%s&8] &7X: &f&l%d&r &7Y: &f&l%d&r &7Z: &f&l%d&r &7D: &6&l%d&r &6%s&r";
            Object[] messageArguments = new Object[]{this.getName(), (int)x, (int)y, (int)z, (int)distance, direction};
            ChatUtils.addFormattedMessage(String.format(messageFormat, messageArguments));
         }
      }
   }
}
