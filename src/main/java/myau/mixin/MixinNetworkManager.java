package myau.mixin;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.concurrent.Future;
import myau.MyauClient;
import myau.PacketReceiveEvent;
import myau.PacketSendEvent;
import myau.RawPacketReceiveEvent;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SideOnly(Side.CLIENT)
@Mixin({NetworkManager.class})
public abstract class MixinNetworkManager {
   @Inject(
      method = {"channelRead0*"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onChannelRead(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo callbackInfo) {
      if (!packet.getClass().getName().startsWith("net.minecraft.network.play.client")
         && MyauClient.eventBus != null
         && MyauClient.incomingPacketBuffer != null) {
         MyauClient.eventBus.post(new RawPacketReceiveEvent(packet));
         if (MyauClient.incomingPacketBuffer.buffer((Packet<INetHandlerPlayClient>)packet)) {
            callbackInfo.cancel();
         } else {
            PacketReceiveEvent event = new PacketReceiveEvent(packet);
            MyauClient.eventBus.post(event);
            if (event.isCancelled()) {
               callbackInfo.cancel();
            }
         }
      }
   }

   @Inject(
      method = {"sendPacket(Lnet/minecraft/network/Packet;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSendPacket(Packet<?> packet, CallbackInfo callbackInfo) {
      if (!packet.getClass().getName().startsWith("net.minecraft.network.play.server")
         && MyauClient.eventBus != null
         && MyauClient.packetActionTracker != null
         && MyauClient.outgoingPacketBuffer != null
         && MyauClient.packetDelayManager != null) {
         PacketSendEvent packetSendEvent = new PacketSendEvent(packet);
         MyauClient.eventBus.post(packetSendEvent);
         if (packetSendEvent.isCancelled()) {
            callbackInfo.cancel();
         } else if (!MyauClient.packetDelayManager.isFlushing()) {
            if (!MyauClient.outgoingPacketBuffer.isFlushing()) {
               MyauClient.packetActionTracker.record(packet);
               if (MyauClient.outgoingPacketBuffer.buffer(packet)) {
                  callbackInfo.cancel();
                  return;
               }
            }

            if (MyauClient.packetDelayManager.buffer(packet)) {
               callbackInfo.cancel();
            }
         }
      }
   }

   @Inject(
      method = {"sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;[Lio/netty/util/concurrent/GenericFutureListener;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSendPacketWithListeners(
      Packet<?> packet,
      GenericFutureListener<? extends Future<? super Void>> listener,
      GenericFutureListener<? extends Future<? super Void>>[] additionalListeners,
      CallbackInfo callbackInfo
   ) {
      if (!packet.getClass().getName().startsWith("net.minecraft.network.play.server")
         && MyauClient.packetActionTracker != null
         && MyauClient.outgoingPacketBuffer != null
         && MyauClient.packetDelayManager != null
         && !MyauClient.packetDelayManager.isFlushing()) {
         if (!MyauClient.outgoingPacketBuffer.isFlushing()) {
            MyauClient.packetActionTracker.record(packet);
            if (MyauClient.outgoingPacketBuffer.buffer(packet)) {
               callbackInfo.cancel();
               return;
            }
         }

         if (MyauClient.packetDelayManager.buffer(packet)) {
            callbackInfo.cancel();
         }
      }
   }
}
