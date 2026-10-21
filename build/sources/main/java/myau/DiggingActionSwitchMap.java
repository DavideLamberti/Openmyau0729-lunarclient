package myau;

import net.minecraft.network.play.client.C07PacketPlayerDigging.Action;

class DiggingActionSwitchMap {
   static final int[] J = new int[Action.values().length];

   static {
      try {
         J[Action.START_DESTROY_BLOCK.ordinal()] = 1;
      } catch (NoSuchFieldError var2) {
      }

      try {
         J[Action.STOP_DESTROY_BLOCK.ordinal()] = 2;
      } catch (NoSuchFieldError var1) {
      }
   }
}
