package myau;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

public class HitSelectModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   Map<EntityLivingBase, Integer> remainingHurtTicks = new HashMap<>();
   Map<EntityLivingBase, Float> previousAttackDamage = new HashMap<>();
   public final IntegerSetting hurtTime = new IntegerSetting("hurt-time", 4, 0, 10);
   public final BooleanSetting swing = new BooleanSetting("swing", true);

   public HitSelectModule() {
      super("HitSelect", false, 0);
   }

   public boolean shouldDelayAttack(EntityLivingBase target) {
      float attackDamage = PlayerUtils.estimateAttackDamage(target);
      int remaining = this.remainingHurtTicks.getOrDefault(target, 0);
      if (remaining <= 0) {
         this.remainingHurtTicks.put(target, target.hurtTime);
         this.previousAttackDamage.put(target, attackDamage);
         return false;
      } else {
         remaining = this.remainingHurtTicks.getOrDefault(target, 0);
         if (remaining <= this.hurtTime.getValue()) {
            return false;
         } else if (this.previousAttackDamage.getOrDefault(target, 0.0F) > attackDamage) {
            this.previousAttackDamage.put(target, attackDamage);
            return false;
         } else if (minecraft.thePlayer.hurtTime <= 0 && minecraft.thePlayer.isSprinting()) {
            if (this.swing.getValue()) {
               PlayerUtils.swingLocally();
            }

            return true;
         } else {
            return false;
         }
      }
   }

   @EventHandler(
      priority = -100
   )
   public void onLeftClick(LeftClickEvent event) {
      boolean enabled = this.isEnabled();
      if (enabled && !event.isCancelled()) {
         if (minecraft.objectMouseOver != null) {
            if (minecraft.objectMouseOver.typeOfHit == MovingObjectType.ENTITY) {
               if (minecraft.objectMouseOver.entityHit instanceof EntityLivingBase) {
                  EntityLivingBase target = (EntityLivingBase)minecraft.objectMouseOver.entityHit;
                  if (this.shouldDelayAttack(target)) {
                     event.setCancelled(true);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onPreTick(PreTickEvent event) {
      if (this.isEnabled()) {
         this.remainingHurtTicks.replaceAll(HitSelectModule::decrementHurtTicks);
      }
   }

   private static Integer decrementHurtTicks(EntityLivingBase target, Integer remaining) {
      return remaining > 0 ? remaining - 1 : remaining;
   }

   @Override
   public void onDisable() {
      this.remainingHurtTicks.clear();
      this.previousAttackDamage.clear();
   }
}
