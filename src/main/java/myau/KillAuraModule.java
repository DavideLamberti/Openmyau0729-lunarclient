package myau;

import com.google.common.base.CaseFormat;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Optional;
import myau.mixin.AccessorPlayerControllerMP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.DataWatcher.WatchableObject;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.WorldSettings.GameType;

public class KillAuraModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private static final DecimalFormat damageFormat = new DecimalFormat("+0.0;-0.0");
   private final ElapsedTimer targetSwitchTimer = new ElapsedTimer();
   private AuraTarget currentTarget = null;
   private boolean hasTargetInAutoBlockRange = false;
   private int targetIndex = 0;
   private boolean attackedThisUpdate = false;
   private boolean serverBlocking = false;
   private boolean autoBlockActive = false;
   private boolean renderBlocking = false;
   private boolean bufferRestartPending = false;
   private long attackCooldownMillis = 0L;
   private long blockHoldMillis = 0L;
   private long blockReleaseDelayMillis = 0L;
   private int autoBlockPhase = 0;
   private int lastDamageLogTick = 0;
   public final ModeSetting mode = new ModeSetting("mode", 0, new String[]{"SINGLE", "SWITCH"});
   public final ModeSetting sort = new ModeSetting("sort", 0, new String[]{"DISTANCE", "HEALTH", "HURT_TIME", "FOV"});
   public final ModeSetting autoBlock = new ModeSetting(
      "auto-block", 7, new String[]{"NONE", "VANILLA", "HYPIXEL", "BLINK", "INTERACT", "SPOOF", "SWAP", "LEGIT", "FAKE"}
   );
   public final BooleanSetting autoBlockRequirePress = new BooleanSetting("auto-block-require-press", true, this::isAutoBlockRequirePressVisible);
   public final BooleanSetting autoBlockNoSlow = new BooleanSetting("auto-block-no-slow", false, this::isAutoBlockNoSlowVisible);
   public final FloatSetting autoBlockHold = new FloatSetting("auto-block-hold", 1.5F, 1.0F, 20.0F, this::isAutoBlockHoldVisible);
   public final FloatSetting autoBlockDelay = new FloatSetting("auto-block-delay", 0.0F, 0.0F, 20.0F, this::isAutoBlockDelayVisible);
   public final IntegerSetting autoBlockHurtTime = new IntegerSetting("auto-block-hurt-time", 6, 0, 10, this::isAutoBlockHurtTimeVisible);
   public final FloatSetting autoBlockRange = new FloatSetting("auto-block-range", 4.0F, 3.0F, 8.0F, this::isAutoBlockRangeVisible);
   public final FloatSetting swingRange = new FloatSetting("swing-range", 4.0F, 3.0F, 6.0F);
   public final FloatSetting attackRange = new FloatSetting("attack-range", 3.0F, 3.0F, 6.0F);
   public final IntegerSetting fov = new IntegerSetting("fov", 360, 30, 360);
   public final IntegerSetting minAps = new IntegerSetting("min-aps", 14, 1, 20);
   public final IntegerSetting maxAps = new IntegerSetting("max-aps", 14, 1, 20);
   public final IntegerSetting switchDelay = new IntegerSetting("switch-delay", 150, 0, 1000);
   public final ModeSetting rotations = new ModeSetting("rotations", 2, new String[]{"NONE", "LEGIT", "SILENT", "LOCK_VIEW"});
   public final ModeSetting moveFix = new ModeSetting("move-fix", 1, new String[]{"NONE", "SILENT", "STRICT"});
   public final PercentSetting smoothing = new PercentSetting("smoothing", 0);
   public final IntegerSetting angleStep = new IntegerSetting("angle-step", 90, 30, 180);
   public final BooleanSetting throughWalls = new BooleanSetting("through-walls", true);
   public final BooleanSetting requirePress = new BooleanSetting("require-press", false);
   public final BooleanSetting allowMining = new BooleanSetting("allow-mining", true);
   public final BooleanSetting weaponsOnly = new BooleanSetting("weapons-only", true);
   public final BooleanSetting allowTools = new BooleanSetting("allow-tools", false, this.weaponsOnly::getValue);
   public final BooleanSetting inventoryCheck = new BooleanSetting("inventory-check", true);
   public final BooleanSetting botCheck = new BooleanSetting("bot-check", true);
   public final BooleanSetting players = new BooleanSetting("players", true);
   public final BooleanSetting bosses = new BooleanSetting("bosses", false);
   public final BooleanSetting mobs = new BooleanSetting("mobs", false);
   public final BooleanSetting animals = new BooleanSetting("animals", false);
   public final BooleanSetting golems = new BooleanSetting("golems", false);
   public final BooleanSetting silverfish = new BooleanSetting("silverfish", false);
   public final BooleanSetting teams = new BooleanSetting("teams", true);
   public final ModeSetting showTarget = new ModeSetting("show-target", 0, new String[]{"NONE", "DEFAULT", "HUD"});
   public final ModeSetting debugLog = new ModeSetting("debug-log", 0, new String[]{"NONE", "HEALTH"});

   private long nextAttackDelayMillis() {
      return 1000L / RandomUtils.randomLong(this.minAps.getValue().longValue(), this.maxAps.getValue().longValue());
   }

   private long blockHoldDurationMillis() {
      return NumericConversions.truncateFloatToLong(this.autoBlockHold.getValue() * 50.0F);
   }

   private long blockReleaseDurationMillis() {
      return NumericConversions.truncateFloatToLong(this.autoBlockDelay.getValue() * 50.0F);
   }

   private boolean tryAttackTarget(float yaw, float pitch) {
      if (!MyauClient.packetActionTracker.sentDigging && !MyauClient.packetActionTracker.sentBlockPlacement) {
         if (this.isBlockingSword() && this.autoBlock.getValue() != 1) {
            return false;
         } else if (this.attackCooldownMillis > 0L) {
            return false;
         } else {
            HitSelectModule hitSelect = (HitSelectModule)MyauClient.moduleManager.modules.get(HitSelectModule.class);
            if (hitSelect.isEnabled() && hitSelect.shouldDelayAttack(this.currentTarget.entity)) {
               return false;
            } else {
               this.attackCooldownMillis = this.attackCooldownMillis + this.nextAttackDelayMillis();
               minecraft.thePlayer.swingItem();
               boolean canAttack;
               if (this.rotations.getValue() == 0) {
                  canAttack = this.isWithinAttackRange(this.currentTarget.distance);
               } else {
                  Vec3 eyes = minecraft.thePlayer.getPositionEyes(1.0F);
                  if (this.throughWalls.getValue()) {
                     canAttack = RotationUtils.intersectBoundsFrom(this.currentTarget.bounds, eyes, yaw, pitch, this.attackRange.getValue().doubleValue())
                        != null;
                  } else {
                     canAttack = !RotationUtils.isBoundsRayObstructed(this.currentTarget.bounds, eyes, yaw, pitch, this.attackRange.getValue().doubleValue());
                  }
               }

               if (!canAttack) {
                  return false;
               } else {
                  ((AccessorPlayerControllerMP)minecraft.playerController).callSyncCurrentPlayItem();
                  PacketUtils.send(new C02PacketUseEntity(this.currentTarget.entity, Action.ATTACK));
                  if (minecraft.playerController.getCurrentGameType() != GameType.SPECTATOR) {
                     PlayerUtils.attackLocally(this.currentTarget.entity);
                  }

                  this.attackedThisUpdate = true;
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   private void startBlockingHeldItem() {
      ((AccessorPlayerControllerMP)minecraft.playerController).callSyncCurrentPlayItem();
      this.startBlockingWithStack(minecraft.thePlayer.getHeldItem());
   }

   private void startBlockingWithStack(ItemStack stack) {
      PacketUtils.send(new C08PacketPlayerBlockPlacement(stack));
      minecraft.thePlayer.setItemInUse(stack, stack.getMaxItemUseDuration());
      this.serverBlocking = true;
   }

   private void releaseBlocking() {
      PacketUtils.send(
         new C07PacketPlayerDigging(net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN)
      );
      minecraft.thePlayer.stopUsingItem();
      this.serverBlocking = false;
   }

   private void interactWithTargetAndBlock(float yaw, float pitch) {
      if (this.currentTarget != null) {
         MovingObjectPosition hit = RotationUtils.intersectBounds(this.currentTarget.bounds, yaw, pitch, 8.0);
         if (hit != null) {
            ((AccessorPlayerControllerMP)minecraft.playerController).callSyncCurrentPlayItem();
            PacketUtils.send(
               new C02PacketUseEntity(
                  this.currentTarget.entity,
                  new Vec3(
                     hit.hitVec.xCoord - this.currentTarget.x,
                     hit.hitVec.yCoord - this.currentTarget.y,
                     hit.hitVec.zCoord - this.currentTarget.z
                  )
               )
            );
            PacketUtils.send(new C02PacketUseEntity(this.currentTarget.entity, Action.INTERACT));
            this.startBlockingWithStack(minecraft.thePlayer.getHeldItem());
         }
      }
   }

   private boolean canRunCombat() {
      if (this.currentTarget == null) {
         return false;
      } else if (this.inventoryCheck.getValue() && minecraft.currentScreen instanceof GuiContainer) {
         return false;
      } else if (!this.weaponsOnly.getValue() || ItemUtils.isHoldingWeapon() || this.allowTools.getValue() && ItemUtils.isHoldingTool()) {
         if ((ItemUtils.isHoldingConsumable() || ItemUtils.isHoldingBow()) && PlayerUtils.isUseItemKeyDown()) {
            return false;
         } else {
            AutoHealModule autoHeal = (AutoHealModule)MyauClient.moduleManager.modules.get(AutoHealModule.class);
            if (autoHeal.isEnabled() && autoHeal.hasPendingSlotRestore()) {
               return false;
            } else {
               BedNukerModule bedNuker = (BedNukerModule)MyauClient.moduleManager.modules.get(BedNukerModule.class);
               if (bedNuker.isEnabled() && bedNuker.isControllingInteractions()) {
                  return false;
               } else {
                  ScaffoldModule scaffold = (ScaffoldModule)MyauClient.moduleManager.modules.get(ScaffoldModule.class);
                  if (scaffold.isEnabled()) {
                     return false;
                  } else {
                     boolean attackKeyDown = PlayerUtils.isAttackKeyDown();
                     return this.allowMining.getValue() && attackKeyDown && minecraft.objectMouseOver.typeOfHit == MovingObjectType.BLOCK
                        ? false
                        : !this.requirePress.getValue() || attackKeyDown;
                  }
               }
            }
         }
      } else {
         return false;
      }
   }

   private boolean canAutoBlock() {
      if (!ItemUtils.isHoldingSword()) {
         return false;
      } else {
         int autoBlockMode = this.autoBlock.getValue();
         if (autoBlockMode == 0) {
            return true;
         } else if (autoBlockMode == 8) {
            return this.hasTargetInAutoBlockRange;
         } else {
            return !this.autoBlockRequirePress.getValue() && this.hasTargetInAutoBlockRange ? true : PlayerUtils.isUseItemKeyDown();
         }
      }
   }

   private boolean isValidTargetEntity(EntityLivingBase candidate) {
      return candidate != minecraft.thePlayer
            && candidate != minecraft.thePlayer.ridingEntity
            && candidate != minecraft.getRenderViewEntity()
            && candidate != minecraft.getRenderViewEntity().ridingEntity
         ? this.passesEntityCategoryFilters(candidate)
         : false;
   }

   private boolean passesEntityCategoryFilters(EntityLivingBase candidate) {
      if (candidate.deathTime > 0) {
         return false;
      } else if (candidate instanceof EntityOtherPlayerMP) {
         if (!this.players.getValue()) {
            return false;
         } else {
            EntityPlayer player = (EntityPlayer)candidate;
            if (EntityUtils.isFriend(player)) {
               return false;
            } else if (EntityUtils.isEnemy(player)) {
               return true;
            } else {
               return this.teams.getValue() && EntityUtils.isTeammate(player) ? false : !this.botCheck.getValue() || !EntityUtils.isBot(player);
            }
         }
      } else if (candidate instanceof EntityDragon || candidate instanceof EntityWither) {
         return this.bosses.getValue();
      } else if (!(candidate instanceof EntityMob) && !(candidate instanceof EntitySlime)) {
         if (candidate instanceof EntityAnimal || candidate instanceof EntityBat || candidate instanceof EntitySquid || candidate instanceof EntityVillager) {
            return this.animals.getValue();
         } else if (!(candidate instanceof EntityIronGolem)) {
            return false;
         } else {
            return !this.golems.getValue() ? false : !this.teams.getValue() || !EntityUtils.hasTeamArmorStand(candidate);
         }
      } else if (!(candidate instanceof EntitySilverfish)) {
         return this.mobs.getValue();
      } else {
         return !this.silverfish.getValue() ? false : !this.teams.getValue() || !EntityUtils.hasTeamArmorStand(candidate);
      }
   }

   private boolean isWithinAnyCombatRange(double distance) {
      return this.isWithinAutoBlockRange(distance) || this.isWithinSwingRange(distance) || this.isWithinAttackRange(distance);
   }

   private boolean isWithinAutoBlockRange(double distance) {
      return this.autoBlock.getValue() != 0 && distance <= this.autoBlockRange.getValue().doubleValue();
   }

   private boolean isWithinSwingRange(double distance) {
      return distance <= this.swingRange.getValue().doubleValue();
   }

   private boolean isWithinAttackRange(double distance) {
      return distance <= this.attackRange.getValue().doubleValue();
   }

   private boolean isEnemyPlayer(EntityLivingBase entity) {
      return entity instanceof EntityPlayer && EntityUtils.isEnemy((EntityPlayer)entity);
   }

   private int findSwapSlot(int excludedSlot) {
      for (int slot = 0; slot < 9; slot++) {
         if (slot != excludedSlot && minecraft.thePlayer.inventory.getStackInSlot(slot) == null) {
            return slot;
         }
      }

      for (int slotx = 0; slotx < 9; slotx++) {
         ItemStack stack = minecraft.thePlayer.inventory.getStackInSlot(slotx);
         if (slotx != excludedSlot && stack != null && !stack.hasDisplayName()) {
            return slotx;
         }
      }

      return Math.floorMod(excludedSlot - 1, 9);
   }

   private int findAlternateSwordSlot(int excludedSlot) {
      for (int slot = 0; slot < 9; slot++) {
         if (slot != excludedSlot) {
            ItemStack stack = minecraft.thePlayer.inventory.getStackInSlot(slot);
            if (stack != null && stack.getItem() instanceof ItemSword) {
               return slot;
            }
         }
      }

      return -1;
   }

   public KillAuraModule() {
      super("KillAura", false, 0);
   }

   public EntityLivingBase getTargetEntity() {
      return this.currentTarget == null ? null : this.currentTarget.entity;
   }

   public boolean isCombatInputAllowed() {
      if (this.inventoryCheck.getValue() && minecraft.currentScreen instanceof GuiContainer) {
         return false;
      } else if (((AccessorPlayerControllerMP)minecraft.playerController).getIsHittingBlock()) {
         return false;
      } else {
         ScaffoldModule scaffold = (ScaffoldModule)MyauClient.moduleManager.modules.get(ScaffoldModule.class);
         if (scaffold.isEnabled()) {
            return false;
         } else {
            return !this.weaponsOnly.getValue() || ItemUtils.isHoldingWeapon() || this.allowTools.getValue() && ItemUtils.isHoldingTool()
               ? !this.requirePress.getValue() || PlayerUtils.isAttackKeyDown()
               : false;
         }
      }
   }

   public boolean shouldRenderBlocking() {
      return this.renderBlocking && ItemUtils.isHoldingSword();
   }

   public boolean isBlockingSword() {
      return (minecraft.thePlayer.isUsingItem() || this.serverBlocking) && ItemUtils.isHoldingSword();
   }

   public boolean isAutoBlockActive() {
      return this.autoBlockActive;
   }

   private float[] calculateCombatRotation(RotationUpdateEvent event) {
      float previousYaw = event.getPreviousYaw();
      float previousPitch = event.getPreviousPitch();
      float yawDelta = MathHelper.wrapAngleTo180_float(this.currentTarget.yaw - previousYaw);
      float pitchDelta = this.currentTarget.pitch - previousPitch;
      if (Math.abs(yawDelta) < 0.5F) {
         yawDelta = 0.0F;
      }

      if (Math.abs(pitchDelta) < 0.5F) {
         pitchDelta = 0.0F;
      }

      yawDelta = RotationUtils.clampAngleChange(yawDelta, (float)this.angleStep.getValue().intValue() * RandomUtils.randomFloat(0.75F, 1.0F));
      pitchDelta = RotationUtils.clampAngleChange(pitchDelta, (float)this.angleStep.getValue().intValue() * RandomUtils.randomFloat(0.75F, 1.0F) * 0.5F);
      yawDelta = RotationUtils.scaleAngleChange(yawDelta, (float)this.smoothing.getValue().intValue() / 100.0F + RandomUtils.randomFloat(-0.05F, 0.05F));
      pitchDelta = RotationUtils.scaleAngleChange(pitchDelta, (float)this.smoothing.getValue().intValue() / 100.0F + RandomUtils.randomFloat(-0.05F, 0.05F));
      float sensitivity = minecraft.gameSettings.mouseSensitivity * 0.6F + 0.2F;
      float mouseStep = sensitivity * sensitivity * sensitivity * 8.0F;
      if (yawDelta == 0.0F && pitchDelta != 0.0F) {
         yawDelta += mouseStep * 0.15F * (RandomUtils.randomBoolean() ? 1.0F : -1.0F);
      }

      if (yawDelta != 0.0F && pitchDelta == 0.0F) {
         pitchDelta += mouseStep * 0.15F * (RandomUtils.randomBoolean() ? 1.0F : -1.0F);
      }

      float yaw = RotationUtils.snapToMouseSensitivity(previousYaw + yawDelta, previousYaw);
      float pitch = RotationUtils.snapToMouseSensitivity(previousPitch + pitchDelta, previousPitch);
      return new float[]{yaw, MathHelper.clamp_float(pitch, -90.0F, 90.0F)};
   }

   @EventHandler(
      priority = -50
   )
   public void onRotationUpdate(RotationUpdateEvent event) {
      if (this.isEnabled()) {
         int controllerSlot = ((AccessorPlayerControllerMP)minecraft.playerController).getCurrentPlayerItem();
         boolean combatEligible = this.canRunCombat();
         boolean canAttackThisTick = combatEligible;
         boolean canBlockThisTick = combatEligible && this.canAutoBlock();
         int autoBlockMode = this.autoBlock.getValue();
         boolean blockSuppressed = autoBlockMode != 0
            && autoBlockMode != 8
            && canBlockThisTick
            && (MyauClient.hurtTimeTracker.remainingHurtTicks() > this.autoBlockHurtTime.getValue() || this.blockReleaseDelayMillis > 0L);
         if (this.attackCooldownMillis > 0L) {
            this.attackCooldownMillis -= 50L;
         }

         if (this.blockHoldMillis > 0L) {
            this.blockHoldMillis -= 50L;
         }

         if (this.blockReleaseDelayMillis > 0L) {
            this.blockReleaseDelayMillis -= 50L;
         }

         if (!canBlockThisTick || blockSuppressed) {
            if (this.autoBlockActive
               && this.isBlockingSword()
               && !MyauClient.packetActionTracker.sentDigging
               && !MyauClient.packetActionTracker.sentBlockPlacement) {
               if (autoBlockMode != 4 && autoBlockMode != 5) {
                  this.releaseBlocking();
               } else {
                  int swapSlot = this.findSwapSlot(controllerSlot);
                  PacketUtils.send(new C09PacketHeldItemChange(swapSlot));
                  ((AccessorPlayerControllerMP)minecraft.playerController).setCurrentPlayerItem(swapSlot);
               }

               canAttackThisTick = false;
            }

            MyauClient.outgoingPacketBuffer.setEnabled(false, OutgoingBufferOwner.AUTO_BLOCK);
            this.autoBlockActive = false;
            this.renderBlocking = false;
            if (this.blockHoldMillis > 0L) {
               this.blockHoldMillis = 0L;
            }

            this.autoBlockPhase = 0;
         }

         if (combatEligible) {
            if (blockSuppressed) {
               canBlockThisTick = false;
               this.autoBlockActive = true;
               this.renderBlocking = true;
            }

            boolean shouldStartBlocking = false;
            if (canBlockThisTick) {
               switch (autoBlockMode) {
                  case 0:
                     MyauClient.outgoingPacketBuffer.setEnabled(false, OutgoingBufferOwner.AUTO_BLOCK);
                     this.autoBlockActive = false;
                     this.renderBlocking = false;
                     if (PlayerUtils.isUseItemKeyDown()
                        && !this.isBlockingSword()
                        && !MyauClient.packetActionTracker.sentDigging
                        && !MyauClient.packetActionTracker.sentBlockPlacement) {
                        shouldStartBlocking = true;
                     }
                     break;
                  case 1:
                     MyauClient.outgoingPacketBuffer.setEnabled(false, OutgoingBufferOwner.AUTO_BLOCK);
                     this.autoBlockActive = true;
                     this.renderBlocking = false;
                     if (!this.isBlockingSword() && !MyauClient.packetActionTracker.sentDigging && !MyauClient.packetActionTracker.sentBlockPlacement) {
                        shouldStartBlocking = true;
                     }
                     break;
                  case 2:
                     this.autoBlockActive = true;
                     this.renderBlocking = true;
                     if (!MyauClient.packetActionTracker.sentDigging && !MyauClient.packetActionTracker.sentBlockPlacement) {
                        switch (this.autoBlockPhase) {
                           case 0:
                              if (!this.isBlockingSword()) {
                                 shouldStartBlocking = true;
                                 this.blockHoldMillis = this.blockHoldMillis + this.blockHoldDurationMillis();
                              }

                              this.bufferRestartPending = true;
                              this.autoBlockPhase = 1;
                              break;
                           case 1:
                              if (this.blockHoldMillis <= 0L) {
                                 if (this.isBlockingSword()) {
                                    this.releaseBlocking();
                                    canAttackThisTick = false;
                                 }

                                 this.blockReleaseDelayMillis = this.blockReleaseDelayMillis + this.blockReleaseDurationMillis();
                                 this.autoBlockPhase = 0;
                              } else {
                                 this.bufferRestartPending = true;
                              }
                              break;
                           default:
                              this.autoBlockPhase = 0;
                        }
                     }
                     break;
                  case 3:
                     this.autoBlockActive = true;
                     this.renderBlocking = true;
                     if (!MyauClient.packetActionTracker.sentDigging && !MyauClient.packetActionTracker.sentBlockPlacement) {
                        switch (this.autoBlockPhase) {
                           case 0:
                              if (!this.isBlockingSword()) {
                                 shouldStartBlocking = true;
                                 this.blockHoldMillis = this.blockHoldMillis + this.blockHoldDurationMillis();
                              }

                              this.bufferRestartPending = true;
                              this.autoBlockPhase = 1;
                              break;
                           case 1:
                              if (this.isBlockingSword()) {
                                 this.releaseBlocking();
                                 canAttackThisTick = false;
                              }

                              if (this.blockHoldMillis <= 0L) {
                                 this.blockReleaseDelayMillis = this.blockReleaseDelayMillis + this.blockReleaseDurationMillis();
                                 this.autoBlockPhase = 0;
                              }
                              break;
                           default:
                              this.autoBlockPhase = 0;
                        }
                     }
                     break;
                  case 4:
                     this.autoBlockActive = true;
                     this.renderBlocking = true;
                     if (minecraft.thePlayer.inventory.currentItem == controllerSlot
                        && !MyauClient.packetActionTracker.sentDigging
                        && !MyauClient.packetActionTracker.sentBlockPlacement) {
                        switch (this.autoBlockPhase) {
                           case 0:
                              if (!this.isBlockingSword()) {
                                 shouldStartBlocking = true;
                                 this.blockHoldMillis = this.blockHoldMillis + this.blockHoldDurationMillis();
                              }

                              this.bufferRestartPending = true;
                              this.autoBlockPhase = 1;
                              break;
                           case 1:
                              if (this.isBlockingSword()) {
                                 int swapSlot = this.findSwapSlot(controllerSlot);
                                 PacketUtils.send(new C09PacketHeldItemChange(swapSlot));
                                 ((AccessorPlayerControllerMP)minecraft.playerController).setCurrentPlayerItem(swapSlot);
                                 canAttackThisTick = false;
                              }

                              if (this.blockHoldMillis <= 0L) {
                                 this.blockReleaseDelayMillis = this.blockReleaseDelayMillis + this.blockReleaseDurationMillis();
                                 this.autoBlockPhase = 0;
                              }
                              break;
                           default:
                              this.autoBlockPhase = 0;
                        }
                     }
                     break;
                  case 5:
                     MyauClient.outgoingPacketBuffer.setEnabled(false, OutgoingBufferOwner.AUTO_BLOCK);
                     this.autoBlockActive = true;
                     this.renderBlocking = false;
                     if (!MyauClient.packetActionTracker.sentDigging
                        && !MyauClient.packetActionTracker.sentBlockPlacement
                        && minecraft.thePlayer.inventory.currentItem == controllerSlot
                        && (!this.isBlockingSword() || this.blockHoldMillis <= 0L)) {
                        int swapSlot = this.findSwapSlot(controllerSlot);
                        PacketUtils.send(new C09PacketHeldItemChange(swapSlot));
                        PacketUtils.send(new C09PacketHeldItemChange(controllerSlot));
                        shouldStartBlocking = true;
                        this.blockHoldMillis = this.blockHoldMillis + this.blockHoldDurationMillis();
                     }
                     break;
                  case 6:
                     MyauClient.outgoingPacketBuffer.setEnabled(false, OutgoingBufferOwner.AUTO_BLOCK);
                     this.autoBlockActive = true;
                     this.renderBlocking = true;
                     if (minecraft.thePlayer.inventory.currentItem == controllerSlot
                        && !MyauClient.packetActionTracker.sentDigging
                        && !MyauClient.packetActionTracker.sentBlockPlacement) {
                        switch (this.autoBlockPhase) {
                           case 0:
                              if (!this.isBlockingSword()) {
                                 shouldStartBlocking = true;
                                 this.blockHoldMillis = this.blockHoldMillis + this.blockHoldDurationMillis();
                              }

                              this.autoBlockPhase = 1;
                              break;
                           case 1:
                              if (this.blockHoldMillis <= 0L) {
                                 if (this.isBlockingSword()) {
                                    int swordSlot = this.findAlternateSwordSlot(controllerSlot);
                                    if (swordSlot != -1) {
                                       PacketUtils.send(new C09PacketHeldItemChange(swordSlot));
                                       ((AccessorPlayerControllerMP)minecraft.playerController).setCurrentPlayerItem(swordSlot);
                                       this.startBlockingWithStack(minecraft.thePlayer.inventory.getStackInSlot(swordSlot));
                                    } else {
                                       this.releaseBlocking();
                                    }

                                    canAttackThisTick = false;
                                 }

                                 this.blockReleaseDelayMillis = this.blockReleaseDelayMillis + this.blockReleaseDurationMillis();
                                 this.autoBlockPhase = 0;
                              }
                              break;
                           default:
                              this.autoBlockPhase = 0;
                        }
                     }
                     break;
                  case 7:
                     MyauClient.outgoingPacketBuffer.setEnabled(false, OutgoingBufferOwner.AUTO_BLOCK);
                     this.autoBlockActive = true;
                     this.renderBlocking = true;
                     if (!MyauClient.packetActionTracker.sentDigging && !MyauClient.packetActionTracker.sentBlockPlacement) {
                        switch (this.autoBlockPhase) {
                           case 0:
                              if (!this.isBlockingSword()) {
                                 shouldStartBlocking = true;
                                 this.blockHoldMillis = this.blockHoldMillis + this.blockHoldDurationMillis();
                              }

                              this.autoBlockPhase = 1;
                              break;
                           case 1:
                              if (this.blockHoldMillis <= 0L) {
                                 if (this.isBlockingSword()) {
                                    this.releaseBlocking();
                                    canAttackThisTick = false;
                                 }

                                 this.blockReleaseDelayMillis = this.blockReleaseDelayMillis + this.blockReleaseDurationMillis();
                                 this.autoBlockPhase = 0;
                              }
                              break;
                           default:
                              this.autoBlockPhase = 0;
                        }
                     }
                     break;
                  case 8:
                     MyauClient.outgoingPacketBuffer.setEnabled(false, OutgoingBufferOwner.AUTO_BLOCK);
                     this.autoBlockActive = false;
                     this.renderBlocking = true;
                     if (PlayerUtils.isUseItemKeyDown()
                        && !this.isBlockingSword()
                        && !MyauClient.packetActionTracker.sentDigging
                        && !MyauClient.packetActionTracker.sentBlockPlacement) {
                        shouldStartBlocking = true;
                     }
               }
            }

            boolean attacked = false;
            if (this.currentTarget.hasUsableAim && this.isWithinSwingRange(this.currentTarget.distance)) {
               int rotationMode = this.rotations.getValue();
               if (rotationMode == 2 || rotationMode == 3) {
                  float[] rotation = this.calculateCombatRotation(event);
                  event.setRotation(rotation[0], rotation[1], 1);
                  if (rotationMode == 3) {
                     MyauClient.rotationSmoother.requestRotation(rotation[0], rotation[1], 1, true);
                  }

                  if (this.moveFix.getValue() != 0 || rotationMode == 3) {
                     event.setMovementYaw(rotation[0], 1);
                  }
               }

               if (canAttackThisTick) {
                  attacked = this.tryAttackTarget(event.getYaw(), event.getPitch());
               }
            }

            if (shouldStartBlocking) {
               if (attacked) {
                  this.interactWithTargetAndBlock(event.getYaw(), event.getPitch());
               } else {
                  this.startBlockingHeldItem();
               }
            }
         }
      }
   }

   @EventHandler(
      priority = -50
   )
   public void onPostPlayerUpdate(PostPlayerUpdateEvent event) {
      if (this.bufferRestartPending) {
         this.bufferRestartPending = false;
         MyauClient.outgoingPacketBuffer.setEnabled(false, OutgoingBufferOwner.AUTO_BLOCK);
         MyauClient.outgoingPacketBuffer.setEnabled(true, OutgoingBufferOwner.AUTO_BLOCK);
      }
   }

   @EventHandler
   public void onPreTick(PreTickEvent event) {
      if (this.isEnabled()) {
         ArrayList<AuraTarget> candidates = this.collectTargetCandidates();
         if (candidates.isEmpty()) {
            this.currentTarget = null;
            this.hasTargetInAutoBlockRange = false;
         } else {
            this.hasTargetInAutoBlockRange = candidates.stream().anyMatch(this::isCandidateWithinAutoBlockRange);
            if (candidates.stream().anyMatch(KillAuraModule::hasCandidateUsableAim)) {
               candidates.removeIf(KillAuraModule::shouldDiscardUnusableAim);
            }

            if (candidates.stream().anyMatch(this::isCandidateWithinSwingRange)) {
               candidates.removeIf(this::shouldDiscardOutsideSwingRange);
            }

            if (candidates.stream().anyMatch(this::isCandidateWithinAttackRange)) {
               candidates.removeIf(this::shouldDiscardOutsideAttackRange);
            }

            if (candidates.stream().anyMatch(this::isEnemyCandidate)) {
               candidates.removeIf(this::shouldDiscardNonEnemyCandidate);
            }

            if (candidates.isEmpty()) {
               this.currentTarget = null;
            } else {
               if (this.currentTarget != null && !this.targetSwitchTimer.hasElapsed((long)this.switchDelay.getValue().intValue())) {
                  Optional<AuraTarget> existing = candidates.stream().filter(this::matchesCurrentTarget).findFirst();
                  if (existing.isPresent()) {
                     this.currentTarget = existing.get();
                     return;
                  }
               }

               candidates.sort(this::compareTargetCandidates);
               if (this.mode.getValue() == 1 && this.attackedThisUpdate) {
                  this.attackedThisUpdate = false;
                  this.targetIndex++;
               }

               if (this.mode.getValue() == 0 || this.targetIndex >= candidates.size()) {
                  this.targetIndex = 0;
               }

               this.currentTarget = candidates.get(this.targetIndex);
               this.targetSwitchTimer.reset();
            }
         }
      }
   }

   private ArrayList<AuraTarget> collectTargetCandidates() {
      Vec3 eyes = minecraft.thePlayer.getPositionEyes(1.0F);
      ArrayList<AuraTarget> candidates = new ArrayList<>();

      for (Entity entity : minecraft.theWorld.loadedEntityList) {
         if (entity instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase)entity;
            if (this.isValidTargetEntity(living)) {
               double border = (double)living.getCollisionBorderSize();
               AxisAlignedBB bounds = living.getEntityBoundingBox().expand(border, border, border);
               double distance = RotationUtils.distanceToBoundsFrom(bounds, eyes);
               if (this.isWithinAnyCombatRange(distance)) {
                  float angularDistance = RotationUtils.boundsAimError(bounds, eyes);
                  if (!(angularDistance > (float)this.fov.getValue().intValue())) {
                     float[] aim = RotationUtils.aimAtBounds(bounds);
                     float yaw = aim[0];
                     float pitch = aim[1];
                     boolean usableAim = true;
                     if (!this.throughWalls.getValue() && RotationUtils.isBoundsRayObstructed(bounds, eyes, yaw, pitch, 8.0)) {
                        if (RotationUtils.isBoundsRayObstructed(bounds, eyes, minecraft.thePlayer.rotationYaw, minecraft.thePlayer.rotationPitch, 8.0)
                           )
                         {
                           usableAim = false;
                        } else {
                           yaw = minecraft.thePlayer.rotationYaw;
                           pitch = minecraft.thePlayer.rotationPitch;
                        }
                     }

                     candidates.add(
                        new AuraTarget(
                           living, bounds, living.posX, living.posY, living.posZ, yaw, pitch, distance, angularDistance, usableAim
                        )
                     );
                  }
               }
            }
         }
      }

      return candidates;
   }

   @EventHandler
   public void onPostTick(PostTickEvent event) {
      if (this.isEnabled() && this.isBlockingSword() && !minecraft.thePlayer.isBlocking()) {
         ItemStack heldItem = minecraft.thePlayer.getHeldItem();
         if (heldItem != null) {
            minecraft.thePlayer.setItemInUse(heldItem, heldItem.getMaxItemUseDuration());
         }
      }
   }

   @EventHandler(
      priority = -100
   )
   public void onPacketSend(PacketSendEvent event) {
      if (this.isEnabled() && !event.isCancelled()) {
         if (event.getPacket() instanceof C07PacketPlayerDigging) {
            C07PacketPlayerDigging digging = (C07PacketPlayerDigging)event.getPacket();
            if (digging.getStatus() == net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
               this.serverBlocking = false;
            }
         } else {
            if (event.getPacket() instanceof C09PacketHeldItemChange) {
               this.serverBlocking = false;
               if (this.autoBlockActive) {
                  minecraft.thePlayer.stopUsingItem();
               }
            }
         }
      }
   }

   private void logHealthDelta(float delta) {
      if (delta != 0.0F && this.lastDamageLogTick != minecraft.thePlayer.ticksExisted) {
         this.lastDamageLogTick = minecraft.thePlayer.ticksExisted;
         String color = delta > 0.0F ? "&a" : "&c";
         ChatUtils.addFormattedMessage(
            String.format(
               "%sHealth: %s&l%s&r (&otick: %d&r)&r", "&7[&bMyau&7] &r", color, damageFormat.format((double)delta), minecraft.thePlayer.ticksExisted
            )
         );
      }
   }

   @EventHandler(
      priority = -100
   )
   public void onPacketReceive(PacketReceiveEvent event) {
      if (this.isEnabled() && !event.isCancelled() && this.debugLog.getValue() == 1) {
         if (event.getPacket() instanceof S06PacketUpdateHealth) {
            S06PacketUpdateHealth healthPacket = (S06PacketUpdateHealth)event.getPacket();
            this.logHealthDelta(healthPacket.getHealth() - minecraft.thePlayer.getHealth());
         } else if (event.getPacket() instanceof S1CPacketEntityMetadata) {
            S1CPacketEntityMetadata metadataPacket = (S1CPacketEntityMetadata)event.getPacket();
            if (metadataPacket.getEntityId() == minecraft.thePlayer.getEntityId()) {
               for (WatchableObject metadata : metadataPacket.func_149376_c()) {
                  if (metadata.getDataValueId() == 6) {
                     this.logHealthDelta((Float)metadata.getObject() - minecraft.thePlayer.getHealth());
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onMovementInput(MovementInputEvent event) {
      if (this.isEnabled() && this.moveFix.getValue() == 1) {
         if (this.rotations.getValue() != 3) {
            if (RotationState.isActive() && RotationState.movementPriority() == 1.0F && MovementUtils.hasMovementInput()) {
               MovementUtils.correctMovementForYaw(RotationState.movementYaw());
            }
         }
      }
   }

   @EventHandler
   public void onRenderWorld(RenderWorldEvent event) {
      if (this.isEnabled()
         && this.showTarget.getValue() != 0
         && this.currentTarget != null
         && EntityUtils.isLoaded(this.currentTarget.entity)
         && this.isCombatInputAllowed()) {
         Color color = Color.WHITE;
         if (this.showTarget.getValue() == 1) {
            color = this.currentTarget.entity.hurtTime > 0 ? new Color(16733525) : new Color(5635925);
         } else if (this.showTarget.getValue() == 2) {
            color = ((HUDModule)MyauClient.moduleManager.modules.get(HUDModule.class)).getColor(System.currentTimeMillis());
         }

         RenderUtils.beginOverlay();
         RenderUtils.fillEntityBounds(this.currentTarget.entity, color.getRed(), color.getGreen(), color.getBlue(), 63, 0.1F);
         RenderUtils.endOverlay();
      }
   }

   @EventHandler
   public void onLeftClick(LeftClickEvent event) {
      if (this.autoBlockActive) {
         event.setCancelled(true);
      } else {
         if (this.isEnabled() && this.canRunCombat() && this.currentTarget != null && this.isWithinSwingRange(this.currentTarget.distance)) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onRightClick(RightClickEvent event) {
      if (this.autoBlockActive) {
         event.setCancelled(true);
      } else {
         if (this.isEnabled() && this.canRunCombat() && this.currentTarget != null && this.isWithinSwingRange(this.currentTarget.distance)) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onBlockClick(BlockClickEvent event) {
      if (this.autoBlockActive) {
         event.setCancelled(true);
      } else {
         if (this.isEnabled() && this.canRunCombat() && this.currentTarget != null && this.isWithinSwingRange(this.currentTarget.distance)) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onStopUsingItem(StopUsingItemEvent event) {
      if (this.autoBlockActive) {
         event.setCancelled(true);
      }
   }

   @Override
   public void onEnable() {
      this.currentTarget = null;
      this.hasTargetInAutoBlockRange = false;
      this.targetIndex = 0;
      this.attackedThisUpdate = false;
      this.attackCooldownMillis = 0L;
      this.blockHoldMillis = 0L;
      this.blockReleaseDelayMillis = 0L;
      this.autoBlockPhase = 0;
   }

   @Override
   public void onDisable() {
      MyauClient.outgoingPacketBuffer.setEnabled(false, OutgoingBufferOwner.AUTO_BLOCK);
      if (this.serverBlocking) {
         minecraft.thePlayer.stopUsingItem();
      }

      this.currentTarget = null;
      this.hasTargetInAutoBlockRange = false;
      this.bufferRestartPending = false;
      this.serverBlocking = false;
      this.autoBlockActive = false;
      this.renderBlocking = false;
      this.autoBlockPhase = 0;
      this.blockHoldMillis = 0L;
      this.blockReleaseDelayMillis = 0L;
   }

   @Override
   public void onSettingChanged(String settingName) {
      if (this.swingRange.getName().equals(settingName) && this.swingRange.getValue() < this.attackRange.getValue()) {
         this.attackRange.setValue(this.swingRange.getValue());
      } else if (this.attackRange.getName().equals(settingName) && this.attackRange.getValue() > this.swingRange.getValue()) {
         this.swingRange.setValue(this.attackRange.getValue());
      }

      if (this.minAps.getValue() > this.maxAps.getValue()) {
         if (this.minAps.getName().equals(settingName)) {
            this.maxAps.setValue(this.minAps.getValue());
         } else {
            this.minAps.setValue(this.maxAps.getValue());
         }
      }
   }

   @Override
   public String[] getHudInfo() {
      return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeName())};
   }

   private int compareTargetCandidates(AuraTarget first, AuraTarget second) {
      int comparison;
      switch (this.sort.getValue()) {
         case 1:
            comparison = Float.compare(EntityUtils.armorAdjustedHealth(first.entity), EntityUtils.armorAdjustedHealth(second.entity));
            break;
         case 2:
            comparison = Integer.compare(first.entity.hurtResistantTime, second.entity.hurtResistantTime);
            break;
         case 3:
            comparison = Float.compare(first.angularDistance, second.angularDistance);
            break;
         default:
            comparison = Double.compare(first.distance, second.distance);
      }

      return comparison != 0 ? comparison : Double.compare(first.distance, second.distance);
   }

   private boolean matchesCurrentTarget(AuraTarget candidate) {
      return this.currentTarget != null && candidate.entity == this.currentTarget.entity;
   }

   private boolean shouldDiscardNonEnemyCandidate(AuraTarget candidate) {
      return !this.isEnemyPlayer(candidate.entity);
   }

   private boolean isEnemyCandidate(AuraTarget candidate) {
      return this.isEnemyPlayer(candidate.entity);
   }

   private boolean shouldDiscardOutsideAttackRange(AuraTarget candidate) {
      return !this.isWithinAttackRange(candidate.distance);
   }

   private boolean isCandidateWithinAttackRange(AuraTarget candidate) {
      return this.isWithinAttackRange(candidate.distance);
   }

   private boolean shouldDiscardOutsideSwingRange(AuraTarget candidate) {
      return !this.isWithinSwingRange(candidate.distance);
   }

   private boolean isCandidateWithinSwingRange(AuraTarget candidate) {
      return this.isWithinSwingRange(candidate.distance);
   }

   private static boolean shouldDiscardUnusableAim(AuraTarget candidate) {
      return !candidate.hasUsableAim;
   }

   private static boolean hasCandidateUsableAim(AuraTarget candidate) {
      return candidate.hasUsableAim;
   }

   private boolean isCandidateWithinAutoBlockRange(AuraTarget candidate) {
      return this.isWithinAutoBlockRange(candidate.distance);
   }

   private boolean isAutoBlockRangeVisible() {
      return this.autoBlock.getValue() != 0;
   }

   private boolean isAutoBlockHurtTimeVisible() {
      int mode = this.autoBlock.getValue();
      return mode != 0 && mode != 8;
   }

   private boolean isAutoBlockDelayVisible() {
      int mode = this.autoBlock.getValue();
      return mode != 0 && mode != 1 && mode != 8;
   }

   private boolean isAutoBlockHoldVisible() {
      int mode = this.autoBlock.getValue();
      return mode != 0 && mode != 8;
   }

   private boolean isAutoBlockNoSlowVisible() {
      return this.autoBlock.getValue() != 0;
   }

   private boolean isAutoBlockRequirePressVisible() {
      return this.autoBlock.getValue() != 0;
   }
}
