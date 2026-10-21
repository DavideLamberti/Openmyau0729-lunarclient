package myau;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import myau.mixin.AccessorEntityRenderer;
import myau.mixin.AccessorRenderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.util.vector.Vector4f;

public class ESPModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private final OutlineShader outlineShader = new OutlineShader();
   private final ColorMaskShader colorShader = new ColorMaskShader();
   private Framebuffer outlineFramebuffer;
   private boolean nameTagsAllowed = true;
   private boolean enchantmentGlintAllowed = true;
   public final ModeSetting mode = new ModeSetting("mode", 2, new String[]{"NONE", "2D", "3D", "OUTLINE"});
   public final ModeSetting color = new ModeSetting("color", 0, new String[]{"DEFAULT", "TEAMS", "HUD"});
   public final ModeSetting healthBar = new ModeSetting("health-bar", 0, new String[]{"NONE", "2D", "RAVEN"});
   public final BooleanSetting players = new BooleanSetting("players", true);
   public final BooleanSetting friends = new BooleanSetting("friends", true);
   public final BooleanSetting enemies = new BooleanSetting("enemies", true);
   public final BooleanSetting self = new BooleanSetting("self", false);
   public final BooleanSetting bots = new BooleanSetting("bots", false);

   public ESPModule() {
      super("ESP", false, 0);
   }

   public boolean areNameTagsAllowed() {
      return this.nameTagsAllowed;
   }

   public boolean isEnchantmentGlintAllowed() {
      return this.enchantmentGlintAllowed;
   }

   private boolean isEligible(EntityPlayer player) {
      if (!EntityUtils.isLocalOrViewPlayer(player)) {
         if (EntityUtils.isFriend(player)) {
            return this.friends.getValue();
         } else if (EntityUtils.isEnemy(player)) {
            return this.enemies.getValue();
         } else {
            return EntityUtils.isBot(player) ? this.bots.getValue() : this.players.getValue();
         }
      } else {
         return this.self.getValue() && minecraft.gameSettings.thirdPersonView != 0;
      }
   }

   private boolean shouldRender(EntityPlayer player) {
      return this.isEligible(player) && RenderUtils.isEntityInFrustum(player, 0.1F);
   }

   private Color colorFor(EntityPlayer player) {
      if (EntityUtils.isFriend(player)) {
         return MyauClient.friendList.color();
      } else if (EntityUtils.isEnemy(player)) {
         return MyauClient.enemyList.color();
      } else {
         switch (this.color.getValue()) {
            case 0:
               return EntityUtils.teamColor(player, 1.0F);
            case 1:
               boolean teammate = EntityUtils.isTeammate(player);
               byte teamColor = (byte)(teammate ? ChatColor.BLUE.getRgb() : ChatColor.RED.getRgb());
               return new Color(teamColor);
            case 2:
               return ((HUDModule)MyauClient.moduleManager.modules.get(HUDModule.class)).getColor(System.currentTimeMillis());
            default:
               return new Color(-1);
         }
      }
   }

   private void reportFramebufferUnavailable() {
      this.setEnabled(false);
      ChatUtils.addFormattedMessage(String.format("%s%s: &cFramebuffer is not enabled!&r", "&7[&bMyau&7] &r", this.getName()));
   }

   @EventHandler
   public void onFramebufferResize(FramebufferResizeEvent event) {
      if (this.outlineFramebuffer != null) {
         this.outlineFramebuffer.deleteFramebuffer();
      }

      this.outlineFramebuffer = new Framebuffer(minecraft.displayWidth, minecraft.displayHeight, false);
   }

   @EventHandler(
      priority = 50
   )
   public void onRenderOverlay(RenderOverlayEvent event) {
      if (this.isEnabled()) {
         int selectedMode = this.mode.getValue();
         if (selectedMode == 1 || selectedMode == 3 || this.healthBar.getValue() == 1) {
            if (selectedMode == 3 && !OpenGlHelper.isFramebufferEnabled()) {
               this.reportFramebufferUnavailable();
            } else {
               List<EntityPlayer> targets = EntityUtils.entitiesBackToFront()
                  .stream()
                  .filter(entity -> entity instanceof EntityPlayer)
                  .map(EntityPlayer.class::cast)
                  .filter(this::shouldRender)
                  .collect(Collectors.toList());
               if (!targets.isEmpty()) {
                  if (selectedMode == 3) {
                     this.drawOutlines(event, targets);
                  }

                  if (selectedMode == 1 || this.healthBar.getValue() == 1) {
                     Map<EntityPlayer, Vector4f> screenBounds = new HashMap<>();
                     ((AccessorEntityRenderer)minecraft.entityRenderer).callSetupCameraTransform(event.getPartialTicks(), 0);

                     for (EntityPlayer player : targets) {
                        screenBounds.put(player, RenderUtils.projectEntityBounds(player, 0.1F));
                     }

                     minecraft.entityRenderer.setupOverlayRendering();
                     GlStateManager.enableBlend();
                     GlStateManager.blendFunc(770, 771);
                     GlStateManager.disableTexture2D();
                     GlStateManager.disableAlpha();

                     for (EntityPlayer player : targets) {
                        Vector4f bounds = screenBounds.get(player);
                        if (bounds != null) {
                           float left = bounds.x;
                           float top = bounds.y;
                           float right = bounds.z;
                           float bottom = bounds.w;
                           if (selectedMode == 1) {
                              int rgb = this.colorFor(player).getRGB();
                              RenderUtils.drawBorderedRect(left, top, right, bottom, 3.0F, 0, (rgb & 16579836) >> 2 | rgb & 0xFF000000);
                              RenderUtils.drawBorderedRect(left, top, right, bottom, 1.5F, 0, rgb);
                           }

                           if (this.healthBar.getValue() == 1) {
                              float health = player.getHealth() + player.getAbsorptionAmount();
                              float fraction = Math.min(Math.max(health / player.getMaxHealth(), 0.0F), 1.0F);
                              float offset = (right - left) * 0.08F;
                              int rgb = ColorUtils.healthGradient(fraction).getRGB();
                              RenderUtils.drawLine2D(left - offset, top, left - offset, bottom, 3.0F, (rgb & 16579836) >> 2 | rgb & 0xFF000000);
                              RenderUtils.drawLine2D(left - offset, bottom, left - offset, bottom + (top - bottom) * fraction, 1.5F, rgb);
                           }
                        }
                     }

                     GlStateManager.enableAlpha();
                     GlStateManager.enableTexture2D();
                     GlStateManager.disableBlend();
                  }
               }
            }
         }
      }
   }

   private void drawOutlines(RenderOverlayEvent event, List<EntityPlayer> targets) {
      GlStateManager.pushMatrix();
      GlStateManager.pushAttrib();
      if (this.outlineFramebuffer == null) {
         this.outlineFramebuffer = new Framebuffer(minecraft.displayWidth, minecraft.displayHeight, false);
      } else {
         this.outlineFramebuffer.framebufferClear();
      }

      this.outlineFramebuffer.bindFramebuffer(false);
      ((AccessorEntityRenderer)minecraft.entityRenderer).callSetupCameraTransform(event.getPartialTicks(), 0);
      boolean shadows = minecraft.gameSettings.entityShadows;
      minecraft.gameSettings.entityShadows = false;
      this.nameTagsAllowed = false;
      this.enchantmentGlintAllowed = false;
      this.colorShader.use();

      for (EntityPlayer player : targets) {
         Color tint = this.colorFor(player);
         this.colorShader.setColor(tint);
         boolean invisible = player.isInvisible();
         player.setInvisible(false);
         minecraft.getRenderManager().renderEntityStatic(player, event.getPartialTicks(), true);
         player.setInvisible(invisible);
      }

      this.colorShader.stop();
      this.enchantmentGlintAllowed = true;
      this.nameTagsAllowed = true;
      minecraft.gameSettings.entityShadows = shadows;
      minecraft.entityRenderer.setupOverlayRendering();
      minecraft.getFramebuffer().bindFramebuffer(false);
      this.outlineShader.use();
      RenderUtils.drawFramebuffer(this.outlineFramebuffer);
      this.outlineShader.stop();
      GlStateManager.popAttrib();
      GlStateManager.popMatrix();
   }

   @EventHandler
   public void onRenderWorld(RenderWorldEvent event) {
      if (this.isEnabled()) {
         if (this.mode.getValue() == 2 || this.healthBar.getValue() == 2) {
            if (this.mode.getValue() == 3 && !OpenGlHelper.isFramebufferEnabled()) {
               this.reportFramebufferUnavailable();
            } else {
               RenderUtils.beginOverlay();

               for (EntityPlayer player : EntityUtils.entitiesBackToFront()
                  .stream()
                  .filter(entity -> entity instanceof EntityPlayer)
                  .map(EntityPlayer.class::cast)
                  .filter(this::shouldRender)
                  .collect(Collectors.toList())) {
                  if (this.mode.getValue() == 2) {
                     Color tint = this.colorFor(player);
                     RenderUtils.outlineEntityBounds(player, tint.getRed(), tint.getGreen(), tint.getBlue(), tint.getAlpha(), 1.5F, 0.1F);
                  }

                  if (this.healthBar.getValue() == 2) {
                     double x = MathUtils.lerpDouble(player.lastTickPosX, player.posX, (double)event.getPartialTicks())
                        - ((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosX();
                     double y = MathUtils.lerpDouble(player.lastTickPosY, player.posY, (double)event.getPartialTicks())
                        - ((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosY()
                        - 0.1F;
                     double z = MathUtils.lerpDouble(player.lastTickPosZ, player.posZ, (double)event.getPartialTicks())
                        - ((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosZ();
                     GlStateManager.pushMatrix();
                     GlStateManager.translate(x, y, z);
                     GlStateManager.rotate(minecraft.getRenderManager().playerViewY * -1.0F, 0.0F, 1.0F, 0.0F);
                     float currentHealth = player.getHealth() + player.getAbsorptionAmount();
                     float healthFraction = Math.min(Math.max(currentHealth / player.getMaxHealth(), 0.0F), 1.0F);
                     Color tint = ColorUtils.healthGradient(healthFraction);
                     float left = 0.6F;
                     float height = player.height + 0.2F;
                     float width = 0.1F;
                     float border = width * 0.275F;
                     RenderUtils.fillSmoothRect(left - border, -border, left + width + border, height + border, Color.black.getRGB());
                     RenderUtils.fillSmoothRect(left, 0.0F, left + width, height, Color.darkGray.getRGB());
                     RenderUtils.fillSmoothRect(left, 0.0F, left + width, height * healthFraction, tint.getRGB());
                     GlStateManager.popMatrix();
                  }
               }

               RenderUtils.endOverlay();
            }
         }
      }
   }

   @Override
   public void onEnable() {
      if (this.mode.getValue() == 3 && !OpenGlHelper.isFramebufferEnabled()) {
         this.reportFramebufferUnavailable();
      }
   }
}
