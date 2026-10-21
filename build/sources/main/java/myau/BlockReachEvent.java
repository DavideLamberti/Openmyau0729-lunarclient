package myau;

public class BlockReachEvent extends Event {
   private double reachDistance;

   public BlockReachEvent(double reachDistance) {
      this.reachDistance = reachDistance;
   }

   public double getReachDistance() {
      return this.reachDistance;
   }

   public void setReachDistance(double reachDistance) {
      this.reachDistance = reachDistance;
   }
}
