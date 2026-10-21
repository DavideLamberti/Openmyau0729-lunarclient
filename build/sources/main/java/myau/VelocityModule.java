package myau;

import com.google.common.base.CaseFormat;
import myau.mixin.AccessorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.potion.Potion;

public class VelocityModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private int chanceAccumulator;
   private int delayChanceAccumulator;
   private boolean explosionPending;
   private boolean awaitingDamageStatus = true;
   private boolean jumpPending;
   private boolean bufferingVelocity;
   private boolean reversePending;
   public final ModeSetting mode = new ModeSetting("mode", 0, new String[]{"VANILLA", "JUMP", "DELAY", "REVERSE"});
   public final IntegerSetting delayTicks = new IntegerSetting("delay-ticks", 3, 1, 20, this::showDelaySettings);
   public final PercentSetting delayChance = new PercentSetting("delay-chance", 100, this::showDelaySettings);
   public final PercentSetting chance = new PercentSetting("chance", 100);
   public final PercentSetting horizontal = new PercentSetting("horizontal", 0);
   public final PercentSetting vertical = new PercentSetting("vertical", 100);
   public final PercentSetting explosionsHorizontal = new PercentSetting("explosions-horizontal", 100);
   public final PercentSetting explosionsVertical = new PercentSetting("explosions-vertical", 100);
   public final BooleanSetting fakeCheck = new BooleanSetting("fake-check", true);
   public final BooleanSetting debugLog = new BooleanSetting("debug-log", false);

   public VelocityModule() {
      super("Velocity", false, 0);
   }

   private boolean inFluidOrWeb() {
      boolean inWater = minecraft.thePlayer.isInWater();
      return inWater || minecraft.thePlayer.isInLava() || ((AccessorEntity)minecraft.thePlayer).getIsInWeb();
   }

   private boolean showDelaySettings() {
      int selection = this.mode.getValue();
      return selection == 2;
   }

   @EventHandler
   public void onVelocity(VelocityEvent event) {
      if (!this.isEnabled() || event.isCancelled()) {
         this.explosionPending = false;
         this.awaitingDamageStatus = true;
      } else if (!this.awaitingDamageStatus || !this.fakeCheck.getValue()) {
         this.awaitingDamageStatus = true;
         if (this.explosionPending) {
            this.explosionPending = false;
            if (this.explosionsHorizontal.getValue() > 0) {
               event.setVelocityX(event.getVelocityX() * (double)this.explosionsHorizontal.getValue().intValue() / 100.0);
               event.setVelocityZ(event.getVelocityZ() * (double)this.explosionsHorizontal.getValue().intValue() / 100.0);
            } else {
               event.setVelocityX(minecraft.thePlayer.motionX);
               event.setVelocityZ(minecraft.thePlayer.motionZ);
            }

            if (this.explosionsVertical.getValue() > 0) {
               event.setVelocityY(event.getVelocityY() * (double)this.explosionsVertical.getValue().intValue() / 100.0);
            } else {
               event.setVelocityY(minecraft.thePlayer.motionY);
            }
         } else {
            this.chanceAccumulator = this.chanceAccumulator % 100 + this.chance.getValue();
            if (this.chanceAccumulator >= 100) {
               this.jumpPending = (this.mode.getValue() == 1 || this.mode.getValue() == 2) && event.getVelocityY() > 0.0;
               this.reversePending = this.mode.getValue() == 3;
               if (this.horizontal.getValue() > 0) {
                  event.setVelocityX(event.getVelocityX() * (double)this.horizontal.getValue().intValue() / 100.0);
                  event.setVelocityZ(event.getVelocityZ() * (double)this.horizontal.getValue().intValue() / 100.0);
               } else {
                  event.setVelocityX(minecraft.thePlayer.motionX);
                  event.setVelocityZ(minecraft.thePlayer.motionZ);
               }

               if (this.vertical.getValue() > 0) {
                  event.setVelocityY(event.getVelocityY() * (double)this.vertical.getValue().intValue() / 100.0);
               } else {
                  event.setVelocityY(minecraft.thePlayer.motionY);
               }
            }
         }
      }
   }

   @EventHandler
   public void onPostPlayerUpdate(PostPlayerUpdateEvent event) {
      boolean applyReverse = this.bufferingVelocity;
      if (this.bufferingVelocity
         && (
            minecraft.thePlayer.onGround
               || this.inFluidOrWeb()
               || MyauClient.incomingPacketBuffer.bufferedTickCount() >= (long)this.delayTicks.getValue().intValue()
         )) {
         MyauClient.incomingPacketBuffer.setEnabled(false, IncomingBufferOwner.VELOCITY);
         this.bufferingVelocity = false;
      }

      applyReverse = this.reversePending;
      if (applyReverse) {
         MovementUtils.setHorizontalMotion(MovementUtils.horizontalSpeed(), MovementUtils.movementYaw());
         this.reversePending = false;
      }
   }

   @EventHandler
   public void onLivingUpdate(LivingUpdateEvent event) {
      if (this.jumpPending) {
         this.jumpPending = false;
         if (minecraft.thePlayer.onGround && minecraft.thePlayer.isSprinting() && !minecraft.thePlayer.isPotionActive(Potion.jump)) {
            if (!this.inFluidOrWeb()) {
               minecraft.thePlayer.movementInput.jump = true;
            }
         }
      }
   }

   @EventHandler
   public void onRawPacketReceive(RawPacketReceiveEvent event) {
      Packet packet = event.getPacket();
      if (packet instanceof S12PacketEntityVelocity) {
         packet = event.getPacket();
         S12PacketEntityVelocity velocity = (S12PacketEntityVelocity)packet;
         int entityId = velocity.getEntityID();
         if (entityId == minecraft.thePlayer.getEntityId() && this.bufferingVelocity) {
            MyauClient.incomingPacketBuffer.setEnabled(false, IncomingBufferOwner.VELOCITY);
            this.bufferingVelocity = false;
         }
      }
   }

   @EventHandler
   public void onPacketReceive(PacketReceiveEvent event) {
      boolean enabled = this.isEnabled();
      if (enabled && !event.isCancelled()) {
         if (event.getPacket() instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity velocity = (S12PacketEntityVelocity)event.getPacket();
            if (velocity.getEntityID() == minecraft.thePlayer.getEntityId()) {
               LongJumpModule conflictingModule = (LongJumpModule)MyauClient.moduleManager.modules.get(LongJumpModule.class);
               if (this.mode.getValue() == 2
                  && !this.bufferingVelocity
                  && MyauClient.incomingPacketBuffer.owner() == IncomingBufferOwner.NONE
                  && !minecraft.thePlayer.onGround
                  && !this.inFluidOrWeb()
                  && !this.explosionPending
                  && (!this.awaitingDamageStatus || !this.fakeCheck.getValue())
                  && (!conflictingModule.isEnabled() || !conflictingModule.isAwaitingLaunchVelocity())) {
                  this.delayChanceAccumulator = this.delayChanceAccumulator % 100 + this.delayChance.getValue();
                  if (this.delayChanceAccumulator >= 100) {
                     MyauClient.incomingPacketBuffer.setEnabled(true, IncomingBufferOwner.VELOCITY);
                     MyauClient.incomingPacketBuffer.queuedPackets.offer(velocity);
                     event.setCancelled(true);
                     this.bufferingVelocity = true;
                     return;
                  }
               }

               if (this.debugLog.getValue()) {
                  ChatUtils.addFormattedMessage(
                     String.format(
                        "%sVelocity (&otick: %d, x: %.2f, y: %.2f, z: %.2f&r)&r",
                        "&7[&bMyau&7] &r",
                        minecraft.thePlayer.ticksExisted,
                        (double)velocity.getMotionX() / 8000.0,
                        (double)velocity.getMotionY() / 8000.0,
                        (double)velocity.getMotionZ() / 8000.0
                     )
                  );
               }
            }
         } else if (event.getPacket() instanceof S27PacketExplosion) {
            S27PacketExplosion explosion = (S27PacketExplosion)event.getPacket();
            if (explosion.func_149149_c() != 0.0F || explosion.func_149144_d() != 0.0F || explosion.func_149147_e() != 0.0F) {
               this.explosionPending = true;
               if (this.explosionsHorizontal.getValue() == 0 || this.explosionsVertical.getValue() == 0) {
                  event.setCancelled(true);
               }

               if (this.debugLog.getValue()) {
                  ChatUtils.addFormattedMessage(
                     String.format(
                        "%sExplosion (&otick: %d, x: %.2f, y: %.2f, z: %.2f&r)&r",
                        "&7[&bMyau&7] &r",
                        minecraft.thePlayer.ticksExisted,
                        minecraft.thePlayer.motionX + (double)explosion.func_149149_c(),
                        minecraft.thePlayer.motionY + (double)explosion.func_149144_d(),
                        minecraft.thePlayer.motionZ + (double)explosion.func_149147_e()
                     )
                  );
               }
            }
         } else {
            if (event.getPacket() instanceof S19PacketEntityStatus) {
               S19PacketEntityStatus status = (S19PacketEntityStatus)event.getPacket();
               Entity entity = status.getEntity(minecraft.theWorld);
               if (entity == null || !entity.equals(minecraft.thePlayer)) {
                  return;
               }

               if (status.getOpCode() == 2) {
                  this.awaitingDamageStatus = false;
               }
            }
         }
      }
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      this.onDisable();
   }

   @Override
   public void onDisable() {
      this.explosionPending = false;
      this.awaitingDamageStatus = true;
   }

   @Override
   public String[] getHudInfo() {
      int selection = this.mode.getValue();
      boolean jumpOrDelay = selection == 1 || this.mode.getValue() == 2;
      if (jumpOrDelay && this.horizontal.getValue() == 100 && this.vertical.getValue() == 100) {
         return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeName())};
      } else {
         int length = 2;
         String[] suffix = new String[length];
         suffix[0] = ChatColor.formatText(String.format(this.mode.getValue() == 3 ? "&m%d%%&r" : "%d%%", this.horizontal.getValue()));
         suffix[1] = String.format("%d%%", this.vertical.getValue());
         return suffix;
      }
   }
}
