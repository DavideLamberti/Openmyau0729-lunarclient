package myau;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;
import myau.mixin.AccessorPlayerControllerMP;
import myau.mixin.AccessorRenderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

public class LagRangeModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private int delayTicks = -1;
   private long remainingDelayMillis;
   private boolean delaying;
   private Vec3 previousPosition;
   private Vec3 currentPosition;
   public final IntegerSetting delay = new IntegerSetting("delay", 100, 0, 1000);
   public final FloatSetting range = new FloatSetting("range", 10.0F, 3.0F, 100.0F);
   public final BooleanSetting weaponsOnly = new BooleanSetting("weapons-only", true);
   public final BooleanSetting allowTools = new BooleanSetting("allow-tools", false, this.weaponsOnly::getValue);
   public final BooleanSetting botCheck = new BooleanSetting("bot-check", true);
   public final BooleanSetting teams = new BooleanSetting("teams", true);
   public final ModeSetting showPosition = new ModeSetting("show-position", 0, new String[]{"NONE", "DEFAULT", "HUD"});

   public LagRangeModule() {
      super("LagRange", false, 0);
   }

   private boolean isTarget(EntityPlayer player) {
      if (player == minecraft.thePlayer
         || player == minecraft.thePlayer.ridingEntity
         || player == minecraft.getRenderViewEntity()
         || player == minecraft.getRenderViewEntity().ridingEntity) {
         return false;
      } else if (player.deathTime > 0 || EntityUtils.isFriend(player)) {
         return false;
      } else if (EntityUtils.isEnemy(player)) {
         return true;
      } else {
         return this.teams.getValue() && EntityUtils.isTeammate(player) ? false : !this.botCheck.getValue() || !EntityUtils.isBot(player);
      }
   }

   private boolean shouldFlush(Packet packet) {
      if (packet instanceof C02PacketUseEntity) {
         return true;
      } else if (packet instanceof C07PacketPlayerDigging) {
         return ((C07PacketPlayerDigging)packet).getStatus() != Action.RELEASE_USE_ITEM;
      } else if (!(packet instanceof C08PacketPlayerBlockPlacement)) {
         return false;
      } else {
         ItemStack stack = ((C08PacketPlayerBlockPlacement)packet).getStack();
         return stack == null || !(stack.getItem() instanceof ItemSword);
      }
   }

   @EventHandler(
      priority = -50
   )
   public void onPreTick(PreTickEvent event) {
      if (this.isEnabled()) {
         MyauClient.packetDelayManager.setDelayTicks(0);
         this.delaying = false;
         BedNukerModule conflictingModule = (BedNukerModule)MyauClient.moduleManager.modules.get(BedNukerModule.class);
         if (conflictingModule.isEnabled() && conflictingModule.isControllingInteractions()) {
            this.delayTicks = -1;
         } else if (!((AccessorPlayerControllerMP)minecraft.playerController).getIsHittingBlock()
            && (!minecraft.thePlayer.isUsingItem() || minecraft.thePlayer.isBlocking())
            && (!this.weaponsOnly.getValue() || ItemUtils.isHoldingWeapon() || this.allowTools.getValue() && ItemUtils.isHoldingTool())) {
            List<EntityPlayer> targets = minecraft.theWorld
               .loadedEntityList
               .stream()
               .filter(entity -> entity instanceof EntityPlayer)
               .map(EntityPlayer.class::cast)
               .filter(this::isTarget)
               .collect(Collectors.toList());
            if (targets.isEmpty()) {
               this.delayTicks = -1;
            } else {
               double eyeHeight = (double)minecraft.thePlayer.getEyeHeight();
               Vec3 bufferedEyes = MyauClient.packetDelayManager.lastReleasedPosition().addVector(0.0, eyeHeight, 0.0);
               Vec3 previousEyes = new Vec3(
                  minecraft.thePlayer.lastTickPosX, minecraft.thePlayer.lastTickPosY + eyeHeight, minecraft.thePlayer.lastTickPosZ
               );
               Vec3 currentEyes = new Vec3(
                  minecraft.thePlayer.posX, minecraft.thePlayer.posY + eyeHeight, minecraft.thePlayer.posZ
               );

               for (EntityPlayer target : targets) {
                  double currentDistance = RotationUtils.distanceToEntityFrom(target, currentEyes);
                  if (!(currentDistance > (double)this.range.getValue().floatValue())) {
                     double previousDistance = RotationUtils.distanceToEntityFrom(target, previousEyes);
                     double bufferedDistance = RotationUtils.distanceToEntityFrom(target, bufferedEyes);
                     if (currentDistance < previousDistance || currentDistance < bufferedDistance) {
                        this.delayTicks = 0;

                        for (this.remainingDelayMillis = this.remainingDelayMillis + (long)this.delay.getValue().intValue();
                           this.remainingDelayMillis > 0L;
                           this.remainingDelayMillis -= 50L
                        ) {
                           this.delayTicks++;
                        }

                        MyauClient.packetDelayManager.setDelayTicks(this.delayTicks);
                        this.delaying = true;
                        break;
                     }
                  }
               }
            }
         } else {
            this.delayTicks = -1;
         }
      }
   }

   @EventHandler(
      priority = -50
   )
   public void onPostTick(PostTickEvent event) {
      if (this.isEnabled()) {
         Vec3 position = MyauClient.packetDelayManager.lastReleasedPosition();
         this.previousPosition = this.currentPosition == null ? position : this.currentPosition;
         this.currentPosition = position;
      }
   }

   @EventHandler
   public void onPacketSend(PacketSendEvent event) {
      boolean flush = this.isEnabled();
      if (flush && !event.isCancelled()) {
         flush = this.shouldFlush(event.getPacket());
         if (flush) {
            MyauClient.packetDelayManager.setDelayTicks(0);
            this.delayTicks = -1;
         }
      }
   }

   @EventHandler(
      priority = 50
   )
   public void onRenderWorld(RenderWorldEvent event) {
      boolean draw = this.isEnabled();
      if (draw && this.showPosition.getValue() != 0 && minecraft.gameSettings.thirdPersonView != 0) {
         draw = this.delaying;
         if (draw && this.previousPosition != null) {
            if (this.currentPosition != null) {
               Color positionColor = new Color(-1);
               switch (this.showPosition.getValue()) {
                  case 1:
                     positionColor = EntityUtils.teamColor(minecraft.thePlayer, 1.0F);
                     break;
                  case 2:
                     positionColor = ((HUDModule)MyauClient.moduleManager.modules.get(HUDModule.class)).getColor(System.currentTimeMillis());
               }

               double x = MathUtils.lerpDouble(this.previousPosition.xCoord, this.currentPosition.xCoord, (double)event.getPartialTicks());
               double y = MathUtils.lerpDouble(this.previousPosition.yCoord, this.currentPosition.yCoord, (double)event.getPartialTicks());
               double z = MathUtils.lerpDouble(this.previousPosition.zCoord, this.currentPosition.zCoord, (double)event.getPartialTicks());
               float border = minecraft.thePlayer.getCollisionBorderSize();
               AxisAlignedBB bounds = new AxisAlignedBB(
                     x - (double)minecraft.thePlayer.width / 2.0,
                     y,
                     z - (double)minecraft.thePlayer.width / 2.0,
                     x + (double)minecraft.thePlayer.width / 2.0,
                     y + (double)minecraft.thePlayer.height,
                     z + (double)minecraft.thePlayer.width / 2.0
                  )
                  .expand((double)border, (double)border, (double)border)
                  .offset(
                     -((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosX(),
                     -((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosY(),
                     -((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosZ()
                  );
               RenderUtils.beginOverlay();
               RenderUtils.fillBounds(bounds, positionColor.getRed(), positionColor.getGreen(), positionColor.getBlue(), 63);
               RenderUtils.endOverlay();
            }
         }
      }
   }

   @Override
   public void onDisable() {
      MyauClient.packetDelayManager.setDelayTicks(0);
      this.delayTicks = -1;
      this.remainingDelayMillis = 0L;
      this.delaying = false;
      this.previousPosition = null;
      this.currentPosition = null;
   }

   @Override
   public String[] getHudInfo() {
      return new String[]{String.format("%dms", this.delay.getValue())};
   }
}
