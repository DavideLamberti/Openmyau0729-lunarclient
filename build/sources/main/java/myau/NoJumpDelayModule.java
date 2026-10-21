package myau;

import myau.mixin.AccessorEntityLivingBase;
import net.minecraft.client.Minecraft;

public class NoJumpDelayModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   public final IntegerSetting delay = new IntegerSetting("delay", 3, 0, 8);

   public NoJumpDelayModule() {
      super("NoJumpDelay", false, 0);
   }

   @EventHandler(
      priority = 100
   )
   public void onPreTick(PreTickEvent event) {
      if (this.isEnabled()) {
         AccessorEntityLivingBase player = (AccessorEntityLivingBase)minecraft.thePlayer;
         player.setJumpTicks(Math.min(player.getJumpTicks(), this.delay.getValue() + 1));
      }
   }

   @Override
   public String[] getHudInfo() {
      return new String[]{this.delay.getValue().toString()};
   }
}
