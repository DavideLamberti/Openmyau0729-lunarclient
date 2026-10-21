package myau;

import com.google.gson.JsonObject;
import java.util.function.BooleanSupplier;

public class BooleanSetting extends Setting<Boolean> {
   public BooleanSetting(String name, Boolean defaultValue) {
      this(name, defaultValue, null);
   }

   public BooleanSetting(String name, Boolean defaultValue, BooleanSupplier visibilityCondition) {
      super(name, defaultValue, visibilityCondition);
   }

   @Override
   public String getUsage() {
      return "true/false";
   }

   @Override
   public String getDisplayValue() {
      return this.getValue() ? "&atrue" : "&cfalse";
   }

   @Override
   public boolean parseValue(String input) {
      if (input == null) {
         return this.setValue(Boolean.valueOf(!this.getValue()));
      } else if (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("on") || input.equalsIgnoreCase("1")) {
         return this.setValue(Boolean.valueOf(true));
      } else {
         return !input.equalsIgnoreCase("false") && !input.equalsIgnoreCase("off") && !input.equalsIgnoreCase("0")
            ? false
            : this.setValue(Boolean.valueOf(false));
      }
   }

   @Override
   public boolean readJson(JsonObject json) {
      return this.setValue(Boolean.valueOf(json.get(this.getName()).getAsBoolean()));
   }

   @Override
   public void writeJson(JsonObject json) {
      json.addProperty(this.getName(), this.getValue());
   }
}
