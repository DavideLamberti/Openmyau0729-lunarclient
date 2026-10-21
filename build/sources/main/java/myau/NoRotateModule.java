package myau;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.MathHelper;

public class NoRotateModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private boolean skipNextCorrection;

   public NoRotateModule() {
      super("NoRotate", false, 0);
   }

   public boolean handleServerPositionAndRotation(EntityPlayer player, double x, double y, double z, float yaw, float pitch) {
      if (this.skipNextCorrection) {
         this.skipNextCorrection = false;
         return false;
      } else if (minecraft.thePlayer.rotationYaw == -180.0F && minecraft.thePlayer.rotationPitch == 0.0F) {
         return false;
      } else {
         float sensitivity = 0.2F + minecraft.gameSettings.mouseSensitivity * 0.6F;
         float mouseStep = sensitivity * sensitivity * sensitivity * 8.0F * 0.15F;
         float yawJitter = RandomUtils.randomBoolean() ? mouseStep : -mouseStep;
         float localYaw = minecraft.thePlayer.rotationYaw + yawJitter;
         float serverYaw = yaw % 360.0F;
         float restoredYaw = RotationUtils.snapToMouseSensitivity(localYaw, serverYaw);
         float pitchJitter = RandomUtils.randomBoolean() ? mouseStep : -mouseStep;
         float localPitch = minecraft.thePlayer.rotationPitch + pitchJitter;
         float serverPitch = pitch % 360.0F;
         float restoredPitch = MathHelper.clamp_float(RotationUtils.snapToMouseSensitivity(localPitch, serverPitch), -90.0F, 90.0F);
         player.setPositionAndRotation(x, y, z, yaw, pitch);
         PacketUtils.send(
            new C06PacketPlayerPosLook(
               minecraft.thePlayer.posX,
               minecraft.thePlayer.getEntityBoundingBox().minY,
               minecraft.thePlayer.posZ,
               serverYaw,
               serverPitch,
               false
            )
         );
         minecraft.thePlayer.rotationYaw = restoredYaw;
         minecraft.thePlayer.rotationPitch = restoredPitch;
         return true;
      }
   }

   @EventHandler
   public void onPacketReceive(PacketReceiveEvent event) {
      if (this.isEnabled() && !event.isCancelled() && event.getPacket() instanceof S02PacketChat) {
         String message = ((S02PacketChat)event.getPacket()).getChatComponent().getFormattedText();
         if (message.contains("§e§lProtect your bed and destroy the enemy beds.") || message.contains("§eYou will respawn in §r§c1 §r§esecond!")) {
            this.skipNextCorrection = true;
         }
      }
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      this.skipNextCorrection = false;
   }

   @Override
   public void onDisable() {
      this.skipNextCorrection = false;
   }
}
