package myau;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import myau.mixin.AccessorRenderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.StringUtils;

public class NameTagsModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private static final DecimalFormat heartFormat = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));
   public final FloatSetting scale = new FloatSetting("scale", 1.0F, 0.5F, 2.0F);
   public final BooleanSetting autoScale = new BooleanSetting("auto-scale", true);
   public final PercentSetting background = new PercentSetting("background", 25);
   public final BooleanSetting shadow = new BooleanSetting("shadow", true);
   public final ModeSetting distance = new ModeSetting("distance", 0, new String[]{"NONE", "DEFAULT", "VAPE"});
   public final ModeSetting health = new ModeSetting("health", 2, new String[]{"NONE", "HP", "HEARTS", "TAB"});
   public final BooleanSetting armor = new BooleanSetting("armor", true);
   public final BooleanSetting effects = new BooleanSetting("effects", true);
   public final BooleanSetting players = new BooleanSetting("players", true);
   public final BooleanSetting friends = new BooleanSetting("friends", true);
   public final BooleanSetting enemies = new BooleanSetting("enemies", true);
   public final BooleanSetting bosses = new BooleanSetting("bosses", false);
   public final BooleanSetting mobs = new BooleanSetting("mobs", false);
   public final BooleanSetting creepers = new BooleanSetting("creepers", false);
   public final BooleanSetting endermen = new BooleanSetting("endermen", false);
   public final BooleanSetting blazes = new BooleanSetting("blazes", false);
   public final BooleanSetting animals = new BooleanSetting("animals", false);
   public final BooleanSetting self = new BooleanSetting("self", false);
   public final BooleanSetting bots = new BooleanSetting("bots", false);

   public NameTagsModule() {
      super("NameTags", false, 0);
   }

   public boolean shouldRenderNameTag(EntityLivingBase entity) {
      if (entity.deathTime > 0 || minecraft.getRenderViewEntity().getDistanceToEntity(entity) > 512.0F) {
         return false;
      } else if (entity instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)entity;
         if (EntityUtils.isLocalOrViewPlayer(player)) {
            return this.self.getValue() && minecraft.gameSettings.thirdPersonView != 0;
         } else if (EntityUtils.isFriend(player)) {
            return this.friends.getValue();
         } else if (EntityUtils.isEnemy(player)) {
            return this.enemies.getValue();
         } else {
            return EntityUtils.isBot(player) ? this.bots.getValue() : this.players.getValue();
         }
      } else if (entity instanceof EntityDragon || entity instanceof EntityWither) {
         return !entity.isInvisible() && this.bosses.getValue();
      } else if (!(entity instanceof EntityMob) && !(entity instanceof EntitySlime)) {
         return !(entity instanceof EntityAnimal) && !(entity instanceof EntityBat) && !(entity instanceof EntitySquid) && !(entity instanceof EntityVillager)
            ? false
            : this.animals.getValue();
      } else if (entity instanceof EntityCreeper) {
         return this.creepers.getValue();
      } else if (entity instanceof EntityEnderman) {
         return this.endermen.getValue();
      } else {
         return entity instanceof EntityBlaze ? this.blazes.getValue() : this.mobs.getValue();
      }
   }

   @EventHandler
   public void onRenderWorld(RenderWorldEvent event) {
      if (this.isEnabled()) {
         for (Entity entity : EntityUtils.entitiesBackToFront()) {
            if (entity instanceof EntityLivingBase
               && this.shouldRenderNameTag((EntityLivingBase)entity)
               && (entity.ignoreFrustumCheck || RenderUtils.isEntityInFrustum(entity, 10.0))) {
               String displayName = EntityUtils.formattedName(entity);
               if (!StringUtils.isBlank(EnumChatFormatting.getTextWithoutFormattingCodes(displayName))) {
                  double x = MathUtils.lerpDouble(entity.lastTickPosX, entity.posX, (double)event.getPartialTicks())
                     - ((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosX();
                  double y = MathUtils.lerpDouble(entity.lastTickPosY, entity.posY, (double)event.getPartialTicks())
                     - ((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosY()
                     + (double)entity.getEyeHeight();
                  double z = MathUtils.lerpDouble(entity.lastTickPosZ, entity.posZ, (double)event.getPartialTicks())
                     - ((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosZ();
                  double entityDistance = (double)minecraft.getRenderViewEntity().getDistanceToEntity(entity);
                  GlStateManager.pushMatrix();
                  GlStateManager.translate(x, y + (entity.isSneaking() ? 0.225 : 0.4), z);
                  float rotation = minecraft.getRenderManager().playerViewY * -1.0F;
                  float axisX = 0.0F;
                  GlStateManager.rotate(rotation, 0.0F, 1.0F, 0.0F);
                  rotation = minecraft.getRenderManager().playerViewX;
                  axisX = minecraft.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F;
                  GlStateManager.rotate(rotation, axisX, 0.0F, 0.0F);
                  double labelScale = Math.pow(Math.min(Math.max(this.autoScale.getValue() ? entityDistance : 0.0, 6.0), 128.0), 0.75) * 0.0075;
                  GlStateManager.scale(
                     -labelScale * (double)this.scale.getValue().floatValue(), -labelScale * (double)this.scale.getValue().floatValue(), 1.0
                  );
                  String distancePrefix = "";
                  switch (this.distance.getValue()) {
                     case 1:
                        distancePrefix = String.format("&7%dm&r ", (int)entityDistance);
                        break;
                     case 2:
                        distancePrefix = String.format("&a[&f%d&a]&r ", (int)entityDistance);
                  }

                  float currentHealth = ((EntityLivingBase)entity).getHealth();
                  float absorption = ((EntityLivingBase)entity).getAbsorptionAmount();
                  float maxHealth = ((EntityLivingBase)entity).getMaxHealth();
                  float healthFraction = Math.min(Math.max((currentHealth + absorption) / maxHealth, 0.0F), 1.0F);
                  String healthSuffix = "";
                  switch (this.health.getValue()) {
                     case 1:
                        healthSuffix = String.format(" %d%s", (int)currentHealth, absorption > 0.0F ? String.format(" &6%d&r", (int)absorption) : "&r");
                        break;
                     case 2:
                        String hearts = heartFormat.format((double)currentHealth / 2.0);
                        String absorptionHearts = absorption > 0.0F ? String.format(" &6%s&r", heartFormat.format((double)absorption / 2.0)) : "&r";
                        healthSuffix = String.format(" %s%s", hearts, absorptionHearts);
                        break;
                     case 3:
                        if (entity instanceof EntityPlayer) {
                           Scoreboard scoreboard = minecraft.theWorld.getScoreboard();
                           if (scoreboard != null) {
                              ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(2);
                              if (objective != null) {
                                 Score score = scoreboard.getValueFromObjective(entity.getName(), objective);
                                 if (score != null) {
                                    healthSuffix = String.format(" &e%d&r", score.getScorePoints());
                                 }
                              }
                           }
                        }
                  }

                  String label = ChatColor.formatText(String.format("%s&f%s&r%s", distancePrefix, displayName, healthSuffix));
                  int width = minecraft.fontRendererObj.getStringWidth(label);
                  if (this.background.getValue() > 0) {
                     Color backgroundColor = !entity.isSneaking() && !entity.isInvisible()
                        ? new Color(0.0F, 0.0F, 0.0F, (float)this.background.getValue().intValue() / 100.0F)
                        : new Color(0.33F, 0.0F, 0.33F, (float)this.background.getValue().intValue() / 100.0F);
                     RenderUtils.beginOverlay();
                     RenderUtils.fillRect(
                        (float)(-width) / 2.0F - 1.0F,
                        (float)(-minecraft.fontRendererObj.FONT_HEIGHT) - 1.0F,
                        (float)width / 2.0F + (this.shadow.getValue() ? 1.0F : 0.0F),
                        this.shadow.getValue() ? 0.0F : -1.0F,
                        backgroundColor.getRGB()
                     );
                     RenderUtils.endOverlay();
                  }

                  GlStateManager.disableDepth();
                  minecraft.fontRendererObj
                     .drawString(
                        label,
                        (float)(-width) / 2.0F,
                        (float)(-minecraft.fontRendererObj.FONT_HEIGHT),
                        ColorUtils.healthGradient(healthFraction).getRGB(),
                        this.shadow.getValue()
                     );
                  GlStateManager.enableDepth();
                  if (entity instanceof EntityPlayer) {
                     this.drawPlayerDetails((EntityPlayer)entity, width);
                  }

                  GlStateManager.popMatrix();
               }
            }
         }
      }
   }

   private void drawPlayerDetails(EntityPlayer player, int labelWidth) {
      int rowOffset = minecraft.fontRendererObj.FONT_HEIGHT + 2;
      if (this.armor.getValue()) {
         ArrayList<ItemStack> equipment = new ArrayList<>();

         for (int slot = 4; slot >= 0; slot--) {
            ItemStack stack = slot == 0 ? player.getHeldItem() : player.inventory.armorInventory[slot - 1];
            if (stack != null) {
               equipment.add(stack);
            }
         }

         if (!equipment.isEmpty()) {
            int left = equipment.size() * -8;

            for (int index = 0; index < equipment.size(); index++) {
               RenderUtils.drawItemWithEnchantments(equipment.get(index), left + index * 16, -rowOffset - 16);
            }

            rowOffset += 16;
         }
      }

      if (this.effects.getValue()) {
         List<PotionEffect> visibleEffects = player.getActivePotionEffects().stream().filter(NameTagsModule::hasStatusIcon).collect(Collectors.toList());
         if (!visibleEffects.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5F, 0.5F, 1.0F);
            int left = visibleEffects.size() * -9;

            for (int index = 0; index < visibleEffects.size(); index++) {
               RenderUtils.drawPotionIcon(visibleEffects.get(index), left + index * 18, -(rowOffset * 2) - 18);
            }

            GlStateManager.popMatrix();
         }
      }

      if (EntityUtils.isFriend(player)) {
         RenderUtils.beginOverlay();
         RenderUtils.drawBorderedRect(
            (float)(-labelWidth) / 2.0F - 1.0F,
            (float)(-minecraft.fontRendererObj.FONT_HEIGHT) - 1.0F,
            (float)labelWidth / 2.0F + 1.0F,
            this.shadow.getValue() ? 0.0F : -1.0F,
            1.5F,
            0,
            MyauClient.friendList.color().getRGB()
         );
         RenderUtils.endOverlay();
      } else if (EntityUtils.isEnemy(player)) {
         RenderUtils.beginOverlay();
         RenderUtils.drawBorderedRect(
            (float)(-labelWidth) / 2.0F - 1.0F,
            (float)(-minecraft.fontRendererObj.FONT_HEIGHT) - 1.0F,
            (float)labelWidth / 2.0F + 1.0F,
            this.shadow.getValue() ? 0.0F : -1.0F,
            1.5F,
            0,
            MyauClient.enemyList.color().getRGB()
         );
         RenderUtils.endOverlay();
      }
   }

   private static boolean hasStatusIcon(PotionEffect effect) {
      return Potion.potionTypes[effect.getPotionID()].hasStatusIcon();
   }
}
