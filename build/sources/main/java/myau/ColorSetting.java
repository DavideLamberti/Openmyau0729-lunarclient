package myau;

import com.google.gson.JsonObject;
import java.util.function.BooleanSupplier;

public class ColorSetting extends Setting<Integer> {
   public ColorSetting(String name, Integer defaultValue) {
      this(name, defaultValue, null);
   }

   public ColorSetting(String name, Integer defaultValue, BooleanSupplier visibilityCondition) {
      super(name, defaultValue, value -> value <= 16777215, visibilityCondition);
   }

   @Override
   public String getUsage() {
      return "RGB";
   }

   @Override
   public String getDisplayValue() {
      String hex = String.format("%06X", this.getValue());
      return String.format("&c%s&a%s&9%s", hex.substring(0, 2), hex.substring(2, 4), hex.substring(4, 6));
   }

   @Override
   public boolean parseValue(String input) {
      return this.setValue(Integer.valueOf(Integer.parseInt(input.replace("#", ""), 16)));
   }

   @Override
   public boolean readJson(JsonObject json) {
      return this.parseValue(json.get(this.getName()).getAsString());
   }

   @Override
   public void writeJson(JsonObject json) {
      json.addProperty(this.getName(), String.format("%06X", this.getValue()));
   }
}
