package myau;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;

public final class EntityUtils {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private static final int DEFAULT_TEAM_COLOR = 16777215;

   private EntityUtils() {
   }

   public static boolean isLoaded(Entity entity) {
      return entity != null && minecraft.theWorld.loadedEntityList.contains(entity);
   }

   public static List<Entity> entitiesBackToFront() {
      RenderManager renderManager = minecraft.getRenderManager();
      ArrayList<Entity> entities = new ArrayList<>(minecraft.theWorld.loadedEntityList);
      entities.sort((first, second) -> compareRenderOrder(renderManager, first, second));
      return entities;
   }

   public static float armorAdjustedHealth(EntityLivingBase entity) {
      return entity.getHealth() * (20.0F / (float)entity.getTotalArmorValue());
   }

   public static String formattedName(Entity entity) {
      return entity.getDisplayName().getFormattedText().replaceAll("§\\S$", "").replaceAll("(?i)§r", "§f").trim();
   }

   public static Color teamColor(EntityPlayer player, float alpha) {
      int rgb = 16777215;
      ScorePlayerTeam team = (ScorePlayerTeam)player.getTeam();
      if (team == null) {
         return fromRgb(rgb, alpha);
      } else {
         String formatting = FontRenderer.getFormatFromString(team.getColorPrefix());
         if (formatting.length() < 2) {
            return fromRgb(rgb, alpha);
         } else {
            rgb = minecraft.fontRendererObj.getColorCode(formatting.charAt(1));
            return fromRgb(rgb, alpha);
         }
      }
   }

   private static Color fromRgb(int rgb, float alpha) {
      return new Color((float)(rgb >> 16 & 0xFF) / 255.0F, (float)(rgb >> 8 & 0xFF) / 255.0F, (float)(rgb & 0xFF) / 255.0F, alpha);
   }

   public static boolean isLocalOrViewPlayer(EntityPlayer player) {
      return player == minecraft.thePlayer || player == minecraft.getRenderViewEntity();
   }

   public static boolean isBot(EntityPlayer player) {
      if (player == minecraft.thePlayer) {
         return false;
      } else {
         NetworkPlayerInfo playerInfo = minecraft.getNetHandler().getPlayerInfo(player.getName());
         if (playerInfo == null) {
            return true;
         } else if (!ScoreboardUtils.isHypixelScoreboard()) {
            return false;
         } else if (player.getName().startsWith("§k")) {
            return player.isInvisible();
         } else if (playerInfo.getResponseTime() < 1) {
            return true;
         } else {
            ScorePlayerTeam team = playerInfo.getPlayerTeam();
            if (team != null) {
               if (team.getTeamName().isEmpty() && team.getColorPrefix().equals("§c")) {
                  return true;
               }

               if (!team.getColorPrefix().isEmpty()) {
                  return false;
               }
            }

            return playerInfo.getGameProfile().getId().version() == 2;
         }
      }
   }

   public static boolean isTeammate(EntityPlayer player) {
      if (player == minecraft.thePlayer) {
         return true;
      } else {
         NetworkPlayerInfo localInfo = minecraft.getNetHandler().getPlayerInfo(minecraft.thePlayer.getUniqueID());
         if (localInfo != null && localInfo.getPlayerTeam() != null) {
            NetworkPlayerInfo targetInfo = minecraft.getNetHandler().getPlayerInfo(player.getUniqueID());
            return targetInfo != null && targetInfo.getPlayerTeam() != null
               ? localInfo.getPlayerTeam().getColorPrefix().equals(targetInfo.getPlayerTeam().getColorPrefix())
               : false;
         } else {
            return false;
         }
      }
   }

   public static boolean hasTeamArmorStand(EntityLivingBase entity) {
      if (entity == minecraft.thePlayer) {
         return true;
      } else {
         NetworkPlayerInfo localInfo = minecraft.getNetHandler().getPlayerInfo(minecraft.thePlayer.getUniqueID());
         if (localInfo == null) {
            return false;
         } else {
            ScorePlayerTeam localTeam = localInfo.getPlayerTeam();
            if (localTeam != null && localTeam.getColorPrefix().length() >= 2) {
               EntityLivingBase armorStand = (EntityLivingBase)minecraft.theWorld.findNearestEntityWithinAABB(EntityArmorStand.class, entity.getEntityBoundingBox(), entity);
               return armorStand != null && armorStand.getName().contains(localTeam.getColorPrefix().substring(0, 2));
            } else {
               return false;
            }
         }
      }
   }

   public static boolean isShopNPC(EntityLivingBase entity) {
      EntityLivingBase armorStand = (EntityLivingBase)minecraft.theWorld.findNearestEntityWithinAABB(EntityArmorStand.class, entity.getEntityBoundingBox(), entity);
      if (armorStand == null) {
         return false;
      } else {
         String name = armorStand.getName();
         return name.contains("RIGHT CLICK")
            || name.contains("ITEM SHOP")
            || name.contains("UPGRADES")
            || name.contains("BANKER")
            || name.contains("STREAK POWERS");
      }
   }

   public static boolean isFriend(EntityPlayer player) {
      return MyauClient.friendList.contains(player.getName());
   }

   public static boolean isEnemy(EntityPlayer player) {
      return MyauClient.enemyList.contains(player.getName());
   }

   private static int compareRenderOrder(RenderManager renderManager, Entity first, Entity second) {
      return Double.compare(
         renderManager.getDistanceToCamera(second.posX, second.posY, second.posZ),
         renderManager.getDistanceToCamera(first.posX, first.posY, first.posZ)
      );
   }
}
