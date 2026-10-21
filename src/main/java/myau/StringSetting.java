package myau;

import com.google.gson.JsonObject;
import java.util.function.BooleanSupplier;

public class StringSetting extends Setting<String> {
   public StringSetting(String name, String defaultValue) {
      this(name, defaultValue, null);
   }

   public StringSetting(String name, String defaultValue, BooleanSupplier visibilityCondition) {
      super(name, defaultValue, visibilityCondition);
   }

   @Override
   public String getUsage() {
      return "text";
   }

   @Override
   public String getDisplayValue() {
      return String.format("&f%s", this.getValue());
   }

   @Override
   public boolean parseValue(String input) {
      return this.setValue(input);
   }

   @Override
   public boolean readJson(JsonObject json) {
      return this.parseValue(json.get(this.getName()).getAsString());
   }

   @Override
   public void writeJson(JsonObject json) {
      json.addProperty(this.getName(), this.getValue());
   }
}
