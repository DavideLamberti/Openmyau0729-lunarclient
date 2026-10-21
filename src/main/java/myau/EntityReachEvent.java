package myau;

public class EntityReachEvent extends Event {
   private double reachDistance;

   public EntityReachEvent(double reachDistance) {
      this.reachDistance = reachDistance;
   }

   public double getReachDistance() {
      return this.reachDistance;
   }

   public void setReachDistance(double reachDistance) {
      this.reachDistance = reachDistance;
   }
}
