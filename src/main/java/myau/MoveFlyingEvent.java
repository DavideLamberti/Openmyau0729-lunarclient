package myau;

public class MoveFlyingEvent extends Event {
   private float strafe;
   private float forward;
   private float friction;

   public MoveFlyingEvent(float strafe, float forward, float friction) {
      this.strafe = strafe;
      this.forward = forward;
      this.friction = friction;
   }

   public float getStrafe() {
      return this.strafe;
   }

   public float getForward() {
      return this.forward;
   }

   public float getFriction() {
      return this.friction;
   }

   public void setStrafe(float strafe) {
      this.strafe = strafe;
   }

   public void setForward(float forward) {
      this.forward = forward;
   }

   public void setFriction(float friction) {
      this.friction = friction;
   }
}
