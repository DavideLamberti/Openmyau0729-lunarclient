package myau;

import com.google.gson.JsonObject;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public abstract class Setting<T> {
   private final String name;
   private final T defaultValue;
   private final Predicate<T> validator;
   private final BooleanSupplier visibilityCondition;
   private T value;
   private Module module;

   protected Setting(String name, T defaultValue, BooleanSupplier visibilityCondition) {
      this(name, defaultValue, null, visibilityCondition);
   }

   protected Setting(String name, T defaultValue, Predicate<T> validator, BooleanSupplier visibilityCondition) {
      this.name = name;
      this.defaultValue = defaultValue;
      this.validator = validator;
      this.visibilityCondition = visibilityCondition;
      this.value = defaultValue;
   }

   public String getName() {
      return this.name;
   }

   public abstract String getUsage();

   public boolean isVisible() {
      return this.visibilityCondition == null || this.visibilityCondition.getAsBoolean();
   }

   public T getValue() {
      return this.value;
   }

   public abstract String getDisplayValue();

   public boolean setValue(T tValue) {
      if (this.validator != null && !this.validator.test(tValue)) {
         return false;
      } else {
         this.value = tValue;
         if (this.module != null) {
            this.module.onSettingChanged(this.name);
         }

         return true;
      }
   }

   public void reset() {
      this.setValue(this.defaultValue);
   }

   public void setModule(Module module) {
      this.module = module;
   }

   public abstract boolean parseValue(String var1);

   public abstract boolean readJson(JsonObject var1);

   public abstract void writeJson(JsonObject var1);
}
