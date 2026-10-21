package myau;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;

public class PacketActionTracker {
   public boolean sentUseEntity;
   public boolean sentDigging;
   public boolean sentBlockPlacement;
   public boolean sentHeldItemChange;
   public boolean sentAnimation;

   public void record(Packet<?> packet) {
      if (packet instanceof C02PacketUseEntity) {
         this.sentUseEntity = true;
      }

      if (packet instanceof C07PacketPlayerDigging) {
         this.sentDigging = true;
      }

      if (packet instanceof C08PacketPlayerBlockPlacement) {
         this.sentBlockPlacement = true;
      }

      if (packet instanceof C09PacketHeldItemChange) {
         this.sentHeldItemChange = true;
      }

      if (packet instanceof C0APacketAnimation) {
         this.sentAnimation = true;
      }

      if (packet instanceof C03PacketPlayer) {
         this.sentUseEntity = false;
         this.sentDigging = false;
         this.sentBlockPlacement = false;
         this.sentHeldItemChange = false;
         this.sentAnimation = false;
      }
   }
}
