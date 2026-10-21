package myau;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.DataWatcher.WatchableObject;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;

public class IncomingPacketBuffer {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private static final int HEALTH_METADATA_ID = 6;
   private IncomingBufferOwner bufferOwner = IncomingBufferOwner.NONE;
   private long bufferedTicks;
   public final Deque<Packet<INetHandlerPlayClient>> queuedPackets = new ConcurrentLinkedDeque<>();

   public boolean buffer(Packet<INetHandlerPlayClient> packet) {
      if (this.bufferOwner == IncomingBufferOwner.NONE) {
         return false;
      } else if (packet instanceof S01PacketJoinGame || packet instanceof S07PacketRespawn) {
         this.setEnabled(false, this.bufferOwner);
         return false;
      } else if (!(packet instanceof S00PacketKeepAlive) && !(packet instanceof S06PacketUpdateHealth)) {
         if (packet instanceof S19PacketEntityStatus) {
            byte opcode = ((S19PacketEntityStatus)packet).getOpCode();
            if (opcode == 2 || opcode == 3) {
               return false;
            }
         }

         if (packet instanceof S1CPacketEntityMetadata) {
            List<WatchableObject> metadata = ((S1CPacketEntityMetadata)packet).func_149376_c();
            if (metadata != null) {
               for (WatchableObject entry : metadata) {
                  if (entry.getDataValueId() == 6) {
                     return false;
                  }
               }
            }
         }

         this.queuedPackets.offer(packet);
         return true;
      } else {
         return false;
      }
   }

   public boolean setEnabled(boolean enabled, IncomingBufferOwner owner) {
      if (owner != IncomingBufferOwner.NONE && (this.bufferOwner == IncomingBufferOwner.NONE || this.bufferOwner == owner)) {
         if (enabled) {
            this.bufferOwner = owner;
            return true;
         } else {
            this.bufferOwner = IncomingBufferOwner.NONE;
            this.bufferedTicks = 0L;
            if (minecraft.getNetHandler() == null) {
               this.queuedPackets.clear();
               return true;
            } else {
               while (!this.queuedPackets.isEmpty() && this.bufferOwner == IncomingBufferOwner.NONE) {
                  Packet<INetHandlerPlayClient> packet = this.queuedPackets.poll();
                  PacketReceiveEvent event = new PacketReceiveEvent(packet);
                  MyauClient.eventBus.post(event);
                  if (!event.isCancelled()) {
                     try {
                        packet.processPacket(minecraft.getNetHandler());
                     } catch (ThreadQuickExitException var6) {
                     }
                  }
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   public IncomingBufferOwner owner() {
      return this.bufferOwner;
   }

   public void setOwner(IncomingBufferOwner owner) {
      this.bufferOwner = owner;
   }

   public long bufferedTickCount() {
      return this.bufferedTicks;
   }

   @EventHandler
   public void onPacketSend(PacketSendEvent event) {
      if (event.getPacket() instanceof C00Handshake
         || event.getPacket() instanceof C00PacketLoginStart
         || event.getPacket() instanceof C00PacketServerQuery
         || event.getPacket() instanceof C01PacketPing
         || event.getPacket() instanceof C01PacketEncryptionResponse) {
         this.setEnabled(false, this.bufferOwner);
      }
   }

   @EventHandler
   public void onPostTick(PostTickEvent event) {
      if (minecraft.thePlayer.isDead) {
         this.setEnabled(false, this.bufferOwner);
      }

      if (this.bufferOwner != IncomingBufferOwner.NONE) {
         this.bufferedTicks++;
      }
   }
}
