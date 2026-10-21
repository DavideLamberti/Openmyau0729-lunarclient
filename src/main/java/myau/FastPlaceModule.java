package myau;

import java.text.DecimalFormat;
import myau.mixin.AccessorMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

public class FastPlaceModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private static final DecimalFormat DELAY_FORMAT = new DecimalFormat("0.0#");
   private long remainingDelayMillis = 0L;
   public final FloatSetting delay = new FloatSetting("delay", 1.0F, 1.0F, 3.0F);
   public final BooleanSetting blocksOnly = new BooleanSetting("blocks-only", true);
   public final BooleanSetting placeFix = new BooleanSetting("place-fix", true);

   public FastPlaceModule() {
      super("FastPlace", false, 0);
   }

   private boolean canPlaceImmediately() {
      ItemStack heldStack = minecraft.thePlayer.getHeldItem();
      if (heldStack == null) {
         return !this.blocksOnly.getValue();
      } else {
         Item heldItem = heldStack.getItem();
         if (heldItem instanceof ItemFishingRod) {
            return false;
         } else if (!(heldItem instanceof ItemBlock)) {
            return !this.blocksOnly.getValue();
         } else if (!this.placeFix.getValue()) {
            return true;
         } else {
            MovingObjectPosition hit = RotationUtils.traceBlocks(
               minecraft.thePlayer.rotationYaw, minecraft.thePlayer.rotationPitch, (double)minecraft.playerController.getBlockReachDistance(), 1.0F
            );
            return hit == null
               ? false
               : hit.typeOfHit == MovingObjectType.BLOCK
                  && ((ItemBlock)heldItem).canPlaceBlockOnSide(minecraft.theWorld, hit.getBlockPos(), hit.sideHit, minecraft.thePlayer, heldStack);
         }
      }
   }

   @EventHandler
   public void onPreTick(PreTickEvent event) {
      if (this.isEnabled()) {
         int rightClickDelay = ((AccessorMinecraft)minecraft).getRightClickDelayTimer();
         if (rightClickDelay == 4) {
            this.remainingDelayMillis = this.remainingDelayMillis + (long)(50.0F * this.delay.getValue());
         }

         if (this.remainingDelayMillis > 0L) {
            this.remainingDelayMillis -= 50L;
         }

         if (this.remainingDelayMillis <= 0L && rightClickDelay > 1 && this.canPlaceImmediately()) {
            ((AccessorMinecraft)minecraft).setRightClickDelayTimer(0);
         }
      }
   }

   @Override
   public void onDisable() {
      this.remainingDelayMillis = 0L;
   }

   @Override
   public String[] getHudInfo() {
      return new String[]{DELAY_FORMAT.format(this.delay.getValue())};
   }
}
