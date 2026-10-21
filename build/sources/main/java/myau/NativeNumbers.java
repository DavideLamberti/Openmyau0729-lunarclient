package myau;

final class NativeNumbers {
   private NativeNumbers() {
   }

   static long truncateFloatToLong(float value) {
      return !Float.isNaN(value) && !(value >= 9.223372E18F) ? (long)value : Long.MIN_VALUE;
   }
}
