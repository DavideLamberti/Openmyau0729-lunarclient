package myau;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import myau.mixin.AccessorEntityRenderer;
import myau.mixin.AccessorMinecraft;
import myau.mixin.AccessorRenderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public final class RenderUtils {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private static final Frustum frustum = new Frustum();
   private static final IntBuffer viewportBuffer = GLAllocation.createDirectIntBuffer(16);
   private static final FloatBuffer modelViewBuffer = GLAllocation.createDirectFloatBuffer(16);
   private static final FloatBuffer projectionBuffer = GLAllocation.createDirectFloatBuffer(16);
   private static final FloatBuffer screenCoordsBuffer = GLAllocation.createDirectFloatBuffer(4);

   private RenderUtils() {
   }

   public static void drawOutlinedText(String text, float x, float y, int color) {
      String plainText = text.replaceAll("(?i)§[\\da-f]", "");
      minecraft.fontRendererObj.drawString(plainText, x + 1.0F, y, 0, false);
      minecraft.fontRendererObj.drawString(plainText, x - 1.0F, y, 0, false);
      minecraft.fontRendererObj.drawString(plainText, x, y + 1.0F, 0, false);
      minecraft.fontRendererObj.drawString(plainText, x, y - 1.0F, 0, false);
      minecraft.fontRendererObj.drawString(text, x, y, color, false);
   }

   public static void drawItemWithEnchantments(ItemStack stack, int x, int y) {
      GlStateManager.pushMatrix();
      GlStateManager.depthMask(true);
      GlStateManager.clear(256);
      RenderHelper.enableGUIStandardItemLighting();
      GL11.glDisable(2896);
      GlStateManager.pushMatrix();
      GlStateManager.scale(1.0F, 1.0F, -0.01F);
      minecraft.getRenderItem().zLevel = -150.0F;
      minecraft.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
      minecraft.getRenderItem().renderItemOverlays(minecraft.fontRendererObj, stack, x, y);
      minecraft.getRenderItem().zLevel = 0.0F;
      GlStateManager.popMatrix();
      RenderHelper.disableStandardItemLighting();
      GlStateManager.enableAlpha();
      GlStateManager.disableBlend();
      GlStateManager.enableTexture2D();
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      GlStateManager.scale(0.5F, 0.5F, 0.5F);
      GlStateManager.disableDepth();
      List<String> enchantmentLabels = EnchantmentText.labels(stack);

      for (int index = 0; index < enchantmentLabels.size(); index++) {
         drawOutlinedText(enchantmentLabels.get(index), (float)x * 2.0F, ((float)y + (float)index * 4.0F) * 2.0F, -1);
      }

      GlStateManager.enableDepth();
      GlStateManager.scale(2.0F, 2.0F, 2.0F);
      GlStateManager.popMatrix();
   }

   public static void drawPotionIcon(PotionEffect effect, int x, int y) {
      Potion potion = Potion.potionTypes[effect.getPotionID()];
      int iconIndex = potion.getStatusIconIndex();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      GlStateManager.depthMask(true);
      GlStateManager.clear(256);
      GlStateManager.pushMatrix();
      GlStateManager.scale(1.0F, 1.0F, -0.01F);
      minecraft.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
      Gui.drawModalRectWithCustomSizedTexture(x, y, (float)(iconIndex % 8 * 18), (float)(198 + iconIndex / 8 * 18), 18, 18, 256.0F, 256.0F);
      GlStateManager.popMatrix();
      GlStateManager.enableAlpha();
      GlStateManager.disableBlend();
      GlStateManager.enableTexture2D();
      GlStateManager.popMatrix();
   }

   public static void fillRect(float left, float top, float right, float bottom, int color) {
      if (color != 0) {
         setColor(color);
         GL11.glBegin(9);
         GL11.glVertex2f(left, top);
         GL11.glVertex2f(left, bottom);
         GL11.glVertex2f(right, bottom);
         GL11.glVertex2f(right, top);
         GL11.glEnd();
         GlStateManager.resetColor();
      }
   }

   public static void drawBorderedRect(float left, float top, float right, float bottom, float lineWidth, int fillColor, int borderColor) {
      fillRect(left, top, right, bottom, fillColor);
      if (!(lineWidth <= 0.0F) && borderColor != 0) {
         setColor(borderColor);
         GL11.glLineWidth(lineWidth);
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         GL11.glBegin(1);
         GL11.glVertex2f(left, top);
         GL11.glVertex2f(left, bottom);
         GL11.glVertex2f(right, bottom);
         GL11.glVertex2f(right, top);
         GL11.glVertex2f(left, top);
         GL11.glVertex2f(right, top);
         GL11.glVertex2f(left, bottom);
         GL11.glVertex2f(right, bottom);
         GL11.glEnd();
         GL11.glDisable(2848);
         GL11.glLineWidth(2.0F);
         GlStateManager.resetColor();
      }
   }

   public static void fillSmoothRect(float left, float top, float right, float bottom, int color) {
      if (color != 0) {
         setColor(color);
         GL11.glEnable(2881);
         GL11.glHint(3155, 4354);
         GL11.glBegin(9);

         for (int pass = 0; pass < 2; pass++) {
            GL11.glVertex2f(left, top);
            GL11.glVertex2f(left, bottom);
            GL11.glVertex2f(right, bottom);
            GL11.glVertex2f(right, top);
         }

         GL11.glEnd();
         GL11.glDisable(2881);
         GlStateManager.resetColor();
      }
   }

   public static void drawLine2D(float startX, float startY, float endX, float endY, float lineWidth, int color) {
      setColor(color);
      GL11.glLineWidth(lineWidth);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glBegin(1);
      GL11.glVertex2f(startX, startY);
      GL11.glVertex2f(endX, endY);
      GL11.glEnd();
      GL11.glDisable(2848);
      GL11.glLineWidth(2.0F);
      GlStateManager.resetColor();
   }

   public static void drawTracer(Vec3 start, double worldX, double worldY, double worldZ, float red, float green, float blue, float alpha, float lineWidth) {
      GlStateManager.pushMatrix();
      GlStateManager.color(red, green, blue, alpha);
      boolean viewBobbing = minecraft.gameSettings.viewBobbing;
      minecraft.gameSettings.viewBobbing = false;
      ((AccessorEntityRenderer)minecraft.entityRenderer).callSetupCameraTransform(((AccessorMinecraft)minecraft).getTimer().renderPartialTicks, 2);
      minecraft.gameSettings.viewBobbing = viewBobbing;
      GL11.glLineWidth(lineWidth);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glBegin(1);
      GL11.glVertex3d(start.xCoord, start.yCoord, start.zCoord);
      GL11.glVertex3d(
         worldX - ((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosX(),
         worldY - ((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosY(),
         worldZ - ((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosZ()
      );
      GL11.glEnd();
      GL11.glDisable(2848);
      GL11.glLineWidth(2.0F);
      GlStateManager.resetColor();
      GlStateManager.popMatrix();
   }

   public static void drawChevron(float x, float y, float angleRadians, float length, float lineWidth, int color) {
      float leftAngle = angleRadians + (float)Math.toRadians(45.0);
      float rightAngle = angleRadians - (float)Math.toRadians(45.0);
      setColor(color);
      GL11.glLineWidth(lineWidth);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glBegin(1);
      GL11.glVertex2f(x, y);
      GL11.glVertex2f(x + length * (float)Math.cos((double)leftAngle), y + length * (float)Math.sin((double)leftAngle));
      GL11.glVertex2f(x, y);
      GL11.glVertex2f(x + length * (float)Math.cos((double)rightAngle), y + length * (float)Math.sin((double)rightAngle));
      GL11.glEnd();
      GL11.glDisable(2848);
      GL11.glLineWidth(2.0F);
      GlStateManager.resetColor();
   }

   public static void fillArrow(float x, float y, float angleRadians, float length, int color) {
      float leftAngle = angleRadians + (float)Math.toRadians(26.25);
      float rightAngle = angleRadians - (float)Math.toRadians(26.25);
      setColor(color);
      GL11.glEnable(2881);
      GL11.glHint(3155, 4354);
      GL11.glBegin(9);
      GL11.glVertex2f(x, y);
      GL11.glVertex2f(x + length * (float)Math.cos((double)leftAngle), y + length * (float)Math.sin((double)leftAngle));
      GL11.glVertex2f(x + length * (float)Math.cos((double)rightAngle), y + length * (float)Math.sin((double)rightAngle));
      GL11.glEnd();
      GL11.glDisable(2881);
      GlStateManager.resetColor();
   }

   public static void drawFramebuffer(Framebuffer framebuffer) {
      ScaledResolution scaledResolution = new ScaledResolution(minecraft);
      GlStateManager.bindTexture(framebuffer.framebufferTexture);
      GL11.glBegin(7);
      GL11.glTexCoord2d(0.0, 1.0);
      GL11.glVertex2d(0.0, 0.0);
      GL11.glTexCoord2d(0.0, 0.0);
      GL11.glVertex2d(0.0, (double)scaledResolution.getScaledHeight());
      GL11.glTexCoord2d(1.0, 0.0);
      GL11.glVertex2d((double)scaledResolution.getScaledWidth(), (double)scaledResolution.getScaledHeight());
      GL11.glTexCoord2d(1.0, 1.0);
      GL11.glVertex2d((double)scaledResolution.getScaledWidth(), 0.0);
      GL11.glEnd();
   }

   public static void drawHorizontalCircle(double x, double y, double z, double radius, int segments, float lineWidth, int color) {
      setColor(color);
      GL11.glLineWidth(lineWidth);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glBegin(2);

      for (int segment = 0; segment <= segments; segment++) {
         double angle = (double)segment * ((Math.PI * 2) / (double)segments);
         GL11.glVertex3d(x + Math.cos(angle) * radius, y, z + Math.sin(angle) * radius);
      }

      GL11.glEnd();
      GL11.glDisable(2848);
      GL11.glLineWidth(2.0F);
      GlStateManager.resetColor();
   }

   public static void drawEntityCircle(Entity entity, double radius, int segments, float lineWidth, int color) {
      float partialTicks = ((AccessorMinecraft)minecraft).getTimer().renderPartialTicks;
      double x = MathUtils.lerpDouble(entity.lastTickPosX, entity.posX, (double)partialTicks)
         - ((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosX();
      double y = MathUtils.lerpDouble(entity.lastTickPosY, entity.posY, (double)partialTicks)
         - ((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosY();
      double z = MathUtils.lerpDouble(entity.lastTickPosZ, entity.posZ, (double)partialTicks)
         - ((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosZ();
      drawHorizontalCircle(x, y, z, radius, segments, lineWidth, color);
   }

   public static void fillBounds(AxisAlignedBB bounds, int red, int green, int blue, int alpha) {
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer renderer = tessellator.getWorldRenderer();
      renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
      renderer.pos(bounds.minX, bounds.minY, bounds.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.minX, bounds.minY, bounds.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.maxX, bounds.minY, bounds.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.maxX, bounds.minY, bounds.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.minX, bounds.maxY, bounds.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.minX, bounds.maxY, bounds.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.maxX, bounds.maxY, bounds.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.maxX, bounds.maxY, bounds.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.minX, bounds.minY, bounds.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.minX, bounds.maxY, bounds.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.maxX, bounds.maxY, bounds.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.maxX, bounds.minY, bounds.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.minX, bounds.minY, bounds.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.minX, bounds.maxY, bounds.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.maxX, bounds.maxY, bounds.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.maxX, bounds.minY, bounds.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.minX, bounds.minY, bounds.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.minX, bounds.maxY, bounds.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.minX, bounds.maxY, bounds.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.minX, bounds.minY, bounds.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.maxX, bounds.minY, bounds.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.maxX, bounds.maxY, bounds.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.maxX, bounds.maxY, bounds.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.maxX, bounds.minY, bounds.maxZ).color(red, green, blue, alpha).endVertex();
      tessellator.draw();
   }

   public static void outlineBounds(AxisAlignedBB bounds, int red, int green, int blue, int alpha, float lineWidth) {
      GL11.glLineWidth(lineWidth);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer renderer = tessellator.getWorldRenderer();
      renderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
      renderer.pos(bounds.minX, bounds.minY, bounds.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.maxX, bounds.minY, bounds.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.maxX, bounds.minY, bounds.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.minX, bounds.minY, bounds.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.minX, bounds.minY, bounds.minZ).color(red, green, blue, alpha).endVertex();
      tessellator.draw();
      renderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
      renderer.pos(bounds.minX, bounds.maxY, bounds.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.maxX, bounds.maxY, bounds.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.maxX, bounds.maxY, bounds.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.minX, bounds.maxY, bounds.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.minX, bounds.maxY, bounds.minZ).color(red, green, blue, alpha).endVertex();
      tessellator.draw();
      renderer.begin(1, DefaultVertexFormats.POSITION_COLOR);
      renderer.pos(bounds.minX, bounds.minY, bounds.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.minX, bounds.maxY, bounds.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.maxX, bounds.minY, bounds.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.maxX, bounds.maxY, bounds.minZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.maxX, bounds.minY, bounds.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.maxX, bounds.maxY, bounds.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.minX, bounds.minY, bounds.maxZ).color(red, green, blue, alpha).endVertex();
      renderer.pos(bounds.minX, bounds.maxY, bounds.maxZ).color(red, green, blue, alpha).endVertex();
      tessellator.draw();
      GL11.glDisable(2848);
      GL11.glLineWidth(2.0F);
   }

   public static void fillEntityBounds(Entity entity, int red, int green, int blue, int alpha, double expansion) {
      float partialTicks = ((AccessorMinecraft)minecraft).getTimer().renderPartialTicks;
      double x = MathUtils.lerpDouble(entity.lastTickPosX, entity.posX, (double)partialTicks);
      double y = MathUtils.lerpDouble(entity.lastTickPosY, entity.posY, (double)partialTicks);
      double z = MathUtils.lerpDouble(entity.lastTickPosZ, entity.posZ, (double)partialTicks);
      AxisAlignedBB bounds = entity.getEntityBoundingBox()
         .expand(expansion, expansion, expansion)
         .offset(x - entity.posX, y - entity.posY, z - entity.posZ)
         .offset(
            -((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosX(),
            -((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosY(),
            -((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosZ()
         );
      fillBounds(bounds, red, green, blue, alpha);
   }

   public static void outlineEntityBounds(Entity entity, int red, int green, int blue, int alpha, float lineWidth, double expansion) {
      float partialTicks = ((AccessorMinecraft)minecraft).getTimer().renderPartialTicks;
      double x = MathUtils.lerpDouble(entity.lastTickPosX, entity.posX, (double)partialTicks);
      double y = MathUtils.lerpDouble(entity.lastTickPosY, entity.posY, (double)partialTicks);
      double z = MathUtils.lerpDouble(entity.lastTickPosZ, entity.posZ, (double)partialTicks);
      AxisAlignedBB bounds = entity.getEntityBoundingBox()
         .expand(expansion, expansion, expansion)
         .offset(x - entity.posX, y - entity.posY, z - entity.posZ)
         .offset(
            -((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosX(),
            -((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosY(),
            -((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosZ()
         );
      outlineBounds(bounds, red, green, blue, alpha, lineWidth);
   }

   public static void fillBlockBounds(BlockPos position, double height, int red, int green, int blue, int alpha) {
      AxisAlignedBB bounds = new AxisAlignedBB(
            (double)position.getX(),
            (double)position.getY(),
            (double)position.getZ(),
            (double)position.getX() + 1.0,
            (double)position.getY() + height,
            (double)position.getZ() + 1.0
         )
         .offset(
            -((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosX(),
            -((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosY(),
            -((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosZ()
         );
      fillBounds(bounds, red, green, blue, alpha);
   }

   public static void outlineBlockBounds(BlockPos position, double height, int red, int green, int blue, int alpha, float lineWidth) {
      AxisAlignedBB bounds = new AxisAlignedBB(
            (double)position.getX(),
            (double)position.getY(),
            (double)position.getZ(),
            (double)position.getX() + 1.0,
            (double)position.getY() + height,
            (double)position.getZ() + 1.0
         )
         .offset(
            -((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosX(),
            -((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosY(),
            -((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosZ()
         );
      outlineBounds(bounds, red, green, blue, alpha, lineWidth);
   }

   public static Vector4f projectEntityBounds(Entity entity, double expansion) {
      float partialTicks = ((AccessorMinecraft)minecraft).getTimer().renderPartialTicks;
      AccessorRenderManager renderManager = (AccessorRenderManager)minecraft.getRenderManager();
      ScaledResolution scaledResolution = new ScaledResolution(minecraft);
      float scaleFactor = (float)scaledResolution.getScaleFactor();
      double x = MathUtils.lerpDouble(entity.lastTickPosX, entity.posX, (double)partialTicks);
      double y = MathUtils.lerpDouble(entity.lastTickPosY, entity.posY, (double)partialTicks);
      double z = MathUtils.lerpDouble(entity.lastTickPosZ, entity.posZ, (double)partialTicks);
      AxisAlignedBB bounds = entity.getEntityBoundingBox()
         .expand(expansion, expansion, expansion)
         .offset(x - entity.posX, y - entity.posY, z - entity.posZ)
         .offset(-renderManager.getRenderPosX(), -renderManager.getRenderPosY(), -renderManager.getRenderPosZ());
      GL11.glGetFloat(2982, modelViewBuffer);
      GL11.glGetFloat(2983, projectionBuffer);
      GL11.glGetInteger(2978, viewportBuffer);
      Vector3f[] corners = new Vector3f[]{
         new Vector3f((float)bounds.minX, (float)bounds.minY, (float)bounds.minZ),
         new Vector3f((float)bounds.minX, (float)bounds.maxY, (float)bounds.minZ),
         new Vector3f((float)bounds.maxX, (float)bounds.minY, (float)bounds.minZ),
         new Vector3f((float)bounds.maxX, (float)bounds.maxY, (float)bounds.minZ),
         new Vector3f((float)bounds.minX, (float)bounds.minY, (float)bounds.maxZ),
         new Vector3f((float)bounds.minX, (float)bounds.maxY, (float)bounds.maxZ),
         new Vector3f((float)bounds.maxX, (float)bounds.minY, (float)bounds.maxZ),
         new Vector3f((float)bounds.maxX, (float)bounds.maxY, (float)bounds.maxZ)
      };
      Vector4f screenBounds = null;

      for (Vector3f corner : corners) {
         if (GLU.gluProject(corner.x, corner.y, corner.z, modelViewBuffer, projectionBuffer, viewportBuffer, screenCoordsBuffer)) {
            float projectedX = screenCoordsBuffer.get(0) / scaleFactor;
            float projectedY = (float)scaledResolution.getScaledHeight() - screenCoordsBuffer.get(1) / scaleFactor;
            float projectedZ = screenCoordsBuffer.get(2);
            if (!(projectedZ < 0.0F) && !(projectedZ > 1.0F)) {
               if (screenBounds == null) {
                  screenBounds = new Vector4f(projectedX, projectedY, projectedX, projectedY);
               } else {
                  screenBounds.x = Math.min(projectedX, screenBounds.x);
                  screenBounds.y = Math.min(projectedY, screenBounds.y);
                  screenBounds.z = Math.max(projectedX, screenBounds.z);
                  screenBounds.w = Math.max(projectedY, screenBounds.w);
               }
            }
         }
      }

      return screenBounds;
   }

   public static boolean isEntityInFrustum(Entity entity, double expansion) {
      Entity viewEntity = minecraft.getRenderViewEntity();
      if (viewEntity != null && !(viewEntity.getDistanceToEntity(entity) > 512.0F)) {
         float partialTicks = ((AccessorMinecraft)minecraft).getTimer().renderPartialTicks;
         double x = MathUtils.lerpDouble(entity.lastTickPosX, entity.posX, (double)partialTicks);
         double y = MathUtils.lerpDouble(entity.lastTickPosY, entity.posY, (double)partialTicks);
         double z = MathUtils.lerpDouble(entity.lastTickPosZ, entity.posZ, (double)partialTicks);
         AxisAlignedBB bounds = entity.getEntityBoundingBox()
            .expand(expansion, expansion, expansion)
            .offset(x - entity.posX, y - entity.posY, z - entity.posZ);
         frustum.setPosition(viewEntity.posX, viewEntity.posY, viewEntity.posZ);
         return frustum.isBoundingBoxInFrustum(bounds);
      } else {
         return false;
      }
   }

   public static void beginOverlay() {
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      GlStateManager.disableTexture2D();
      GlStateManager.disableCull();
      GlStateManager.disableAlpha();
      GlStateManager.disableDepth();
   }

   public static void endOverlay() {
      GlStateManager.enableDepth();
      GlStateManager.enableAlpha();
      GlStateManager.enableCull();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
   }

   public static void setColor(int argb) {
      float alpha = (float)(argb >> 24 & 0xFF) / 255.0F;
      float red = (float)(argb >> 16 & 0xFF) / 255.0F;
      float green = (float)(argb >> 8 & 0xFF) / 255.0F;
      float blue = (float)(argb & 0xFF) / 255.0F;
      GlStateManager.color(red, green, blue, alpha);
   }
}
