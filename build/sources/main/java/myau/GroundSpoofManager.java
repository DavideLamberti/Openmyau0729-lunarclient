package myau;

import java.util.LinkedHashMap;
import net.minecraft.client.Minecraft;

public class GroundSpoofManager {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private final LinkedHashMap<GroundSpoofOwner, Boolean> requestsByOwner = new LinkedHashMap<>();
   private boolean offsetActive;

   public boolean isOffsetActive() {
      return this.offsetActive;
   }

   public boolean isDescendingOnGround() {
      return minecraft.thePlayer.onGround
         && minecraft.thePlayer.posY < minecraft.thePlayer.lastTickPosY
         && minecraft.thePlayer.motionY < 0.0;
   }

   public boolean hasActiveRequest() {
      return this.requestsByOwner.containsValue(true);
   }

   public void setRequest(boolean enabled, GroundSpoofOwner owner) {
      this.requestsByOwner.put(owner, Boolean.valueOf(enabled));
   }

   @EventHandler
   public void onPreWalkingUpdate(PreWalkingUpdateEvent event) {
      if ((this.hasActiveRequest() || this.offsetActive) && this.isDescendingOnGround()) {
         minecraft.thePlayer
            .setPosition(minecraft.thePlayer.posX, minecraft.thePlayer.posY + 0.001, minecraft.thePlayer.posZ);
         this.offsetActive = true;
      } else {
         this.offsetActive = false;
      }
   }
}
