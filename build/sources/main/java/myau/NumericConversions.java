package myau;

final class NumericConversions {
   private NumericConversions() {
   }

   static long truncateFloatToLong(float input) {
      return !Float.isNaN(input) && !(input >= 9.223372E18F) ? (long)input : Long.MIN_VALUE;
   }
}
