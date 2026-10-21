package myau;

import net.minecraft.client.Minecraft;
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

public class ChamsModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
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

   public ChamsModule() {
      super("Chams", false, 0);
   }

   public boolean shouldRenderEntity(EntityLivingBase entity) {
      int deathTime = entity.deathTime;
      if (deathTime > 0) {
         return false;
      } else if (minecraft.getRenderViewEntity().getDistanceToEntity(entity) > 512.0F) {
         return false;
      } else if (entity instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)entity;
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
      } else if (!(entity instanceof EntityDragon) && !(entity instanceof EntityWither)) {
         if (!(entity instanceof EntityMob) && !(entity instanceof EntitySlime)) {
            return !(entity instanceof EntityAnimal)
                  && !(entity instanceof EntityBat)
                  && !(entity instanceof EntitySquid)
                  && !(entity instanceof EntityVillager)
               ? false
               : this.animals.getValue();
         } else if (entity instanceof EntityCreeper) {
            return this.creepers.getValue();
         } else if (entity instanceof EntityEnderman) {
            return this.endermen.getValue();
         } else {
            return entity instanceof EntityBlaze ? this.blazes.getValue() : this.mobs.getValue();
         }
      } else {
         return !entity.isInvisible() && this.bosses.getValue();
      }
   }
}
