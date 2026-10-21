package myau;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Random;

public class ReachModule extends Module {
   private static final DecimalFormat rangeFormat = new DecimalFormat("0.0#", new DecimalFormatSymbols(Locale.US));
   private final Random random = new Random();
   private boolean chancePassed = true;
   public final FloatSetting range = new FloatSetting("range", 3.1F, 3.0F, 6.0F);
   public final PercentSetting chance = new PercentSetting("chance", 100);

   public ReachModule() {
      super("Reach", false, 0);
   }

   private boolean shouldApplyReach() {
      return this.isEnabled() && this.chancePassed;
   }

   @EventHandler
   public void onEntityReach(EntityReachEvent event) {
      if (this.shouldApplyReach()) {
         event.setReachDistance(this.range.getValue().doubleValue());
      }
   }

   @EventHandler
   public void onBlockReach(BlockReachEvent event) {
      if (this.shouldApplyReach()) {
         event.setReachDistance(Math.max(event.getReachDistance(), this.range.getValue().doubleValue() + 0.5));
      }
   }

   @EventHandler
   public void onPreTick(PreTickEvent event) {
      if (this.isEnabled()) {
         double difference = this.random.nextDouble() - (double)this.chance.getValue().intValue() / 100.0;
         this.chancePassed = difference <= 0.0;
      }
   }

   @Override
   public String[] getHudInfo() {
      return new String[]{rangeFormat.format(this.range.getValue())};
   }
}
