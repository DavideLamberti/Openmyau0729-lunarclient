package myau;

import com.google.common.base.CaseFormat;
import myau.mixin.AccessorC03PacketPlayer;
import myau.mixin.AccessorMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;

public class NoFallModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private static final int MAX_DELAY_MILLIS = 10000;
   private static final long ARM_DELAY_MILLIS = 3000L;
   private final ElapsedTimer actionDelayTimer = new ElapsedTimer();
   private final ElapsedTimer armDelayTimer = new ElapsedTimer();
   private boolean packetModeActive = false;
   private boolean blinkReady = false;
   public final ModeSetting mode = new ModeSetting("mode", 0, new String[]{"PACKET", "BLINK", "NO_GROUND", "SPOOF"});
   public final FloatSetting distance = new FloatSetting("distance", 3.0F, 0.0F, 20.0F);
   public final IntegerSetting delay = new IntegerSetting("delay", 0, 0, 10000);

   public NoFallModule() {
      super("NoFall", false, 0);
   }

   private boolean isActionReady() {
      return this.armDelayTimer.hasElapsed(3000L) && this.actionDelayTimer.hasElapsed(this.delay.getValue().longValue());
   }

   public boolean isPacketModeActive() {
      return this.packetModeActive;
   }

   @EventHandler(
      priority = 50
   )
   public void onPacketSend(PacketSendEvent event) {
      if (this.isEnabled() && !event.isCancelled() && event.getPacket() instanceof C03PacketPlayer) {
         C03PacketPlayer packet = (C03PacketPlayer)event.getPacket();
         switch (this.mode.getValue()) {
            case 0:
               this.handlePacketMode(packet);
               return;
            case 1:
               this.handleBlinkMode(packet);
               return;
            case 2:
               ((AccessorC03PacketPlayer)packet).setOnGround(false);
               return;
            case 3:
               this.handleSpoofMode(packet);
               return;
         }
      }
   }

   private void handlePacketMode(C03PacketPlayer packet) {
      if (this.packetModeActive) {
         this.packetModeActive = false;
         ((AccessorMinecraft)minecraft).getTimer().timerSpeed = 1.0F;
      } else if (!packet.isOnGround()) {
         AxisAlignedBB bounds = minecraft.thePlayer.getEntityBoundingBox().expand(2.0, 0.0, 2.0);
         if (PlayerUtils.wouldTakeFallDamage(this.distance.getValue()) && !PlayerUtils.isBoundsOverVoid(bounds) && this.isActionReady()) {
            this.actionDelayTimer.reset();
            this.packetModeActive = true;
            ((AccessorMinecraft)minecraft).getTimer().timerSpeed = 0.5F;
         }
      }
   }

   private void handleBlinkMode(C03PacketPlayer packet) {
      boolean playerSafe = !minecraft.thePlayer.isOnLadder()
         && !minecraft.thePlayer.capabilities.allowFlying
         && minecraft.thePlayer.hurtTime == 0;
      if (MyauClient.outgoingPacketBuffer.owner() != OutgoingBufferOwner.NO_FALL) {
         if (this.blinkReady
            && !packet.isOnGround()
            && playerSafe
            && PlayerUtils.isAirBelow(this.distance.getValue().intValue())
            && minecraft.thePlayer.motionY < 0.0) {
            MyauClient.outgoingPacketBuffer.setEnabled(false, MyauClient.outgoingPacketBuffer.owner());
            MyauClient.outgoingPacketBuffer.setEnabled(true, OutgoingBufferOwner.NO_FALL);
         }
      } else if (!playerSafe) {
         MyauClient.outgoingPacketBuffer.setEnabled(false, OutgoingBufferOwner.NO_FALL);
         ChatUtils.addFormattedMessage(String.format("%s%s: &cFailed player check!&r", "&7[&bMyau&7] &r", this.getName()));
      } else if (PlayerUtils.isBoundsOverVoid(minecraft.thePlayer.getEntityBoundingBox().expand(2.0, 0.0, 2.0))) {
         MyauClient.outgoingPacketBuffer.setEnabled(false, OutgoingBufferOwner.NO_FALL);
         ChatUtils.addFormattedMessage(String.format("%s%s: &cFailed void check!&r", "&7[&bMyau&7] &r", this.getName()));
      } else if (packet.isOnGround()) {
         for (Packet queuedPacket : MyauClient.outgoingPacketBuffer.queuedPackets) {
            if (queuedPacket instanceof C03PacketPlayer) {
               ((AccessorC03PacketPlayer)queuedPacket).setOnGround(true);
            }
         }

         MyauClient.outgoingPacketBuffer.setEnabled(false, OutgoingBufferOwner.NO_FALL);
         this.actionDelayTimer.reset();
      }

      this.blinkReady = packet.isOnGround() && playerSafe && this.isActionReady();
   }

   private void handleSpoofMode(C03PacketPlayer packet) {
      if (!packet.isOnGround()) {
         AxisAlignedBB bounds = minecraft.thePlayer.getEntityBoundingBox().expand(2.0, 0.0, 2.0);
         if (PlayerUtils.wouldTakeFallDamage(this.distance.getValue()) && !PlayerUtils.isBoundsOverVoid(bounds) && this.isActionReady()) {
            this.actionDelayTimer.reset();
            ((AccessorC03PacketPlayer)packet).setOnGround(true);
            minecraft.thePlayer.fallDistance = 0.0F;
         }
      }
   }

   @EventHandler(
      priority = 50
   )
   public void onPacketReceive(PacketReceiveEvent event) {
      if (this.isEnabled() && !event.isCancelled()) {
         if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            this.onDisable();
         }
      }
   }

   @EventHandler(
      priority = 100
   )
   public void onPreTick(PreTickEvent event) {
      if (this.isEnabled()) {
         if (ScoreboardUtils.hasLobbyPlayerCount()) {
            this.armDelayTimer.reset();
         }

         if (this.mode.getValue() == 0 && this.packetModeActive) {
            PacketUtils.sendWithoutSendEvent(new C03PacketPlayer(true));
            minecraft.thePlayer.fallDistance = 0.0F;
         }
      }
   }

   @Override
   public void onDisable() {
      this.blinkReady = false;
      MyauClient.outgoingPacketBuffer.setEnabled(false, OutgoingBufferOwner.NO_FALL);
      if (this.packetModeActive) {
         this.packetModeActive = false;
         ((AccessorMinecraft)minecraft).getTimer().timerSpeed = 1.0F;
      }
   }

   @Override
   public void onSettingChanged(String settingName) {
      if (this.isEnabled()) {
         this.onDisable();
      }
   }

   @Override
   public String[] getHudInfo() {
      return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeName())};
   }
}
