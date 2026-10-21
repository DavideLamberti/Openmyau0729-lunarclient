package myau;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

public class RotationSmoother {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private float lastPartialTicks;
   private float yawDelta;
   private float pitchDelta;
   private int requestPriority;
   private boolean requestActive;

   public RotationSmoother() {
      this.resetRotationRequest();
   }

   private void applyRotationDelta(float partialTicks) {
      if (minecraft.thePlayer != null && !Float.isNaN(this.yawDelta) && !Float.isNaN(this.pitchDelta) && !Float.isNaN(this.lastPartialTicks)) {
         float partialDelta = partialTicks - this.lastPartialTicks;
         float yawStep = this.yawDelta * partialDelta;
         if (yawStep != 0.0F) {
            minecraft.thePlayer.prevRotationYaw = minecraft.thePlayer.rotationYaw;
            minecraft.thePlayer.rotationYaw += yawStep;
         }

         float pitchStep = this.pitchDelta * partialDelta;
         if (pitchStep != 0.0F) {
            minecraft.thePlayer.prevRotationPitch = minecraft.thePlayer.rotationPitch;
            minecraft.thePlayer.rotationPitch += pitchStep;
            minecraft.thePlayer.rotationPitch = MathHelper.clamp_float(minecraft.thePlayer.rotationPitch, -90.0F, 90.0F);
         }

         this.lastPartialTicks = partialTicks;
      }
   }

   private void resetRotationRequest() {
      this.lastPartialTicks = Float.NaN;
      this.yawDelta = Float.NaN;
      this.pitchDelta = Float.NaN;
      this.requestPriority = Integer.MIN_VALUE;
      this.requestActive = false;
   }

   public void requestRotation(float yaw, float pitch, int priority, boolean active) {
      if (this.requestPriority <= priority) {
         this.requestPriority = priority;
         this.yawDelta = MathHelper.wrapAngleTo180_float(yaw - minecraft.thePlayer.rotationYaw);
         this.pitchDelta = pitch - minecraft.thePlayer.rotationPitch;
         this.lastPartialTicks = 0.0F;
         this.requestActive = active;
      }
   }

   public boolean isRequestActive() {
      return this.requestActive;
   }

   @EventHandler(
      priority = 100
   )
   public void onPreTick(PreTickEvent event) {
      this.applyRotationDelta(1.0F);
      this.resetRotationRequest();
   }

   @EventHandler(
      priority = 100
   )
   public void onRenderWorld(RenderWorldEvent event) {
      this.applyRotationDelta(event.getPartialTicks());
   }
}
