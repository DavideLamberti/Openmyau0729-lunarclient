package myau;

public abstract class Module {
   protected final String name;
   protected final boolean defaultEnabled;
   protected final int defaultKeyCode;
   protected final boolean defaultHidden;
   protected boolean enabled;
   protected int keyCode;
   protected boolean hidden;
   protected MutableValue<Boolean> previousKeyState;

   public Module(String name, boolean defaultEnabled, int defaultKeyCode) {
      this(name, defaultEnabled, defaultKeyCode, false);
   }

   public Module(String name, boolean defaultEnabled, int defaultKeyCode, boolean defaultHidden) {
      this.name = name;
      this.defaultEnabled = defaultEnabled;
      this.enabled = false;
      this.keyCode = this.defaultKeyCode = defaultKeyCode;
      this.hidden = this.defaultHidden = defaultHidden;
      this.previousKeyState = new MutableValue<>(false);
   }

   public String getName() {
      return this.name;
   }

   public String getDisplayText() {
      String keyPrefix = this.keyCode == 0 ? "" : String.format("&l[%s] &r", InputUtils.getKeyName(this.keyCode));
      String stateText = this.enabled ? "&a&lON" : "&c&lOFF";
      return String.format("%s%s &r(%s&r)", keyPrefix, this.name, stateText);
   }

   public String[] getHudInfo() {
      return new String[0];
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      if (this.enabled != enabled) {
         this.enabled = enabled;
         if (enabled) {
            this.onEnable();
         } else {
            this.onDisable();
         }
      }
   }

   public void resetEnabled() {
      this.setEnabled(this.defaultEnabled);
   }

   public boolean toggle() {
      boolean requested = !this.enabled;
      this.setEnabled(requested);
      if (this.enabled == requested) {
         HUDModule hud = (HUDModule)MyauClient.moduleManager.modules.get(HUDModule.class);
         if (hud != null && hud.toggleSounds.getValue()) {
            SoundUtils.play("random.click");
         }
         return true;
      } else {
         return false;
      }
   }

   public int getKey() {
      return this.getKeyCode();
   }

   public void setKey(int keyCode) {
      this.setKeyCode(keyCode);
   }

   public int getKeyCode() {
      return this.keyCode;
   }

   public void setKeyCode(int keyCode) {
      this.keyCode = keyCode;
   }

   public void resetKeyCode() {
      this.setKeyCode(this.defaultKeyCode);
   }

   public boolean isHidden() {
      return this.hidden;
   }

   public void setHidden(boolean hidden) {
      this.hidden = hidden;
   }

   public void resetHidden() {
      this.setHidden(this.defaultHidden);
   }

   public MutableValue<Boolean> getPreviousKeyState() {
      return this.previousKeyState;
   }

   public void setPreviousKeyState(MutableValue<Boolean> previousKeyState) {
      this.previousKeyState = previousKeyState;
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onSettingChanged(String settingName) {
   }
}
