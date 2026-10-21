package myau;

public class RenderWorldEvent extends Event {
   private final float partialTicks;

   public RenderWorldEvent(float partialTicks) {
      this.partialTicks = partialTicks;
   }

   public float getPartialTicks() {
      return this.partialTicks;
   }
}
