package myau;

import com.google.gson.JsonObject;
import java.util.function.BooleanSupplier;

public class ModeSetting extends Setting<Integer> {
   private final String[] modes;

   public ModeSetting(String name, Integer defaultValue, String[] modes) {
      this(name, defaultValue, modes, null);
   }

   public ModeSetting(String name, Integer defaultValue, String[] modes, BooleanSupplier visibilityCondition) {
      super(name, defaultValue, visibilityCondition);
      this.modes = modes;
   }

   @Override
   public String getUsage() {
      return String.join(", ", this.modes);
   }

   public String getModeName() {
      int index = this.getValue();
      return index >= 0 && index < this.modes.length ? this.modes[index] : "";
   }

   public String[] getModes() {
      return (String[])this.modes.clone();
   }

   @Override
   public String getDisplayValue() {
      String mode = this.getModeName();
      return mode.isEmpty() ? "&4?" : String.format("&9%s", mode);
   }

   @Override
   public boolean parseValue(String input) {
      String normalized = input.replace("_", "");

      for (int index = 0; index < this.modes.length; index++) {
         if (normalized.equalsIgnoreCase(this.modes[index].replace("_", ""))) {
            return this.setValue(Integer.valueOf(index));
         }
      }

      return false;
   }

   @Override
   public boolean readJson(JsonObject json) {
      return this.parseValue(json.get(this.getName()).getAsString());
   }

   @Override
   public void writeJson(JsonObject json) {
      json.addProperty(this.getName(), this.getModeName());
   }

   public void nextMode() {
      int current = this.getValue();
      int next = current + 1;
      if (next >= this.modes.length) {
         next = 0;
      }
      this.setValue(next);
   }

   public void previousMode() {
      int current = this.getValue();
      int prev = current - 1;
      if (prev < 0) {
         prev = this.modes.length - 1;
      }
      this.setValue(prev);
   }
}
