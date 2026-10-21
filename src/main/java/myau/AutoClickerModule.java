package myau;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.WorldSettings.GameType;

public class AutoClickerModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private boolean restoreAttackKey;
   private long clickCooldownMillis;
   private boolean restoreUseItemKey;
   private long blockHitHoldMillis;
   private long blockHitCycleMillis;
   public final IntegerSetting minCps = new IntegerSetting("min-cps", 10, 1, 20);
   public final IntegerSetting maxCps = new IntegerSetting("max-cps", 14, 1, 20);
   public final BooleanSetting blockHit = new BooleanSetting("block-hit", false);
   public final FloatSetting blockHitHold = new FloatSetting("block-hit-hold", 1.5F, 1.0F, 20.0F, this.blockHit::getValue);
   public final FloatSetting blockHitDelay = new FloatSetting("block-hit-delay", 0.0F, 0.0F, 20.0F, this.blockHit::getValue);
   public final IntegerSetting blockHitHurtTime = new IntegerSetting("block-hit-hurt-time", 6, 0, 10, this.blockHit::getValue);
   public final BooleanSetting weaponsOnly = new BooleanSetting("weapons-only", true);
   public final BooleanSetting allowTools = new BooleanSetting("allow-tools", false, this.weaponsOnly::getValue);
   public final BooleanSetting breakBlocks = new BooleanSetting("break-blocks", true);

   private long nextClickDelayMillis() {
      return 1000L / RandomUtils.randomLong(this.minCps.getValue().longValue(), this.maxCps.getValue().longValue());
   }

   private long blockHitHoldDurationMillis() {
      return NumericConversions.truncateFloatToLong(this.blockHitHold.getValue() * 50.0F);
   }

   private long blockHitDelayDurationMillis() {
      return NumericConversions.truncateFloatToLong(this.blockHitDelay.getValue() * 50.0F);
   }

   private boolean isLookingAtBlock() {
      MovingObjectPosition hitResult = minecraft.objectMouseOver;
      return hitResult != null && hitResult.typeOfHit == MovingObjectType.BLOCK;
   }

   private boolean canAutoClick() {
      if (!this.weaponsOnly.getValue() || ItemUtils.isHoldingWeapon() || this.allowTools.getValue() && ItemUtils.isHoldingTool()) {
         if (this.breakBlocks.getValue() && this.isLookingAtBlock()) {
            GameType gameType = minecraft.playerController.getCurrentGameType();
            return gameType != GameType.SURVIVAL && gameType != GameType.CREATIVE;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public AutoClickerModule() {
      super("AutoClicker", false, 0);
   }

   @EventHandler
   public void onPreTick(PreTickEvent event) {
      if (this.clickCooldownMillis > 0L) {
         this.clickCooldownMillis -= 50L;
      }

      if (this.blockHitHoldMillis > 0L) {
         this.blockHitHoldMillis -= 50L;
      }

      if (this.blockHitCycleMillis > 0L) {
         this.blockHitCycleMillis -= 50L;
      }

      if (minecraft.currentScreen != null) {
         this.restoreAttackKey = false;
         this.restoreUseItemKey = false;
      } else {
         if (this.restoreAttackKey) {
            this.restoreAttackKey = false;
            InputUtils.updateKeyState(minecraft.gameSettings.keyBindAttack.getKeyCode());
         }

         if (this.restoreUseItemKey) {
            this.restoreUseItemKey = false;
            InputUtils.updateKeyState(minecraft.gameSettings.keyBindUseItem.getKeyCode());
         }

         if (this.isEnabled() && this.canAutoClick() && minecraft.gameSettings.keyBindAttack.isKeyDown()) {
            if (!minecraft.thePlayer.isUsingItem()) {
               while (this.clickCooldownMillis <= 0L) {
                  this.restoreAttackKey = true;
                  this.clickCooldownMillis = this.clickCooldownMillis + this.nextClickDelayMillis();
                  InputUtils.setKeyState(minecraft.gameSettings.keyBindAttack.getKeyCode(), false);
                  InputUtils.onKeyTick(minecraft.gameSettings.keyBindAttack.getKeyCode());
               }
            }

            if (this.blockHit.getValue() && minecraft.gameSettings.keyBindUseItem.isKeyDown() && ItemUtils.isHoldingSword()) {
               boolean hurtTimeTooHigh = MyauClient.hurtTimeTracker.remainingHurtTicks() > this.blockHitHurtTime.getValue();
               if (this.blockHitHoldMillis <= 0L || hurtTimeTooHigh) {
                  this.restoreUseItemKey = true;
                  InputUtils.setKeyState(minecraft.gameSettings.keyBindUseItem.getKeyCode(), false);
                  if (this.blockHitCycleMillis > 0L || hurtTimeTooHigh) {
                     return;
                  }
               }

               if (this.blockHitHoldMillis <= 0L && !minecraft.thePlayer.isUsingItem()) {
                  long holdDurationMillis = this.blockHitHoldDurationMillis();
                  this.blockHitHoldMillis += holdDurationMillis;
                  this.blockHitCycleMillis = this.blockHitCycleMillis + holdDurationMillis + 50L + this.blockHitDelayDurationMillis();
                  InputUtils.onKeyTick(minecraft.gameSettings.keyBindUseItem.getKeyCode());
               }
            }
         }
      }
   }

   @EventHandler(
      priority = -100
   )
   public void onLeftClick(LeftClickEvent event) {
      if (this.isEnabled() && !event.isCancelled() && !this.restoreAttackKey) {
         this.clickCooldownMillis = this.clickCooldownMillis + this.nextClickDelayMillis();
      }
   }

   @EventHandler(
      priority = -100
   )
   public void onRightClick(RightClickEvent event) {
      if (this.isEnabled() && !event.isCancelled() && ItemUtils.isHoldingSword() && !this.restoreUseItemKey) {
         this.blockHitHoldMillis = this.blockHitHoldMillis + this.blockHitHoldDurationMillis();
      }
   }

   @Override
   public void onEnable() {
      this.clickCooldownMillis = 0L;
      this.blockHitHoldMillis = 0L;
      this.blockHitCycleMillis = 0L;
   }

   @Override
   public void onSettingChanged(String name) {
      if (this.minCps.getName().equals(name)) {
         if (this.minCps.getValue() > this.maxCps.getValue()) {
            this.maxCps.setValue(this.minCps.getValue());
         }
      } else if (this.maxCps.getName().equals(name) && this.minCps.getValue() > this.maxCps.getValue()) {
         this.minCps.setValue(this.maxCps.getValue());
      }
   }

   @Override
   public String[] getHudInfo() {
      return Objects.equals(this.minCps.getValue(), this.maxCps.getValue())
         ? new String[]{this.minCps.getValue().toString()}
         : new String[]{String.format("%d-%d", this.minCps.getValue(), this.maxCps.getValue())};
   }
}
