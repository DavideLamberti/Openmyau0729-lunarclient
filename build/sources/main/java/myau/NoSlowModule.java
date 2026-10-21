package myau;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.BlockPos;

public class NoSlowModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private int lastFloatUseSlot = -1;
   public final ModeSetting swordMode = new ModeSetting("sword-mode", 1, new String[]{"NONE", "VANILLA"});
   public final PercentSetting swordMotion = new PercentSetting("sword-motion", 20, this::isSwordMotionVisible);
   public final BooleanSetting swordSprint = new BooleanSetting("sword-sprint", true, this::isSwordSprintVisible);
   public final ModeSetting foodMode = new ModeSetting("food-mode", 0, new String[]{"NONE", "VANILLA", "FLOAT"});
   public final PercentSetting foodMotion = new PercentSetting("food-motion", 20, this::isFoodMotionVisible);
   public final BooleanSetting foodSprint = new BooleanSetting("food-sprint", true, this::isFoodSprintVisible);
   public final ModeSetting bowMode = new ModeSetting("bow-mode", 0, new String[]{"NONE", "VANILLA", "FLOAT"});
   public final PercentSetting bowMotion = new PercentSetting("bow-motion", 20, this::isBowMotionVisible);
   public final BooleanSetting bowSprint = new BooleanSetting("bow-sprint", true, this::isBowSprintVisible);

   public NoSlowModule() {
      super("NoSlow", false, 0);
   }

   public boolean isSwordModeActive() {
      return this.swordMode.getValue() != 0 && ItemUtils.isHoldingSword();
   }

   public boolean isFoodModeActive() {
      return this.foodMode.getValue() != 0 && ItemUtils.isHoldingConsumable();
   }

   public boolean isBowModeActive() {
      return this.bowMode.getValue() != 0 && ItemUtils.isHoldingBow();
   }

   public boolean isFloatModeActive() {
      return this.foodMode.getValue() == 2 && ItemUtils.isHoldingConsumable() || this.bowMode.getValue() == 2 && ItemUtils.isHoldingBow();
   }

   public boolean shouldSuppressItemSlowdown() {
      return minecraft.thePlayer.isUsingItem() && (this.isSwordModeActive() || this.isFoodModeActive() || this.isBowModeActive());
   }

   public boolean shouldAllowSprint() {
      return this.isSwordModeActive() && this.swordSprint.getValue()
         || this.isFoodModeActive() && this.foodSprint.getValue()
         || this.isBowModeActive() && this.bowSprint.getValue();
   }

   public int getMotionPercent() {
      if (ItemUtils.isHoldingSword()) {
         return this.swordMotion.getValue();
      } else if (ItemUtils.isHoldingConsumable()) {
         return this.foodMotion.getValue();
      } else {
         return ItemUtils.isHoldingBow() ? this.bowMotion.getValue() : 100;
      }
   }

   @EventHandler
   public void onLivingUpdate(LivingUpdateEvent event) {
      if (this.isEnabled() && this.shouldSuppressItemSlowdown()) {
         KillAuraModule killAura = (KillAuraModule)MyauClient.moduleManager.modules.get(KillAuraModule.class);
         if (!killAura.isAutoBlockActive() || !killAura.autoBlockNoSlow.getValue()) {
            float motionScale = (float)this.getMotionPercent() / 100.0F;
            minecraft.thePlayer.movementInput.moveForward *= motionScale;
            minecraft.thePlayer.movementInput.moveStrafe *= motionScale;
            if (!this.shouldAllowSprint()) {
               minecraft.thePlayer.setSprinting(false);
            }
         }
      }
   }

   @EventHandler
   public void onPreWalkingUpdate(PreWalkingUpdateEvent event) {
      if (this.isEnabled() && this.isFloatModeActive()) {
         int selectedSlot = minecraft.thePlayer.inventory.currentItem;
         if (this.lastFloatUseSlot != selectedSlot && PlayerUtils.isUseItemKeyDown()) {
            this.lastFloatUseSlot = selectedSlot;
            MyauClient.groundSpoofManager.setRequest(true, GroundSpoofOwner.NO_SLOW);
         }
      } else {
         this.lastFloatUseSlot = -1;
         MyauClient.groundSpoofManager.setRequest(false, GroundSpoofOwner.NO_SLOW);
      }
   }

   @EventHandler
   public void onRightClick(RightClickEvent event) {
      if (this.isEnabled()) {
         if (minecraft.objectMouseOver != null) {
            switch (minecraft.objectMouseOver.typeOfHit) {
               case BLOCK:
                  BlockPos blockPos = minecraft.objectMouseOver.getBlockPos();
                  if (BlockUtils.isInteractableAt(blockPos) && !PlayerUtils.isSneakKeyDown()) {
                     return;
                  }
                  break;
               case ENTITY:
                  Entity entity = minecraft.objectMouseOver.entityHit;
                  if (entity instanceof EntityVillager) {
                     return;
                  }

                  if (entity instanceof EntityLivingBase && EntityUtils.isShopNPC((EntityLivingBase)entity)) {
                     return;
                  }
            }
         }

         if (this.isFloatModeActive() && !MyauClient.groundSpoofManager.isOffsetActive() && minecraft.thePlayer.onGround) {
            event.setCancelled(true);
            minecraft.thePlayer.motionY = 0.42F;
         }
      }
   }

   private boolean isBowSprintVisible() {
      return this.bowMode.getValue() != 0;
   }

   private boolean isBowMotionVisible() {
      return this.bowMode.getValue() != 0;
   }

   private boolean isFoodSprintVisible() {
      return this.foodMode.getValue() != 0;
   }

   private boolean isFoodMotionVisible() {
      return this.foodMode.getValue() != 0;
   }

   private boolean isSwordSprintVisible() {
      return this.swordMode.getValue() != 0;
   }

   private boolean isSwordMotionVisible() {
      return this.swordMode.getValue() != 0;
   }
}
