package myau;

import myau.mixin.AccessorPlayerControllerMP;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class LongJumpModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private final ElapsedTimer activationTimer = new ElapsedTimer();
   private final ElapsedTimer damageWaitTimer = new ElapsedTimer();
   private boolean jumpStarted;
   private int jumpTicks;
   private int jumpPhase;
   private boolean launchRotationReady;
   private boolean fireballLaunchAttempted;
   private int previousHotbarSlot = -1;
   public final ModeSetting mode = new ModeSetting("mode", 0, new String[]{"FIREBALL", "FIREBALL_MANUAL", "FIREBALL_HIGH", "FIREBALL_FLAT"});
   public final FloatSetting motion = new FloatSetting("motion", 1.0F, 1.0F, 20.0F);
   public final FloatSetting speedMotion = new FloatSetting("speed-motion", 1.0F, 1.0F, 20.0F);
   public final PercentSetting strafe = new PercentSetting("strafe", 0);

   public LongJumpModule() {
      super("LongJump", false, 0);
   }

   private int findFireballSlot() {
      if (minecraft.thePlayer == null) {
         return -1;
      } else {
         for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = minecraft.thePlayer.inventory.getStackInSlot(slot);
            if (stack != null && stack.getItem() instanceof ItemFireball) {
               return slot;
            }
         }

         return -1;
      }
   }

   private double getConfiguredMotion() {
      return MovementUtils.speedPotionLevel() > 0 ? this.speedMotion.getValue().doubleValue() : this.motion.getValue().doubleValue();
   }

   public boolean isAutomaticFireballMode() {
      int currentMode = this.mode.getValue();
      return currentMode == 0 || currentMode == 2 || currentMode == 3;
   }

   public boolean isManualFireballMode() {
      return this.mode.getValue() == 1;
   }

   public boolean isFireballMode() {
      return this.isAutomaticFireballMode() || this.isManualFireballMode();
   }

   public boolean isAwaitingLaunchVelocity() {
      return !this.activationTimer.hasElapsed(1000L) && !this.jumpStarted;
   }

   public boolean hasJumpStarted() {
      return this.jumpStarted;
   }

   @EventHandler(
      priority = 100
   )
   public void onVelocity(VelocityEvent event) {
      if (this.isEnabled() && !event.isCancelled()) {
         if (this.isManualFireballMode() || this.isAutomaticFireballMode()) {
            if (this.isAwaitingLaunchVelocity()) {
               event.setCancelled(true);
               this.jumpStarted = true;
               this.jumpTicks = 0;
            }
         }
      }
   }

   @EventHandler(
      priority = 100
   )
   public void onPreTick(PreTickEvent event) {
      if (this.isEnabled() && this.isAutomaticFireballMode() && !this.fireballLaunchAttempted && this.launchRotationReady) {
         int fireballSlot = this.findFireballSlot();
         if (fireballSlot != -1) {
            this.previousHotbarSlot = minecraft.thePlayer.inventory.currentItem;
            minecraft.thePlayer.inventory.currentItem = fireballSlot;
            ((AccessorPlayerControllerMP)minecraft.playerController).callSyncCurrentPlayItem();
            PacketUtils.send(new C08PacketPlayerBlockPlacement(minecraft.thePlayer.getHeldItem()));
            this.activationTimer.reset();
            this.fireballLaunchAttempted = true;
         }
      }
   }

   @EventHandler(
      priority = 100
   )
   public void onPostTick(PostTickEvent event) {
      if (this.previousHotbarSlot != -1) {
         minecraft.thePlayer.inventory.currentItem = this.previousHotbarSlot;
         this.previousHotbarSlot = -1;
      }
   }

   @EventHandler
   public void onRotationUpdate(RotationUpdateEvent event) {
      if (this.isEnabled() && this.isFireballMode()) {
         if (this.jumpStarted) {
            this.jumpTicks++;
            if (this.jumpTicks == 1) {
               switch (this.mode.getValue()) {
                  case 0:
                  case 1:
                     this.jumpPhase = 0;
                     break;
                  case 2:
                     this.jumpPhase = 1;
                     break;
                  case 3:
                     this.jumpPhase = MovementUtils.hasMovementInput() ? 2 : 1;
               }
            }

            if (this.jumpTicks == 2 && MovementUtils.hasMovementInput()) {
               MovementUtils.setSpeedAlongMotion(MovementUtils.horizontalSpeed() * this.getConfiguredMotion());
            }

            if (this.jumpTicks >= 1 && this.jumpTicks <= 30) {
               switch (this.jumpPhase) {
                  case 1:
                     if (this.jumpTicks == 1) {
                        minecraft.thePlayer.motionY *= 0.75;
                     } else {
                        double adjustedMotionY = minecraft.thePlayer.motionY / 0.98F + 0.055;
                        if (adjustedMotionY > 0.0) {
                           minecraft.thePlayer.motionY = adjustedMotionY;
                        }
                     }
                     break;
                  case 2:
                     if (this.jumpTicks == 1) {
                        minecraft.thePlayer.motionY *= 0.75;
                     } else {
                        minecraft.thePlayer.motionY = 0.01 + (double)this.jumpTicks * 0.003;
                     }
               }
            }

            if (this.jumpTicks >= 30) {
               this.jumpStarted = false;
               this.jumpTicks = 0;
               this.jumpPhase = 0;
               if (this.isAutomaticFireballMode()) {
                  this.setEnabled(false);
               }

               return;
            }
         }

         if (this.isAutomaticFireballMode() && !this.jumpStarted) {
            if (this.damageWaitTimer.hasElapsed(1500L)) {
               this.setEnabled(false);
            } else {
               this.launchRotationReady = true;
               float yaw = RotationUtils.snapToMouseSensitivity(
                  minecraft.thePlayer.rotationYaw - 180.0F - RandomUtils.randomFloat(0.0F, 1.0F), event.getPreviousYaw()
               );
               float pitch = RotationUtils.snapToMouseSensitivity(89.0F + RandomUtils.randomFloat(-0.25F, 0.25F), event.getPreviousPitch());
               event.setRotation(yaw, pitch, 4);
               event.setMovementYaw(yaw, 4);
            }
         }
      }
   }

   @EventHandler
   public void onMovementInput(MovementInputEvent event) {
      if (this.isEnabled() && RotationState.isActive() && RotationState.movementPriority() == 4.0F && MovementUtils.hasMovementInput()) {
         MovementUtils.correctMovementForYaw(RotationState.movementYaw());
      }
   }

   @EventHandler
   public void onMoveFlying(MoveFlyingEvent event) {
      if (this.isEnabled() && this.isFireballMode() && this.jumpStarted && this.jumpTicks >= 5 && this.jumpTicks <= 30 && this.strafe.getValue() > 0) {
         double horizontalSpeed = MovementUtils.horizontalSpeed();
         MovementUtils.setHorizontalMotion(horizontalSpeed * (double)((float)(100 - this.strafe.getValue()) / 100.0F), MovementUtils.motionYaw());
         MovementUtils.addHorizontalMotion(horizontalSpeed * (double)((float)this.strafe.getValue().intValue() / 100.0F), MovementUtils.movementYaw());
         MovementUtils.setSpeedAlongMotion(horizontalSpeed);
      }
   }

   @EventHandler
   public void onKeyPress(KeyPressEvent event) {
      if (event.getKeyCode() == minecraft.gameSettings.keyBindUseItem.getKeyCode()) {
         ItemStack stack = minecraft.thePlayer.inventory.getCurrentItem();
         if (stack != null && stack.getItem() instanceof ItemFireball) {
            this.activationTimer.reset();
         }
      }
   }

   @EventHandler(
      priority = 50
   )
   public void onPacketReceive(PacketReceiveEvent event) {
      if (this.isEnabled() && !event.isCancelled() && event.getPacket() instanceof S08PacketPlayerPosLook) {
         this.jumpStarted = false;
         this.jumpTicks = 0;
         this.jumpPhase = 0;
         if (this.isAutomaticFireballMode()) {
            this.setEnabled(false);
         }
      }
   }

   @Override
   public void onEnable() {
      this.damageWaitTimer.reset();
      if (this.isAutomaticFireballMode() && this.findFireballSlot() == -1) {
         this.setEnabled(false);
         ChatUtils.addFormattedMessage(String.format("%s%s: &cNo fireball found in your hotbar!&r", "&7[&bMyau&7] &r", this.getName()));
      }
   }

   @Override
   public void onDisable() {
      this.jumpStarted = false;
      this.jumpTicks = 0;
      this.jumpPhase = 0;
      this.launchRotationReady = false;
      this.fireballLaunchAttempted = false;
   }

   @Override
   public String[] getHudInfo() {
      return new String[]{"Fireball"};
   }
}
