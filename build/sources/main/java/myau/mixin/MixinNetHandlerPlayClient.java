package myau.mixin;

import myau.MyauClient;
import myau.NoRotateModule;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@SideOnly(Side.CLIENT)
@Mixin({NetHandlerPlayClient.class})
public abstract class MixinNetHandlerPlayClient {
   @Inject(
      method = {"handlePlayerPosLook"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/player/EntityPlayer;setPositionAndRotation(DDDFF)V"
      )},
      locals = LocalCapture.CAPTURE_FAILSOFT,
      cancellable = true
   )
   private void beforeApplyServerPositionRotation(
      S08PacketPlayerPosLook packet, CallbackInfo callbackInfo, EntityPlayer player, double x, double y, double z, float yaw, float pitch
   ) {
      NoRotateModule noRotate = (NoRotateModule)MyauClient.moduleManager.modules.get(NoRotateModule.class);
      if (noRotate.isEnabled() && noRotate.handleServerPositionAndRotation(player, x, y, z, yaw, pitch)) {
         callbackInfo.cancel();
      }
   }
}
