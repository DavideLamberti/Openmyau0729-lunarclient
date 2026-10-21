package myau;

import com.google.common.base.CaseFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import myau.mixin.AccessorEntityPlayerSP;
import myau.mixin.AccessorMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class TimerModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private static final DecimalFormat speedFormat = new DecimalFormat("0.0#", new DecimalFormatSymbols(Locale.US));
   private boolean timerSpeedOwned;
   private boolean allowNextMovementPacket;
   private boolean movementInputCleared;
   private boolean motionStored;
   private double savedMotionX;
   private double savedMotionY;
   private double savedMotionZ;
   private int bufferedMovementPackets;
   public final ModeSetting mode = new ModeSetting("mode", 0, new String[]{"VANILLA", "HYPIXEL"});
   public final FloatSetting speed = new FloatSetting("speed", 1.0F, 0.0F, 10.0F, this::showSpeedSetting);

   public TimerModule() {
      super("Timer", false, 0);
   }

   private boolean readyToMove() {
      return this.bufferedMovementPackets >= 10 || ((AccessorEntityPlayerSP)minecraft.thePlayer).getPositionUpdateTicks() >= 20;
   }

   private boolean showSpeedSetting() {
      int selection = this.mode.getValue();
      return selection == 0;
   }

   @EventHandler
   public void onPostTick(PostTickEvent event) {
      boolean enabled = this.isEnabled();
      if (enabled && this.mode.getValue() == 0) {
         if (minecraft.currentScreen != null) {
            this.onDisable();
         } else {
            NoFallModule conflictingModule = (NoFallModule)MyauClient.moduleManager.modules.get(NoFallModule.class);
            if (conflictingModule.isPacketModeActive()) {
               this.timerSpeedOwned = false;
            } else {
               this.timerSpeedOwned = true;
               ((AccessorMinecraft)minecraft).getTimer().timerSpeed = this.speed.getValue();
            }
         }
      }
   }

   private void clearMovementInput() {
      minecraft.thePlayer.movementInput.moveStrafe = 0.0F;
      minecraft.thePlayer.movementInput.moveForward = 0.0F;
      minecraft.thePlayer.movementInput.jump = false;
      minecraft.thePlayer.movementInput.sneak = false;
   }

   @EventHandler(
      priority = -1000
   )
   public void onMovementInput(MovementInputEvent event) {
      boolean enabled = this.isEnabled();
      if (enabled && this.mode.getValue() == 1 && !this.readyToMove()) {
         this.movementInputCleared = true;
         this.clearMovementInput();
      } else {
         if (this.movementInputCleared) {
            this.movementInputCleared = false;
            this.clearMovementInput();
         }
      }
   }

   @EventHandler(
      priority = -1000
   )
   public void onMoveFlying(MoveFlyingEvent event) {
      boolean enabled = this.isEnabled();
      if (enabled && this.mode.getValue() == 1 && !this.readyToMove()) {
         if (!this.motionStored) {
            this.motionStored = true;
            this.savedMotionX = minecraft.thePlayer.motionX;
            this.savedMotionY = minecraft.thePlayer.motionY;
            this.savedMotionZ = minecraft.thePlayer.motionZ;
         }

         minecraft.thePlayer.motionX = 0.0;
         if (!minecraft.thePlayer.onGround) {
            minecraft.thePlayer.motionY = 0.0;
         }

         minecraft.thePlayer.motionZ = 0.0;
      } else {
         if (this.motionStored) {
            this.motionStored = false;
            minecraft.thePlayer.motionX = this.savedMotionX;
            minecraft.thePlayer.motionY = this.savedMotionY;
            minecraft.thePlayer.motionZ = this.savedMotionZ;
         }
      }
   }

   @EventHandler(
      priority = -1000
   )
   public void onPacketSend(PacketSendEvent event) {
      boolean enabled = this.isEnabled();
      if (enabled && !event.isCancelled() && event.getPacket() instanceof C03PacketPlayer) {
         if (this.allowNextMovementPacket) {
            this.allowNextMovementPacket = false;
         } else if (this.mode.getValue() == 1 && !this.readyToMove()) {
            this.bufferedMovementPackets++;
            if (!(event.getPacket() instanceof C05PacketPlayerLook)) {
               event.setCancelled(true);
            }
         } else {
            this.bufferedMovementPackets = 0;
         }
      }
   }

   @EventHandler(
      priority = -1000
   )
   public void onPacketReceive(PacketReceiveEvent event) {
      if (this.isEnabled()) {
         if (this.mode.getValue() == 1 && !event.isCancelled() && event.getPacket() instanceof S08PacketPlayerPosLook) {
            this.allowNextMovementPacket = true;
            this.motionStored = false;
         }
      }
   }

   @Override
   public void onDisable() {
      if (this.timerSpeedOwned) {
         this.timerSpeedOwned = false;
         ((AccessorMinecraft)minecraft).getTimer().timerSpeed = 1.0F;
      }

      this.bufferedMovementPackets = 0;
   }

   @Override
   public void onSettingChanged(String name) {
      if (this.isEnabled()) {
         this.onDisable();
      }
   }

   @Override
   public String[] getHudInfo() {
      int selection = this.mode.getValue();
      switch (selection) {
         case 0:
            return new String[]{speedFormat.format(this.speed.getValue())};
         case 1:
            return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeName())};
         default:
            return new String[0];
      }
   }
}
