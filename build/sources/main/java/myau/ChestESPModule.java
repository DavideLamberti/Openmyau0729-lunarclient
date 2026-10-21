package myau;

import java.awt.Color;
import java.util.stream.Collectors;
import myau.mixin.AccessorMinecraft;
import myau.mixin.AccessorRenderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

public class ChestESPModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   public final ColorSetting color = new ColorSetting("color", 16755200);
   public final PercentSetting opacity = new PercentSetting("opacity", 100);
   public final BooleanSetting tracers = new BooleanSetting("tracers", false);

   public ChestESPModule() {
      super("ChestESP", false, 0);
   }

   @EventHandler
   public void onRenderWorld(RenderWorldEvent event) {
      if (this.isEnabled()) {
         RenderUtils.beginOverlay();

         for (TileEntity chest : minecraft.theWorld.loadedTileEntityList.stream().filter(ChestESPModule::isChest).collect(Collectors.toList())) {
            AxisAlignedBB bounds = new AxisAlignedBB(
                  (double)chest.getPos().getX() + 0.0625,
                  (double)chest.getPos().getY() + 0.0,
                  (double)chest.getPos().getZ() + 0.0625,
                  (double)chest.getPos().getX() + 0.9375,
                  (double)chest.getPos().getY() + 0.875,
                  (double)chest.getPos().getZ() + 0.9375
               )
               .offset(
                  -((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosX(),
                  -((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosY(),
                  -((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosZ()
               );
            Color outline = new Color(this.color.getValue());
            RenderUtils.outlineBounds(
               bounds, outline.getRed(), outline.getGreen(), outline.getBlue(), (int)((float)this.opacity.getValue().intValue() / 100.0F * 255.0F), 1.5F
            );
            if (this.tracers.getValue()) {
               this.drawTracer(chest, outline);
            }
         }

         RenderUtils.endOverlay();
      }
   }

   private static boolean isChest(TileEntity tile) {
      return tile instanceof TileEntityChest || tile instanceof TileEntityEnderChest;
   }

   private void drawTracer(TileEntity chest, Color outline) {
      Vec3 start;
      if (minecraft.gameSettings.thirdPersonView == 0) {
         start = new Vec3(0.0, 0.0, 1.0)
            .rotatePitch(
               (float)(
                  -Math.toRadians(
                     (double)MathUtils.lerpFloat(
                        minecraft.getRenderViewEntity().prevRotationPitch,
                        minecraft.getRenderViewEntity().rotationPitch,
                        ((AccessorMinecraft)minecraft).getTimer().renderPartialTicks
                     )
                  )
               )
            )
            .rotateYaw(
               (float)(
                  -Math.toRadians(
                     (double)MathUtils.lerpFloat(
                        minecraft.getRenderViewEntity().prevRotationYaw,
                        minecraft.getRenderViewEntity().rotationYaw,
                        ((AccessorMinecraft)minecraft).getTimer().renderPartialTicks
                     )
                  )
               )
            );
      } else {
         start = new Vec3(0.0, 0.0, 0.0)
            .rotatePitch(
               (float)(
                  -Math.toRadians(
                     (double)MathUtils.lerpFloat(
                        minecraft.thePlayer.prevCameraPitch, minecraft.thePlayer.cameraPitch, ((AccessorMinecraft)minecraft).getTimer().renderPartialTicks
                     )
                  )
               )
            )
            .rotateYaw(
               (float)(
                  -Math.toRadians(
                     (double)MathUtils.lerpFloat(
                        minecraft.thePlayer.prevCameraYaw, minecraft.thePlayer.cameraYaw, ((AccessorMinecraft)minecraft).getTimer().renderPartialTicks
                     )
                  )
               )
            );
      }

      start = new Vec3(start.xCoord, start.yCoord + (double)minecraft.getRenderViewEntity().getEyeHeight(), start.zCoord);
      float tracerOpacity = (float)((TracersModule)MyauClient.moduleManager.modules.get(TracersModule.class)).opacity.getValue().intValue() / 100.0F;
      RenderUtils.drawTracer(
         start,
         (double)chest.getPos().getX() + 0.5,
         (double)chest.getPos().getY() + 0.5,
         (double)chest.getPos().getZ() + 0.5,
         (float)outline.getRed() / 255.0F,
         (float)outline.getGreen() / 255.0F,
         (float)outline.getBlue() / 255.0F,
         tracerOpacity,
         1.5F
      );
   }
}
