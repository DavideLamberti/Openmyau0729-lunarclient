package myau;

import java.util.HashMap;

final class EnchantmentLabels extends HashMap<Integer, EnchantmentLabel> {
   EnchantmentLabels() {
      this.put(Integer.valueOf(0), new EnchantmentLabel("Pr", 4));
      this.put(Integer.valueOf(1), new EnchantmentLabel("Fp", 4));
      this.put(Integer.valueOf(2), new EnchantmentLabel("Ff", 4));
      this.put(Integer.valueOf(3), new EnchantmentLabel("Bp", 4));
      this.put(Integer.valueOf(4), new EnchantmentLabel("Pp", 4));
      this.put(Integer.valueOf(5), new EnchantmentLabel("Re", 3));
      this.put(Integer.valueOf(6), new EnchantmentLabel("Aq", 1));
      this.put(Integer.valueOf(7), new EnchantmentLabel("Th", 3));
      this.put(Integer.valueOf(8), new EnchantmentLabel("Ds", 3));
      this.put(Integer.valueOf(16), new EnchantmentLabel("Sh", 5));
      this.put(Integer.valueOf(17), new EnchantmentLabel("Sm", 5));
      this.put(Integer.valueOf(18), new EnchantmentLabel("BoA", 5));
      this.put(Integer.valueOf(19), new EnchantmentLabel("Kb", 2));
      this.put(Integer.valueOf(20), new EnchantmentLabel("Fa", 2));
      this.put(Integer.valueOf(21), new EnchantmentLabel("Lo", 3));
      this.put(Integer.valueOf(32), new EnchantmentLabel("Ef", 5));
      this.put(Integer.valueOf(33), new EnchantmentLabel("St", 1));
      this.put(Integer.valueOf(34), new EnchantmentLabel("Ub", 3));
      this.put(Integer.valueOf(35), new EnchantmentLabel("Fo", 3));
      this.put(Integer.valueOf(48), new EnchantmentLabel("Po", 5));
      this.put(Integer.valueOf(49), new EnchantmentLabel("Pu", 2));
      this.put(Integer.valueOf(50), new EnchantmentLabel("Fl", 1));
      this.put(Integer.valueOf(51), new EnchantmentLabel("Inf", 1));
      this.put(Integer.valueOf(61), new EnchantmentLabel("LoS", 3));
      this.put(Integer.valueOf(62), new EnchantmentLabel("Lu", 3));
   }
}
