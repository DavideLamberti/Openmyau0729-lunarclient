package myau;

public class SafeWalkEvent extends Event {
   private boolean safeWalk;

   public SafeWalkEvent(boolean safeWalk) {
      this.safeWalk = safeWalk;
   }

   public boolean isSafeWalk() {
      return this.safeWalk;
   }

   public void setSafeWalk(boolean safeWalk) {
      this.safeWalk = safeWalk;
   }
}
