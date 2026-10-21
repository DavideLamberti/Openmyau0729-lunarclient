package myau;

public class ElapsedTimer {
   private long lastResetMillis;

   public void reset() {
      this.lastResetMillis = System.currentTimeMillis();
   }

   public long elapsedMillis() {
      return System.currentTimeMillis() - this.lastResetMillis;
   }

   public boolean hasElapsed(long delayMillis) {
      return this.elapsedMillis() >= delayMillis;
   }

   public void setLastResetMillis(long lastResetMillis) {
      this.lastResetMillis = lastResetMillis;
   }
}
