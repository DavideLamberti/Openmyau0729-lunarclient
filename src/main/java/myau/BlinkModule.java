package myau;

public class BlinkModule extends Module {
   public final ModeSetting mode = new ModeSetting("mode", 0, new String[]{"DEFAULT", "PULSE"});
   public final IntegerSetting ticks = new IntegerSetting("ticks", 200, 0, 12000);

   public BlinkModule() {
      super("Blink", false, 0);
   }

   @EventHandler(
      priority = -100
   )
   public void onPostTick(PostTickEvent event) {
      boolean enabled = this.isEnabled();
      if (enabled) {
         if (!MyauClient.outgoingPacketBuffer.owner().equals(OutgoingBufferOwner.BLINK)) {
            this.setEnabled(false);
         } else if (this.ticks.getValue() > 0) {
            if (MyauClient.outgoingPacketBuffer.movementPacketCount() - (long)this.ticks.getValue().intValue() > 0L) {
               switch (this.mode.getValue()) {
                  case 0:
                     this.setEnabled(false);
                     break;
                  case 1:
                     MyauClient.outgoingPacketBuffer.setEnabled(false, OutgoingBufferOwner.BLINK);
                     MyauClient.outgoingPacketBuffer.setEnabled(true, OutgoingBufferOwner.BLINK);
               }
            }
         }
      }
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      this.setEnabled(false);
   }

   @Override
   public void onEnable() {
      MyauClient.outgoingPacketBuffer.setEnabled(false, MyauClient.outgoingPacketBuffer.owner());
      MyauClient.outgoingPacketBuffer.setEnabled(true, OutgoingBufferOwner.BLINK);
   }

   @Override
   public void onDisable() {
      MyauClient.outgoingPacketBuffer.setEnabled(false, OutgoingBufferOwner.BLINK);
   }
}
