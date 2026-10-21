package myau;

import net.minecraft.client.Minecraft;

public class FlyModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private double verticalMotion;
   public final FloatSetting horizontalSpeed = new FloatSetting("horizontal-speed", 1.0F, 0.0F, 100.0F);
   public final FloatSetting verticalSpeed = new FloatSetting("vertical-speed", 1.0F, 0.0F, 100.0F);

   public FlyModule() {
      super("Fly", false, 0);
   }

   @EventHandler
   public void onMoveFlying(MoveFlyingEvent event) {
      boolean keepVerticalMotion = this.isEnabled();
      if (keepVerticalMotion) {
         keepVerticalMotion = minecraft.thePlayer.onGround;
         if (!keepVerticalMotion) {
            minecraft.thePlayer.motionY = this.verticalMotion;
         }

         MovementUtils.setSpeedAlongMotion(0.0);
         event.setFriction((float)MovementUtils.baseMovementSpeed() * this.horizontalSpeed.getValue());
      }
   }

   @EventHandler
   public void onRotationUpdate(RotationUpdateEvent event) {
      if (this.isEnabled()) {
         this.verticalMotion = 0.0;
         if (minecraft.currentScreen == null) {
            boolean jumpPressed = InputUtils.isKeyDown(minecraft.gameSettings.keyBindJump.getKeyCode());
            if (jumpPressed) {
               this.verticalMotion = this.verticalMotion + this.verticalSpeed.getValue().doubleValue() * 0.42F;
            }

            if (InputUtils.isKeyDown(minecraft.gameSettings.keyBindSneak.getKeyCode())) {
               this.verticalMotion = this.verticalMotion - this.verticalSpeed.getValue().doubleValue() * 0.42F;
            }

            InputUtils.setKeyState(minecraft.gameSettings.keyBindSneak.getKeyCode(), false);
         }
      }
   }

   @Override
   public void onDisable() {
      minecraft.thePlayer.motionY = 0.0;
      MovementUtils.setSpeedAlongMotion(0.0);
      InputUtils.updateKeyState(minecraft.gameSettings.keyBindSneak.getKeyCode());
   }
}
