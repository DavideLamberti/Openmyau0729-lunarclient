package myau;

import net.minecraft.network.Packet;

public class RawPacketReceiveEvent extends Event {
   private final Packet<?> packet;

   public RawPacketReceiveEvent(Packet<?> packet) {
      this.packet = packet;
   }

   public Packet<?> getPacket() {
      return this.packet;
   }
}
