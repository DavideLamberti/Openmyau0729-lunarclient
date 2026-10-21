package myau;

import net.minecraft.client.Minecraft;

public class SpammerModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private final ElapsedTimer timer = new ElapsedTimer();
   private int nextSuffixCharacter = 19968;
   public final StringSetting text = new StringSetting("text", "meow");
   public final FloatSetting delay = new FloatSetting("delay", 3.5F, 0.0F, 3600.0F);
   public final IntegerSetting random = new IntegerSetting("random", 0, 0, 10);

   public SpammerModule() {
      super("Spammer", false, 0);
   }

   @EventHandler
   public void onRenderOverlay(RenderOverlayEvent event) {
      if (this.isEnabled()) {
         if (this.timer.hasElapsed((long)(this.delay.getValue() * 1000.0F))) {
            this.timer.reset();
            String message = this.text.getValue();
            int suffixLength = this.random.getValue();
            if (suffixLength <= 0) {
               minecraft.thePlayer.sendChatMessage(message);
            } else {
               message = String.format("%s ", message);

               for (int index = 0; index < suffixLength; index++) {
                  message = String.format("%s%s", message, (char)this.nextSuffixCharacter);
                  this.nextSuffixCharacter++;
               }

               minecraft.thePlayer.sendChatMessage(message);
            }
         }
      }
   }
}
