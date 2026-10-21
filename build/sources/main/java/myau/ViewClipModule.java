package myau;

import net.minecraft.client.Minecraft;

public class ViewClipModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();

   public ViewClipModule() {
      super("ViewClip", false, 0);
   }

   @Override
   public void onEnable() {
      this.reloadRenderers();
   }

   @Override
   public void onDisable() {
      this.reloadRenderers();
   }

   private void reloadRenderers() {
      if (minecraft.theWorld != null) {
         minecraft.renderGlobal.loadRenderers();
      }
   }
}
