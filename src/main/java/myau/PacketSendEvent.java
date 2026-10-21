package myau;

import net.minecraft.network.Packet;

public class PacketSendEvent extends CancellableEvent {
   private final Packet<?> packet;

   public PacketSendEvent(Packet<?> packet) {
      this.packet = packet;
   }

   public Packet<?> getPacket() {
      return this.packet;
   }
}
