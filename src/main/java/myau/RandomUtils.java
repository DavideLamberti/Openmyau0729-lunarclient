package myau;

import java.util.Random;

public final class RandomUtils {
   private static final Random RANDOM = new Random();

   private RandomUtils() {
   }

   public static boolean randomBoolean() {
      return RANDOM.nextBoolean();
   }

   public static long randomLong(long minInclusive, long maxInclusive) {
      return (long)randomDouble((double)minInclusive, (double)(maxInclusive + 1L));
   }

   public static float randomFloat(float min, float max) {
      return RANDOM.nextFloat() * (max - min) + min;
   }

   public static double randomDouble(double min, double max) {
      return RANDOM.nextDouble() * (max - min) + min;
   }
}
