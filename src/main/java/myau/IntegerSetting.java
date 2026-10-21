package myau;

import com.google.gson.JsonObject;
import java.util.function.BooleanSupplier;

public class IntegerSetting extends Setting<Integer> {
   private final Integer min;
   private final Integer max;

   public IntegerSetting(String name, Integer defaultValue, Integer min, Integer max) {
      this(name, defaultValue, min, max, null);
   }

   public IntegerSetting(String name, Integer defaultValue, Integer min, Integer max, BooleanSupplier visibilityCondition) {
      super(name, defaultValue, value -> value >= min && value <= max, visibilityCondition);
      this.min = min;
      this.max = max;
   }

   @Override
   public String getUsage() {
      return String.format("%d-%d", this.min, this.max);
   }

   @Override
   public String getDisplayValue() {
      return String.format("&e%s", this.getValue());
   }

   @Override
   public boolean parseValue(String input) {
      return this.setValue(Integer.valueOf(Integer.parseInt(input)));
   }

   @Override
   public boolean readJson(JsonObject json) {
      return this.setValue(Integer.valueOf(json.get(this.getName()).getAsNumber().intValue()));
   }

   @Override
   public void writeJson(JsonObject json) {
      json.addProperty(this.getName(), this.getValue());
   }

   public Integer getMin() {
      return this.min;
   }

   public Integer getMax() {
      return this.max;
   }

   public Integer getMinimum() {
      return this.min;
   }

   public Integer getMaximum() {
      return this.max;
   }
}
