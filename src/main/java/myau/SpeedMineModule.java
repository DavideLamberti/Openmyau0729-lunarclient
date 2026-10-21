package myau;

import myau.mixin.AccessorPlayerControllerMP;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C07PacketPlayerDigging;

public class SpeedMineModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private boolean startDiggingPending;
   private boolean stopDiggingPending;
   private int chanceAccumulator = 100;
   public final PercentSetting speed = new PercentSetting("speed", 15);
   public final IntegerSetting delay = new IntegerSetting("delay", 0, 0, 5);
   public final PercentSetting chance = new PercentSetting("chance", 50);

   public SpeedMineModule() {
      super("SpeedMine", false, 0);
   }

   @EventHandler
   public void onPostTick(PostTickEvent event) {
      if (this.isEnabled()) {
         if (this.startDiggingPending) {
            this.startDiggingPending = false;
            if (((AccessorPlayerControllerMP)minecraft.playerController).getIsHittingBlock() && this.chanceAccumulator >= 100) {
               float damage = ((AccessorPlayerControllerMP)minecraft.playerController).getCurBlockDamageMP();
               float bonus = 0.3F * (this.speed.getValue().floatValue() / 100.0F);
               ((AccessorPlayerControllerMP)minecraft.playerController).setCurBlockDamageMP(damage + bonus);
            }
         }

         if (this.stopDiggingPending) {
            this.stopDiggingPending = false;
            if (((AccessorPlayerControllerMP)minecraft.playerController).getBlockHitDelay() == 5) {
               this.chanceAccumulator = this.chanceAccumulator % 100 + this.chance.getValue();
               if (this.chanceAccumulator >= 100) {
                  ((AccessorPlayerControllerMP)minecraft.playerController).setBlockHitDelay(this.delay.getValue());
               }
            }
         }
      }
   }

   @EventHandler(
      priority = -100
   )
   public void onPacketSend(PacketSendEvent event) {
      if (this.isEnabled()) {
         if (!event.isCancelled() && event.getPacket() instanceof C07PacketPlayerDigging) {
            C07PacketPlayerDigging packet = (C07PacketPlayerDigging)event.getPacket();
            switch (packet.getStatus()) {
               case START_DESTROY_BLOCK:
                  this.startDiggingPending = true;
                  break;
               case STOP_DESTROY_BLOCK:
                  this.stopDiggingPending = true;
            }
         }
      }
   }

   @Override
   public void onDisable() {
      this.startDiggingPending = false;
      this.stopDiggingPending = false;
   }

   @Override
   public String[] getHudInfo() {
      return new String[]{String.format("%d%%", this.speed.getValue()), String.format("%s", this.delay.getValue())};
   }
}
