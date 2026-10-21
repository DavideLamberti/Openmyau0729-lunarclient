package myau;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;

public class SoundUtils {
   private static final Minecraft minecraft = Minecraft.getMinecraft();

   public static void play(String soundName) {
      SoundHandler soundHandler = minecraft.getSoundHandler();
      if (soundHandler != null) {
         PositionedSoundRecord sound = PositionedSoundRecord.create(new ResourceLocation(soundName));
         soundHandler.playSound(sound);
      }
   }
}
