package myau;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.DataWatcher.WatchableObject;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;

public class HurtTimeTracker {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private static final int HEALTH_METADATA_ID = 6;
   private boolean healthDecreasePending;
   private int remainingHurtTicks;

   public int remainingHurtTicks() {
      return this.remainingHurtTicks;
   }

   @EventHandler(
      priority = 100
   )
   public void onPreTick(PreTickEvent event) {
      if (this.remainingHurtTicks > 0) {
         this.remainingHurtTicks--;
      }

      if (this.healthDecreasePending) {
         this.healthDecreasePending = false;
         this.remainingHurtTicks = minecraft.thePlayer.hurtTime;
      }
   }

   @EventHandler
   public void onPacketReceive(PacketReceiveEvent event) {
      if (!event.isCancelled()) {
         if (event.getPacket() instanceof S06PacketUpdateHealth) {
            S06PacketUpdateHealth packet = (S06PacketUpdateHealth)event.getPacket();
            if (packet.getHealth() < minecraft.thePlayer.getHealth()) {
               this.healthDecreasePending = true;
            }
         } else if (event.getPacket() instanceof S1CPacketEntityMetadata) {
            S1CPacketEntityMetadata packet = (S1CPacketEntityMetadata)event.getPacket();
            if (packet.getEntityId() == minecraft.thePlayer.getEntityId()) {
               for (WatchableObject metadata : packet.func_149376_c()) {
                  if (metadata.getDataValueId() == 6 && (Float)metadata.getObject() < minecraft.thePlayer.getHealth()) {
                     this.healthDecreasePending = true;
                     break;
                  }
               }
            }
         }
      }
   }
}
