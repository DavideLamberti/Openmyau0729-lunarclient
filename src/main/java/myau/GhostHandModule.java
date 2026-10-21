package myau;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class GhostHandModule extends Module {
   public final BooleanSetting teamOnly = new BooleanSetting("team-only", true);
   public final BooleanSetting ignoreWeapons = new BooleanSetting("ignore-weapons", false);

   public GhostHandModule() {
      super("GhostHand", false, 0);
   }

   public boolean shouldIgnoreEntity(Entity entity) {
      boolean player = entity instanceof EntityPlayer;
      if (!player) {
         return false;
      } else if (EntityUtils.isBot((EntityPlayer)entity)) {
         return false;
      } else {
         return this.teamOnly.getValue() && !EntityUtils.isTeammate((EntityPlayer)entity)
            ? false
            : !this.ignoreWeapons.getValue() || !ItemUtils.isHoldingWeapon();
      }
   }
}
