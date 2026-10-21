package myau;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

public class AimAssistModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private static final long ATTACK_COOLDOWN_MILLIS = 350L;
   private final ElapsedTimer lastAttackTimer = new ElapsedTimer();
   private final float[] snappedRotations = new float[]{-180.0F, 0.0F};
   private final float[] smoothedRotations = new float[]{-180.0F, 0.0F};
   public final FloatSetting horizontalSpeed = new FloatSetting("horizontal-speed", 3.0F, 0.0F, 30.0F);
   public final FloatSetting verticalSpeed = new FloatSetting("vertical-speed", 0.0F, 0.0F, 30.0F);
   public final PercentSetting smoothing = new PercentSetting("smoothing", 50);
   public final FloatSetting range = new FloatSetting("range", 4.5F, 3.0F, 8.0F);
   public final IntegerSetting fov = new IntegerSetting("fov", 90, 30, 360);
   public final BooleanSetting requirePress = new BooleanSetting("require-press", true);
   public final BooleanSetting allowMining = new BooleanSetting("allow-mining", true);
   public final BooleanSetting weaponsOnly = new BooleanSetting("weapons-only", true);
   public final BooleanSetting allowTools = new BooleanSetting("allow-tools", false, this.weaponsOnly::getValue);
   public final BooleanSetting botCheck = new BooleanSetting("bot-check", true);
   public final BooleanSetting teams = new BooleanSetting("teams", true);

   public AimAssistModule() {
      super("AimAssist", false, 0);
   }

   private boolean isValidAimTarget(EntityLivingBase candidate) {
      if (candidate == minecraft.thePlayer || candidate == minecraft.thePlayer.ridingEntity) {
         return false;
      } else if (candidate == minecraft.getRenderViewEntity() || candidate == minecraft.getRenderViewEntity().ridingEntity) {
         return false;
      } else if (candidate.deathTime > 0) {
         return false;
      } else if (RotationUtils.distanceToEntity(candidate) > (double)this.range.getValue().floatValue()) {
         return false;
      } else if (RotationUtils.entityAimError(candidate) > (float)this.fov.getValue().intValue()) {
         return false;
      } else if (RotationUtils.traceToEntity(candidate) != null) {
         return false;
      } else if (!(candidate instanceof EntityPlayer)) {
         return true;
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
   }

   private boolean isWithinEffectiveReach(EntityPlayer player) {
      ReachModule reach = (ReachModule)MyauClient.moduleManager.modules.get(ReachModule.class);
      double effectiveReach = reach.isEnabled() ? (double)reach.range.getValue().floatValue() : 3.0;
      return RotationUtils.distanceToEntity(player) <= effectiveReach;
   }

   private EntityLivingBase findTarget() {
      KillAuraModule killAura = (KillAuraModule)MyauClient.moduleManager.modules.get(KillAuraModule.class);
      if (killAura.isEnabled()) {
         EntityLivingBase auraTarget = killAura.getTargetEntity();
         if (auraTarget != null && this.isValidAimTarget(auraTarget)) {
            return auraTarget;
         }
      }

      List<EntityPlayer> candidates = minecraft.theWorld
         .loadedEntityList
         .stream()
         .filter(EntityPlayer.class::isInstance)
         .map(EntityPlayer.class::cast)
         .filter(this::isValidAimTarget)
         .sorted(Comparator.comparingDouble(RotationUtils::distanceToEntity))
         .collect(Collectors.toList());
      if (candidates.isEmpty()) {
         return null;
      } else {
         if (candidates.stream().anyMatch(this::isWithinEffectiveReach)) {
            candidates.removeIf(player -> !this.isWithinEffectiveReach(player));
         }

         return (EntityLivingBase)candidates.get(0);
      }
   }

   private boolean isLookingAtBlock() {
      MovingObjectPosition hitResult = minecraft.objectMouseOver;
      return hitResult != null && hitResult.typeOfHit == MovingObjectType.BLOCK;
   }

   @EventHandler
   public void onPostTick(PostTickEvent event) {
      if (this.isEnabled() && minecraft.currentScreen == null) {
         if (!this.weaponsOnly.getValue() || ItemUtils.isHoldingWeapon() || this.allowTools.getValue() && ItemUtils.isHoldingTool()) {
            boolean attackPressed = PlayerUtils.isAttackKeyDown();
            if (!this.allowMining.getValue() || !attackPressed || !this.isLookingAtBlock()) {
               if (!this.requirePress.getValue() || attackPressed || !this.lastAttackTimer.hasElapsed(350L)) {
                  EntityLivingBase target = this.findTarget();
                  if (target != null && !(RotationUtils.distanceToEntity(target) <= 0.0)) {
                     if (this.snappedRotations[0] != minecraft.thePlayer.rotationYaw) {
                        this.snappedRotations[0] = minecraft.thePlayer.rotationYaw;
                        this.smoothedRotations[0] = minecraft.thePlayer.rotationYaw;
                     }

                     if (this.snappedRotations[1] != minecraft.thePlayer.rotationPitch) {
                        this.snappedRotations[1] = minecraft.thePlayer.rotationPitch;
                        this.smoothedRotations[1] = minecraft.thePlayer.rotationPitch;
                     }

                     AxisAlignedBB bounds = target.getEntityBoundingBox();
                     double border = (double)target.getCollisionBorderSize();
                     float[] targetRotation = RotationUtils.aimAtBounds(bounds.expand(border, border, border));
                     float yawDelta = MathHelper.wrapAngleTo180_float(targetRotation[0] - this.smoothedRotations[0]);
                     float pitchDelta = targetRotation[1] - this.smoothedRotations[1];
                     if (Math.abs(yawDelta) < 0.5F) {
                        yawDelta = 0.0F;
                     }

                     if (Math.abs(pitchDelta) < 0.5F) {
                        pitchDelta = 0.0F;
                     }

                     yawDelta = RotationUtils.clampAngleChange(yawDelta, this.horizontalSpeed.getValue() * 6.0F * RandomUtils.randomFloat(0.75F, 1.0F));
                     pitchDelta = RotationUtils.clampAngleChange(pitchDelta, this.verticalSpeed.getValue() * 6.0F * RandomUtils.randomFloat(0.75F, 1.0F) * 0.5F);
                     float smoothingAmount = (float)this.smoothing.getValue().intValue() / 100.0F;
                     yawDelta = RotationUtils.scaleAngleChange(yawDelta, smoothingAmount + RandomUtils.randomFloat(-0.05F, 0.05F));
                     pitchDelta = RotationUtils.scaleAngleChange(pitchDelta, smoothingAmount + RandomUtils.randomFloat(-0.05F, 0.05F));
                     if (yawDelta == 0.0F && pitchDelta != 0.0F) {
                        yawDelta += RandomUtils.randomFloat(0.1F, 0.2F) * (RandomUtils.randomBoolean() ? 1.0F : -1.0F);
                     } else if (yawDelta != 0.0F && pitchDelta == 0.0F) {
                        pitchDelta += RandomUtils.randomFloat(0.1F, 0.2F) * (RandomUtils.randomBoolean() ? 1.0F : -1.0F);
                     }

                     this.smoothedRotations[0] = this.smoothedRotations[0] + yawDelta;
                     this.smoothedRotations[1] = this.smoothedRotations[1] + pitchDelta;
                     this.smoothedRotations[1] = MathHelper.clamp_float(this.smoothedRotations[1], -90.0F, 90.0F);
                     this.snappedRotations[0] = RotationUtils.snapToMouseSensitivity(this.smoothedRotations[0], this.snappedRotations[0]);
                     this.snappedRotations[1] = RotationUtils.snapToMouseSensitivity(this.smoothedRotations[1], this.snappedRotations[1]);
                     this.snappedRotations[1] = MathHelper.clamp_float(this.snappedRotations[1], -90.0F, 90.0F);
                     MyauClient.rotationSmoother.requestRotation(this.snappedRotations[0], this.snappedRotations[1], 0, false);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onKeyPress(KeyPressEvent event) {
      if (event.getKeyCode() == minecraft.gameSettings.keyBindAttack.getKeyCode()) {
         AutoClickerModule autoClicker = (AutoClickerModule)MyauClient.moduleManager.modules.get(AutoClickerModule.class);
         if (!autoClicker.isEnabled()) {
            this.lastAttackTimer.reset();
         }
      }
   }
}
