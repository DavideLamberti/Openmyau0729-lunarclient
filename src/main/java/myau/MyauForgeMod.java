package myau;

import java.io.File;
import java.nio.file.Files;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(
   modid = "myau",
   name = "Myau",
   version = "1.0.0",
   clientSideOnly = true,
   acceptedMinecraftVersions = "[1.8.9]"
)
public final class MyauForgeMod {
   public static final String MOD_ID = "myau";

   @net.minecraftforge.fml.common.Mod.EventHandler
   public void initialize(FMLInitializationEvent event) {
      MyauClient.initialize();
      File profile = new File("config/Myau/predac1.json");
      if (!profile.isFile()) {
         // First launch: use hardcoded default values from each Setting/Module constructor
         // instead of loading the bundled preset, so the user starts with a clean slate.
         try {
            Files.createDirectories(profile.toPath().getParent());
         } catch (Exception var16) {
            throw new IllegalStateException("Unable to create Myau config directory: " + profile.getParentFile(), var16);
         }
         ConfigProfile.resetToDefaults();
         // Persist the defaults as the initial predac1 profile so future loads work normally.
         if (!new ConfigProfile("predac1").save()) {
            throw new IllegalStateException("Unable to save initial Myau profile: " + profile);
         }
      } else {
         if (!new ConfigProfile("predac1").load()) {
            throw new IllegalStateException("Unable to load Myau profile: " + profile);
         }
      }
   }
}
