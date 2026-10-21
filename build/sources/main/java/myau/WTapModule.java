package myau;

import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.potion.Potion;

public class WTapModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private final ElapsedTimer attackCooldownTimer = new ElapsedTimer();
   private int chanceAccumulator = 0;
   private boolean wTapActive = false;
   private boolean forwardSuppressionStarted = false;
   private long delayRemainingMillis = 0L;
   private long suppressionRemainingMillis = 0L;
   public final FloatSetting delay = new FloatSetting("delay", 5.5F, 0.0F, 10.0F);
   public final FloatSetting duration = new FloatSetting("duration", 1.5F, 1.0F, 5.0F);
   public final PercentSetting chance = new PercentSetting("chance", 100);

   public WTapModule() {
      super("WTap", false, 0);
   }

   private boolean canContinueWTap() {
      if (!(minecraft.thePlayer.movementInput.moveForward < 0.8F) && !minecraft.thePlayer.isCollidedHorizontally) {
         if ((float)minecraft.thePlayer.getFoodStats().getFoodLevel() <= 6.0F && !minecraft.thePlayer.capabilities.allowFlying) {
            return false;
         } else if (minecraft.thePlayer.isSneaking()) {
            return true;
         } else {
            return !minecraft.thePlayer.isUsingItem() && !minecraft.thePlayer.isPotionActive(Potion.blindness)
               ? minecraft.gameSettings.keyBindSprint.isKeyDown()
               : false;
         }
      } else {
         return false;
      }
   }

   @EventHandler(
      priority = -100
   )
   public void onMovementInput(MovementInputEvent event) {
      if (this.wTapActive) {
         if (!this.forwardSuppressionStarted && !this.canContinueWTap()) {
            this.wTapActive = false;

            while (this.delayRemainingMillis > 0L) {
               this.delayRemainingMillis -= 50L;
            }

            while (this.suppressionRemainingMillis > 0L) {
               this.suppressionRemainingMillis -= 50L;
            }
         } else if (this.delayRemainingMillis > 0L) {
            this.delayRemainingMillis -= 50L;
         } else {
            if (this.suppressionRemainingMillis > 0L) {
               this.suppressionRemainingMillis -= 50L;
               this.forwardSuppressionStarted = true;
               minecraft.thePlayer.movementInput.moveForward = 0.0F;
            }

            if (this.suppressionRemainingMillis <= 0L) {
               this.wTapActive = false;
            }
         }
      }
   }

   @EventHandler
   public void onPacketSend(PacketSendEvent event) {
      if (this.isEnabled() && !event.isCancelled() && event.getPacket() instanceof C02PacketUseEntity) {
         C02PacketUseEntity packet = (C02PacketUseEntity)event.getPacket();
         if (packet.getAction() == Action.ATTACK
            && !this.wTapActive
            && this.attackCooldownTimer.hasElapsed(500L)
            && minecraft.thePlayer.isSprinting()) {
            this.attackCooldownTimer.reset();
            this.chanceAccumulator = this.chanceAccumulator % 100 + this.chance.getValue();
            if (this.chanceAccumulator >= 100) {
               this.wTapActive = true;
               this.forwardSuppressionStarted = false;
               this.delayRemainingMillis = this.delayRemainingMillis + (long)(50.0F * this.delay.getValue());
               this.suppressionRemainingMillis = this.suppressionRemainingMillis + (long)(50.0F * this.duration.getValue());
            }
         }
      }
   }
}
