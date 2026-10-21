package myau;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

public class AutoToolModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private int selectedSlot = -1;
   private int previousSlot = -1;
   private int miningTicks;
   public final IntegerSetting delay = new IntegerSetting("delay", 0, 0, 5);
   public final BooleanSetting switchBack = new BooleanSetting("switch-back", true);
   public final BooleanSetting sneakOnly = new BooleanSetting("sneak-only", true);

   public AutoToolModule() {
      super("AutoTool", false, 0);
   }

   @EventHandler
   public void onPreTick(PreTickEvent event) {
      boolean enabled = this.isEnabled();
      if (enabled) {
         if (minecraft.objectMouseOver != null
            && minecraft.objectMouseOver.typeOfHit == MovingObjectType.BLOCK
            && minecraft.gameSettings.keyBindAttack.isKeyDown()
            && !minecraft.thePlayer.isUsingItem()) {
            if (this.miningTicks >= this.delay.getValue()
               && (!this.sneakOnly.getValue() || InputUtils.isKeyDown(minecraft.gameSettings.keyBindSneak.getKeyCode()))) {
               int bestSlot = ItemUtils.findBestToolForBlock(
                  minecraft.thePlayer.inventory.currentItem,
                  minecraft.theWorld.getBlockState(minecraft.objectMouseOver.getBlockPos()).getBlock()
               );
               if (minecraft.thePlayer.inventory.currentItem != bestSlot) {
                  this.previousSlot = minecraft.thePlayer.inventory.currentItem;
                  minecraft.thePlayer.inventory.currentItem = this.selectedSlot = bestSlot;
               }
            }

            this.miningTicks++;
         } else {
            this.selectedSlot = -1;
            this.previousSlot = -1;
            this.miningTicks = 0;
         }
      }
   }

   @Override
   public void onDisable() {
      this.selectedSlot = -1;
      this.previousSlot = -1;
      this.miningTicks = 0;
   }
}
