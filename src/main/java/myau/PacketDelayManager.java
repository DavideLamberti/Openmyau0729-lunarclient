package myau;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.util.Vec3;

public class PacketDelayManager {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   public final Deque<DelayedPacket> queuedPackets = new ConcurrentLinkedDeque<>();
   private int delayTicks;
   private boolean flushing;
   private Vec3 lastReleasedPosition = new Vec3(0.0, 0.0, 0.0);

   private void releaseExpiredPackets() {
      if (minecraft.getNetHandler() == null) {
         this.queuedPackets.clear();
      } else {
         this.flushing = true;

         try {
            while (!this.queuedPackets.isEmpty()) {
               DelayedPacket delayedPacket = this.queuedPackets.peek();
               if (this.delayTicks > 0 && delayedPacket.ageTicks <= this.delayTicks) {
                  break;
               }

               PacketUtils.sendWithoutSendEvent(delayedPacket.packet);
               this.updateLastReleasedPosition(delayedPacket.packet);
               this.queuedPackets.poll();
            }
         } finally {
            this.flushing = false;
         }
      }
   }

   private void updateLastReleasedPosition(Packet<?> packet) {
      if (packet instanceof C03PacketPlayer) {
         C03PacketPlayer movementPacket = (C03PacketPlayer)packet;
         if (movementPacket.isMoving()) {
            this.lastReleasedPosition = new Vec3(movementPacket.getPositionX(), movementPacket.getPositionY(), movementPacket.getPositionZ());
         }
      }
   }

   private void ageQueuedPackets() {
      for (DelayedPacket packet : this.queuedPackets) {
         packet.ageTicks++;
      }
   }

   public boolean buffer(Packet<?> packet) {
      this.releaseExpiredPackets();
      if (packet instanceof C00PacketKeepAlive || packet instanceof C01PacketChatMessage) {
         return false;
      } else if (this.delayTicks > 0) {
         this.queuedPackets.offer(new DelayedPacket(packet));
         return true;
      } else {
         this.updateLastReleasedPosition(packet);
         return false;
      }
   }

   public void setDelayTicks(int ticks) {
      this.delayTicks = ticks;
   }

   public Vec3 lastReleasedPosition() {
      return this.lastReleasedPosition;
   }

   public boolean isFlushing() {
      return this.flushing;
   }

   @EventHandler
   public void onPostTick(PostTickEvent event) {
      if (minecraft.thePlayer.isDead) {
         this.setDelayTicks(0);
      }

      this.ageQueuedPackets();
      this.releaseExpiredPackets();
   }

   @EventHandler
   public void onPacketSend(PacketSendEvent event) {
      Packet<?> packet = event.getPacket();
      if (packet instanceof C00Handshake
         || packet instanceof C00PacketLoginStart
         || packet instanceof C00PacketServerQuery
         || packet instanceof C01PacketPing
         || packet instanceof C01PacketEncryptionResponse) {
         this.setDelayTicks(0);
      }
   }
}
