package myau;

import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;

public final class PacketUtils {
   private static final Minecraft minecraft = Minecraft.getMinecraft();

   private PacketUtils() {
   }

   public static void send(Packet<?> packet) {
      minecraft.getNetHandler().getNetworkManager().sendPacket(packet);
   }

   public static void sendWithoutSendEvent(Packet<?> packet) {
      minecraft.getNetHandler().getNetworkManager().sendPacket(packet, null, new GenericFutureListener[0]);
   }
}
