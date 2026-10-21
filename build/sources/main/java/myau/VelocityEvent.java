package myau;

public class VelocityEvent extends CancellableEvent {
   private double velocityX;
   private double velocityY;
   private double velocityZ;

   public VelocityEvent(double velocityX, double velocityY, double velocityZ) {
      this.velocityX = velocityX;
      this.velocityY = velocityY;
      this.velocityZ = velocityZ;
   }

   public double getVelocityX() {
      return this.velocityX;
   }

   public void setVelocityX(double velocityX) {
      this.velocityX = velocityX;
      this.setCancelled(true);
   }

   public double getVelocityY() {
      return this.velocityY;
   }

   public void setVelocityY(double velocityY) {
      this.velocityY = velocityY;
      this.setCancelled(true);
   }

   public double getVelocityZ() {
      return this.velocityZ;
   }

   public void setVelocityZ(double velocityZ) {
      this.velocityZ = velocityZ;
      this.setCancelled(true);
   }
}
