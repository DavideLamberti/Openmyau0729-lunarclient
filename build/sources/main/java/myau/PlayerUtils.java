package myau;

import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ForgeHooks;

public final class PlayerUtils {
   private static final Minecraft minecraft = Minecraft.getMinecraft();

   private PlayerUtils() {
   }

   public static boolean isJumpKeyDown() {
      return isGameplayKeyDown(minecraft.gameSettings.keyBindJump.getKeyCode());
   }

   public static boolean isSneakKeyDown() {
      return isGameplayKeyDown(minecraft.gameSettings.keyBindSneak.getKeyCode());
   }

   public static boolean isLeftKeyDown() {
      return isGameplayKeyDown(minecraft.gameSettings.keyBindLeft.getKeyCode());
   }

   public static boolean isRightKeyDown() {
      return isGameplayKeyDown(minecraft.gameSettings.keyBindRight.getKeyCode());
   }

   public static boolean isAttackKeyDown() {
      return isGameplayKeyDown(minecraft.gameSettings.keyBindAttack.getKeyCode());
   }

   public static boolean isUseItemKeyDown() {
      return isGameplayKeyDown(minecraft.gameSettings.keyBindUseItem.getKeyCode());
   }

   private static boolean isGameplayKeyDown(int keyCode) {
      return minecraft.currentScreen == null && InputUtils.isKeyDown(keyCode);
   }

   public static boolean wouldTakeFallDamage(float safeFallDistance) {
      if (!minecraft.thePlayer.capabilities.allowFlying && !minecraft.thePlayer.capabilities.disableDamage) {
         PotionEffect jumpBoost = minecraft.thePlayer.getActivePotionEffect(Potion.jump);
         float jumpReduction = jumpBoost == null ? 0.0F : (float)(jumpBoost.getAmplifier() + 1);
         float projectedFallDistance = minecraft.thePlayer.fallDistance;
         if (minecraft.thePlayer.motionY < -0.67 || !hasCollisionBelow()) {
            projectedFallDistance -= (float)minecraft.thePlayer.motionY;
         }

         return MathHelper.ceiling_float_int(projectedFallDistance - safeFallDistance - jumpReduction) > 0;
      } else {
         return false;
      }
   }

   public static boolean isAirBelow(int depth) {
      if (!minecraft.thePlayer.capabilities.allowFlying && !minecraft.thePlayer.capabilities.disableDamage) {
         int playerY = MathHelper.floor_double(minecraft.thePlayer.posY);

         for (int offset = 0; offset <= depth; offset++) {
            int y = playerY - offset;
            if (y < 0) {
               break;
            }

            Block block = minecraft.theWorld
               .getBlockState(new BlockPos(minecraft.thePlayer.posX, (double)y, minecraft.thePlayer.posZ))
               .getBlock();
            if (!(block instanceof BlockAir)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean isOverVoid() {
      AxisAlignedBB bounds = minecraft.thePlayer.getEntityBoundingBox().expand(-1.0E-6, 0.0, -1.0E-6);
      return isBoundsOverVoid(bounds);
   }

   public static boolean isBoundsOverVoid(AxisAlignedBB bounds) {
      if (!minecraft.thePlayer.isInWater() && !minecraft.thePlayer.isInLava()) {
         int startY = MathHelper.floor_double(bounds.minY);
         if (startY < 0) {
            return true;
         } else {
            int minX = MathHelper.floor_double(bounds.minX);
            int maxX = MathHelper.floor_double(bounds.maxX + 1.0);
            int minZ = MathHelper.floor_double(bounds.minZ);
            int maxZ = MathHelper.floor_double(bounds.maxZ + 1.0);

            for (int x = minX; x < maxX; x++) {
               for (int z = minZ; z < maxZ; z++) {
                  for (int y = startY; y >= 0; y--) {
                     if (!BlockUtils.isReplaceableAt(new BlockPos(x, y, z))) {
                        return false;
                     }
                  }
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean isOffsetCollisionFree(double offsetX, double offsetY, double offsetZ) {
      AxisAlignedBB bounds = minecraft.thePlayer.getEntityBoundingBox().offset(offsetX, offsetY, offsetZ);
      return minecraft.theWorld.getCollidingBoundingBoxes(minecraft.thePlayer, bounds).isEmpty();
   }

   public static boolean isNarrowOffsetCollisionFree(double offsetX, double offsetY, double offsetZ, float widthFactor) {
      double horizontalInset = (double)(minecraft.thePlayer.width / -2.0F * (1.0F - widthFactor));
      AxisAlignedBB bounds = minecraft.thePlayer
         .getEntityBoundingBox()
         .offset(offsetX, offsetY, offsetZ)
         .expand(horizontalInset, 0.0, horizontalInset);
      return minecraft.theWorld.getCollidingBoundingBoxes(minecraft.thePlayer, bounds).isEmpty();
   }

   public static boolean hasCollisionBelow() {
      AxisAlignedBB bounds = minecraft.thePlayer.getEntityBoundingBox().offset(0.0, -1.0, 0.0);
      return !minecraft.theWorld.getCollidingBoundingBoxes(minecraft.thePlayer, bounds).isEmpty();
   }

   public static boolean hasCollisionAbove() {
      AxisAlignedBB bounds = minecraft.thePlayer.getEntityBoundingBox().offset(0.0, 1.0, 0.0);
      return !minecraft.theWorld.getCollidingBoundingBoxes(minecraft.thePlayer, bounds).isEmpty();
   }

   public static boolean isBlockWithinReach(BlockPos position, double reach) {
      return isBlockWithinReachFrom(
         position,
         minecraft.thePlayer.posX,
         minecraft.thePlayer.posY + (double)minecraft.thePlayer.getEyeHeight(),
         minecraft.thePlayer.posZ,
         reach
      );
   }

   public static boolean isBlockWithinReachFrom(BlockPos position, double eyeX, double eyeY, double eyeZ, double reach) {
      return position.distanceSqToCenter(eyeX, eyeY, eyeZ) < reach * reach;
   }

   public static void attackLocally(Entity target) {
      if (ForgeHooks.onPlayerAttackTarget(minecraft.thePlayer, target) && target.canAttackWithItem() && !target.hitByEntity(minecraft.thePlayer)) {
         float baseDamage = (float)minecraft.thePlayer.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
         ItemStack heldItem = minecraft.thePlayer.getHeldItem();
         EnumCreatureAttribute creatureAttribute = target instanceof EntityLivingBase
            ? ((EntityLivingBase)target).getCreatureAttribute()
            : EnumCreatureAttribute.UNDEFINED;
         float enchantmentDamage = EnchantmentHelper.getModifierForCreature(heldItem, creatureAttribute);
         int knockbackLevel = EnchantmentHelper.getKnockbackModifier(minecraft.thePlayer);
         if (minecraft.thePlayer.isSprinting()) {
            knockbackLevel++;
         }

         if (!(baseDamage <= 0.0F) || !(enchantmentDamage <= 0.0F)) {
            boolean criticalHit = isCriticalAttack();
            if (criticalHit && baseDamage > 0.0F) {
               baseDamage *= 1.5F;
            }

            float totalDamage = baseDamage + enchantmentDamage;
            int fireAspectLevel = EnchantmentHelper.getFireAspectModifier(minecraft.thePlayer);
            boolean temporaryFire = false;
            if (target instanceof EntityLivingBase && fireAspectLevel > 0 && !target.isBurning()) {
               temporaryFire = true;
               target.setFire(1);
            }

            double previousMotionX = target.motionX;
            double previousMotionY = target.motionY;
            double previousMotionZ = target.motionZ;
            boolean hit = target.attackEntityFrom(DamageSource.causePlayerDamage(minecraft.thePlayer), totalDamage);
            if (!hit) {
               if (temporaryFire) {
                  target.extinguish();
               }
            } else {
               if (knockbackLevel > 0) {
                  target.addVelocity(
                     (double)(-MathHelper.sin(minecraft.thePlayer.rotationYaw * (float) Math.PI / 180.0F) * (float)knockbackLevel * 0.5F),
                     0.1,
                     (double)(MathHelper.cos(minecraft.thePlayer.rotationYaw * (float) Math.PI / 180.0F) * (float)knockbackLevel * 0.5F)
                  );
                  applyPostHitSprintSlowdown(target);
               }

               if (target instanceof EntityPlayerMP && target.velocityChanged) {
                  ((EntityPlayerMP)target).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(target));
                  target.velocityChanged = false;
                  target.motionX = previousMotionX;
                  target.motionY = previousMotionY;
                  target.motionZ = previousMotionZ;
               }

               if (criticalHit) {
                  minecraft.thePlayer.onCriticalHit(target);
               }

               if (enchantmentDamage > 0.0F) {
                  minecraft.thePlayer.onEnchantmentCritical(target);
               }

               if (totalDamage >= 18.0F) {
                  minecraft.thePlayer.triggerAchievement(AchievementList.overkill);
               }

               minecraft.thePlayer.setLastAttacker(target);
               if (target instanceof EntityLivingBase) {
                  EnchantmentHelper.applyThornEnchantments((EntityLivingBase)target, minecraft.thePlayer);
               }

               EnchantmentHelper.applyArthropodEnchantments(minecraft.thePlayer, target);
               if (target instanceof EntityLivingBase) {
                  minecraft.thePlayer.addStat(StatList.damageDealtStat, Math.round(totalDamage * 10.0F));
                  if (fireAspectLevel > 0) {
                     target.setFire(fireAspectLevel * 4);
                  }
               }

               minecraft.thePlayer.addExhaustion(0.3F);
            }
         }
      }
   }

   private static boolean isCriticalAttack() {
      return minecraft.thePlayer.fallDistance > 0.0F
         && !minecraft.thePlayer.onGround
         && !minecraft.thePlayer.isOnLadder()
         && !minecraft.thePlayer.isInWater()
         && !minecraft.thePlayer.isPotionActive(Potion.blindness)
         && minecraft.thePlayer.ridingEntity == null;
   }

   private static void applyPostHitSprintSlowdown(Entity target) {
      KeepSprintModule keepSprint = (KeepSprintModule)MyauClient.moduleManager.modules.get(KeepSprintModule.class);
      boolean useKeepSprint = keepSprint != null && keepSprint.isEnabled();
      if (useKeepSprint && keepSprint.groundOnly.getValue() && !minecraft.thePlayer.onGround) {
         useKeepSprint = false;
      }

      if (useKeepSprint && keepSprint.reachOnly.getValue() && RotationUtils.distanceToEntity(target) <= 3.0) {
         useKeepSprint = false;
      }

      if (useKeepSprint) {
         double multiplier = 0.6 + 0.4 * (1.0 - keepSprint.slowdown.getValue().doubleValue() / 100.0);
         minecraft.thePlayer.motionX *= multiplier;
         minecraft.thePlayer.motionZ *= multiplier;
      } else {
         minecraft.thePlayer.motionX *= 0.6;
         minecraft.thePlayer.motionZ *= 0.6;
         minecraft.thePlayer.setSprinting(false);
      }
   }

   public static float estimateAttackDamage(Entity target) {
      ItemStack heldItem = minecraft.thePlayer.getHeldItem();
      float baseDamage = 1.0F;
      if (heldItem != null) {
         Multimap<String, AttributeModifier> modifiers = heldItem.getAttributeModifiers();
         String attackDamageAttribute = SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName();

         for (AttributeModifier modifier : modifiers.get(attackDamageAttribute)) {
            baseDamage += (float)modifier.getAmount();
         }
      }

      EnumCreatureAttribute creatureAttribute = target instanceof EntityLivingBase
         ? ((EntityLivingBase)target).getCreatureAttribute()
         : EnumCreatureAttribute.UNDEFINED;
      float enchantmentDamage = EnchantmentHelper.getModifierForCreature(heldItem, creatureAttribute);
      if (baseDamage <= 0.0F && enchantmentDamage <= 0.0F) {
         return 0.0F;
      } else {
         if (isCriticalAttack() && baseDamage > 0.0F) {
            baseDamage *= 1.5F;
         }

         return baseDamage + enchantmentDamage;
      }
   }

   public static int swingDuration() {
      if (minecraft.thePlayer.isPotionActive(Potion.digSpeed)) {
         return 6 - (1 + minecraft.thePlayer.getActivePotionEffect(Potion.digSpeed).getAmplifier());
      } else {
         return minecraft.thePlayer.isPotionActive(Potion.digSlowdown)
            ? 6 + (1 + minecraft.thePlayer.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2
            : 6;
      }
   }

   public static void swingLocally() {
      if (!minecraft.thePlayer.isSwingInProgress
         || minecraft.thePlayer.swingProgressInt >= swingDuration() / 2
         || minecraft.thePlayer.swingProgressInt < 0) {
         minecraft.thePlayer.swingProgressInt = -1;
         minecraft.thePlayer.isSwingInProgress = true;
      }
   }
}
