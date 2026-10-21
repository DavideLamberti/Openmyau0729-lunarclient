package myau;

public class AntiDebuffModule extends Module {
   public final BooleanSetting blindness = new BooleanSetting("blindness", true);
   public final BooleanSetting nausea = new BooleanSetting("nausea", true);

   public AntiDebuffModule() {
      super("AntiDebuff", false, 0);
   }
}
