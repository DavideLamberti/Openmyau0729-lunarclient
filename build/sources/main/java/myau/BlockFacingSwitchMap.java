package myau;

import java.lang.invoke.MethodHandles;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import myau.source.SourceExceptions;
import net.minecraft.util.EnumFacing;

class BlockFacingSwitchMap {
   static final int[] r;

   static {
      try {
         long var7 = KeySchedule.a(-8032760974148074160L, -4142899658755579473L, MethodHandles.lookup().lookupClass()).a(183212371878696L) ^ 13482884720054L;
         Cipher var2;
         Cipher var10000 = var2 = Cipher.getInstance("DES/CBC/NoPadding");
         SecretKeyFactory var10002 = SecretKeyFactory.getInstance("DES");
         byte[] var10003 = new byte[]{(byte)((int)(var7 >>> 56)), 0, 0, 0, 0, 0, 0, 0};

         for (int var3 = 1; var3 < 8; var3++) {
            var10003[var3] = (byte)((int)(var7 << var3 * 8 >>> 56));
         }

         var10000.init(2, var10002.generateSecret(new DESKeySpec(var10003)), new IvParameterSpec(new byte[8]));
         long var4 = -4513344447874715490L;
         byte[] var6 = var2.doFinal(
            new byte[]{
               (byte)((int)(var4 >>> 56)),
               (byte)((int)(var4 >>> 48)),
               (byte)((int)(var4 >>> 40)),
               (byte)((int)(var4 >>> 32)),
               (byte)((int)(var4 >>> 24)),
               (byte)((int)(var4 >>> 16)),
               (byte)((int)(var4 >>> 8)),
               (byte)((int)var4)
            }
         );
         long var15 = ((long)var6[0] & 255L) << 56
            | ((long)var6[1] & 255L) << 48
            | ((long)var6[2] & 255L) << 40
            | ((long)var6[3] & 255L) << 32
            | ((long)var6[4] & 255L) << 24
            | ((long)var6[5] & 255L) << 16
            | ((long)var6[6] & 255L) << 8
            | (long)var6[7] & 255L;
         byte var10001 = -1;
         long var0 = var15;
         r = new int[EnumFacing.values().length];

         try {
            r[EnumFacing.DOWN.ordinal()] = 1;
         } catch (NoSuchFieldError var20) {
         }

         try {
            r[EnumFacing.UP.ordinal()] = 2;
         } catch (NoSuchFieldError var19) {
         }

         try {
            r[EnumFacing.NORTH.ordinal()] = 3;
         } catch (NoSuchFieldError var18) {
         }

         try {
            r[EnumFacing.EAST.ordinal()] = 4;
         } catch (NoSuchFieldError var17) {
         }

         try {
            r[EnumFacing.SOUTH.ordinal()] = 5;
         } catch (NoSuchFieldError var16) {
         }

         try {
            r[EnumFacing.WEST.ordinal()] = (int)var0;
         } catch (NoSuchFieldError var15x) {
         }
      } catch (Throwable var21) {
         throw SourceExceptions.propagate(var21);
      }
   }
}
