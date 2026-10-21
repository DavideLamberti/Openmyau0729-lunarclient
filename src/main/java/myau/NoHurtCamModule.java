package myau;

public class NoHurtCamModule extends Module {
   public final PercentSetting multiplier = new PercentSetting("multiplier", 0);

   public NoHurtCamModule() {
      super("NoHurtCam", false, 0, true);
   }
}
