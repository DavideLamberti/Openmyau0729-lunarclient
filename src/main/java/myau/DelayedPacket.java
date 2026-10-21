package myau;

import net.minecraft.network.Packet;

public class DelayedPacket {
   public final Packet<?> packet;
   public int ageTicks;

   public DelayedPacket(Packet<?> packet) {
      this.packet = packet;
      this.ageTicks = 0;
   }
}
