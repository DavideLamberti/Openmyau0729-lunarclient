package myau;

import java.awt.Color;
import java.util.ArrayList;
import myau.mixin.AccessorRenderManager;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemSnowball;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

public class TrajectoriesModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   public final PercentSetting opacity = new PercentSetting("opacity", 100);
   public final BooleanSetting bow = new BooleanSetting("bow", true);
   public final BooleanSetting projectiles = new BooleanSetting("projectiles", false);
   public final BooleanSetting pearls = new BooleanSetting("pearls", true);

   public TrajectoriesModule() {
      super("Trajectories", false, 0, true);
   }

   @EventHandler
   public void onRenderWorld(RenderWorldEvent event) {
      if (this.isEnabled()) {
         if (minecraft.thePlayer.getHeldItem() != null) {
            if (minecraft.gameSettings.thirdPersonView == 0) {
               Item item = minecraft.thePlayer.getHeldItem().getItem();
               RenderManager renderManager = minecraft.getRenderManager();
               boolean arrow = false;
               float speed = 1.5F;
               float drag = 0.99F;
               float gravity;
               float radius;
               if (item instanceof ItemBow && this.bow.getValue()) {
                  if (!minecraft.thePlayer.isUsingItem()) {
                     return;
                  }

                  arrow = true;
                  gravity = 0.05F;
                  radius = 0.3F;
                  float charge = (float)minecraft.thePlayer.getItemInUseDuration() / 20.0F;
                  charge = (charge * charge + charge * 2.0F) / 3.0F;
                  if (charge < 0.1F) {
                     return;
                  }

                  if (charge > 1.0F) {
                     charge = 1.0F;
                  }

                  speed = charge * 3.0F;
               } else if (item instanceof ItemFishingRod && this.projectiles.getValue()) {
                  gravity = 0.04F;
                  radius = 0.25F;
                  drag = 0.92F;
               } else if ((item instanceof ItemSnowball || item instanceof ItemEgg) && this.projectiles.getValue()) {
                  gravity = 0.03F;
                  radius = 0.25F;
               } else {
                  if (!(item instanceof ItemEnderPearl) || !this.pearls.getValue()) {
                     return;
                  }

                  gravity = 0.03F;
                  radius = 0.25F;
               }

               float yaw = minecraft.thePlayer.rotationYaw;
               float pitch = minecraft.thePlayer.rotationPitch;
               double x = ((AccessorRenderManager)renderManager).getRenderPosX() - (double)MathHelper.cos(yaw / 180.0F * (float) Math.PI) * 0.16;
               double y = ((AccessorRenderManager)renderManager).getRenderPosY() + (double)minecraft.thePlayer.getEyeHeight() - 0.1F;
               double z = ((AccessorRenderManager)renderManager).getRenderPosZ() - (double)MathHelper.sin(yaw / 180.0F * (float) Math.PI) * 0.16;
               double motionX = (double)(MathHelper.sin(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(pitch / 180.0F * (float) Math.PI))
                  * (arrow ? 1.0 : 0.4)
                  * -1.0;
               double motionY = (double)MathHelper.sin(pitch / 180.0F * (float) Math.PI) * (arrow ? 1.0 : 0.4) * -1.0;
               double motionZ = (double)(MathHelper.cos(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(pitch / 180.0F * (float) Math.PI))
                  * (arrow ? 1.0 : 0.4);
               float magnitude = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
               motionX /= (double)magnitude;
               motionY /= (double)magnitude;
               motionZ /= (double)magnitude;
               motionX *= (double)speed;
               motionY *= (double)speed;
               motionZ *= (double)speed;
               MovingObjectPosition hit = null;
               boolean collided = false;
               boolean hitEntity = false;
               WorldRenderer renderer = Tessellator.getInstance().getWorldRenderer();
               ArrayList<Vec3> points = new ArrayList<>();

               while (!collided && y > 0.0) {
                  Vec3 start = new Vec3(x, y, z);
                  Vec3 end = new Vec3(x + motionX, y + motionY, z + motionZ);
                  hit = minecraft.theWorld.rayTraceBlocks(start, end, false, true, false);
                  start = new Vec3(x, y, z);
                  end = new Vec3(x + motionX, y + motionY, z + motionZ);
                  AxisAlignedBB searchBox = new AxisAlignedBB(
                        x - (double)radius, y - (double)radius, z - (double)radius, x + (double)radius, y + (double)radius, z + (double)radius
                     )
                     .addCoord(motionX, motionY, motionZ)
                     .expand(1.0, 1.0, 1.0);
                  int firstChunkX = MathHelper.floor_double((searchBox.minX - 2.0) / 16.0);
                  int lastChunkX = MathHelper.floor_double((searchBox.maxX + 2.0) / 16.0);
                  int firstChunkZ = MathHelper.floor_double((searchBox.minZ - 2.0) / 16.0);
                  int lastChunkZ = MathHelper.floor_double((searchBox.maxZ + 2.0) / 16.0);
                  ArrayList<Entity> entities = new ArrayList<>();

                  for (int chunkX = firstChunkX; chunkX <= lastChunkX; chunkX++) {
                     for (int chunkZ = firstChunkZ; chunkZ <= lastChunkZ; chunkZ++) {
                        minecraft.theWorld.getChunkFromChunkCoords(chunkX, chunkZ).getEntitiesWithinAABBForEntity(minecraft.thePlayer, searchBox, entities, null);
                     }
                  }

                  for (Entity entity : entities) {
                     if (entity.canBeCollidedWith() && entity != minecraft.thePlayer) {
                        AxisAlignedBB bounds = entity.getEntityBoundingBox().expand((double)radius, (double)radius, (double)radius);
                        MovingObjectPosition intercept = bounds.calculateIntercept(start, end);
                        if (intercept != null) {
                           hitEntity = true;
                           collided = true;
                        }
                     }
                  }

                  x += motionX;
                  y += motionY;
                  z += motionZ;
                  if (minecraft.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock().getMaterial() == Material.water) {
                     motionX *= 0.6;
                     motionY *= 0.6;
                     motionZ *= 0.6;
                  } else {
                     motionX *= (double)drag;
                     motionY *= (double)drag;
                     motionZ *= (double)drag;
                  }

                  motionY -= (double)gravity;
                  points.add(
                     new Vec3(
                        x - ((AccessorRenderManager)renderManager).getRenderPosX(),
                        y - ((AccessorRenderManager)renderManager).getRenderPosY(),
                        z - ((AccessorRenderManager)renderManager).getRenderPosZ()
                     )
                  );
               }

               if (points.size() > 1) {
                  RenderUtils.beginOverlay();
                  RenderUtils.setColor(
                     new Color(hitEntity ? 85 : 255, 255, hitEntity ? 85 : 255, (int)(this.opacity.getValue().floatValue() / 100.0F * 255.0F)).getRGB()
                  );
                  GL11.glLineWidth(1.5F);
                  GL11.glEnable(2848);
                  GL11.glHint(3154, 4354);
                  renderer.begin(3, DefaultVertexFormats.POSITION);
                  points.forEach(point -> renderer.pos(point.xCoord, point.yCoord, point.zCoord).endVertex());
                  Tessellator.getInstance().draw();
                  GlStateManager.pushMatrix();
                  GlStateManager.translate(
                     x - ((AccessorRenderManager)renderManager).getRenderPosX(),
                     y - ((AccessorRenderManager)renderManager).getRenderPosY(),
                     z - ((AccessorRenderManager)renderManager).getRenderPosZ()
                  );
                  GlStateManager.popMatrix();
                  GL11.glDisable(2848);
                  GL11.glLineWidth(2.0F);
                  GlStateManager.resetColor();
                  RenderUtils.endOverlay();
               }
            }
         }
      }
   }
}
