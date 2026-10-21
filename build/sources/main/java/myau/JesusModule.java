package myau;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class JesusModule extends Module {
   private static final DecimalFormat speedFormat = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
   public final FloatSetting speed = new FloatSetting("speed", 2.5F, 0.0F, 3.0F);
   public final BooleanSetting noPush = new BooleanSetting("no-push", true);
   public final BooleanSetting groundOnly = new BooleanSetting("ground-only", true);

   public JesusModule() {
      super("Jesus", false, 0);
   }

   @Override
   public String[] getHudInfo() {
      return new String[]{speedFormat.format(this.speed.getValue())};
   }
}
