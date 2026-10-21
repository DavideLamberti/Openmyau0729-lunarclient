package myau;

final class StrafeOffset {
   private final double offsetX;
   private final double offsetZ;

   StrafeOffset(double offsetX, double offsetZ) {
      this.offsetX = offsetX;
      this.offsetZ = offsetZ;
   }

   double x() {
      return this.offsetX;
   }

   double z() {
      return this.offsetZ;
   }
}
