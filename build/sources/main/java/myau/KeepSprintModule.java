package myau;

import net.minecraft.client.Minecraft;

public class KeepSprintModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   public final PercentSetting slowdown = new PercentSetting("slowdown", 0);
   public final BooleanSetting groundOnly = new BooleanSetting("ground-only", false);
   public final BooleanSetting reachOnly = new BooleanSetting("reach-only", false);

   public KeepSprintModule() {
      super("KeepSprint", false, 0);
   }

   public boolean shouldPreserveSprint() {
      boolean groundOnly = this.groundOnly.getValue();
      if (groundOnly && !minecraft.thePlayer.onGround) {
         return false;
      } else {
         return !this.reachOnly.getValue() ? true : minecraft.objectMouseOver.hitVec.distanceTo(minecraft.getRenderViewEntity().getPositionEyes(1.0F)) > 3.0;
      }
   }
}
