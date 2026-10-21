package myau;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.util.MathHelper;

public class AntiFireballModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private final ArrayList<EntityFireball> candidateFireballs = new ArrayList<>();
   private final ArrayList<EntityFireball> ignoredCloseFireballs = new ArrayList<>();
   private EntityFireball targetFireball;
   public final FloatSetting range = new FloatSetting("range", 5.0F, 3.0F, 8.0F);
   public final IntegerSetting fov = new IntegerSetting("fov", 360, 1, 360);
   public final BooleanSetting rotations = new BooleanSetting("rotations", true);
   public final BooleanSetting swing = new BooleanSetting("swing", true);
   public final ModeSetting moveFix = new ModeSetting("move-fix", 1, new String[]{"NONE", "SILENT", "STRICT"});
   public final ModeSetting showTarget = new ModeSetting("show-target", 0, new String[]{"NONE", "DEFAULT", "HUD"});

   public AntiFireballModule() {
      super("AntiFireball", false, 0);
   }

   private boolean isValidTarget(EntityFireball fireball) {
      if (fireball.getEntityBoundingBox().hasNaN()) {
         return false;
      } else {
         return RotationUtils.distanceToEntity(fireball) > (double)this.range.getValue().floatValue() + 3.0
            ? false
            : RotationUtils.entityAimError(fireball) <= (float)this.fov.getValue().intValue();
      }
   }

   private void swing() {
      if (this.swing.getValue()) {
         minecraft.thePlayer.swingItem();
      } else {
         PacketUtils.send(new C0APacketAnimation());
      }
   }

   @EventHandler
   public void onPreTick(PreTickEvent event) {
      if (this.isEnabled()) {
         List<EntityFireball> loadedFireballs = minecraft.theWorld
            .loadedEntityList
            .stream()
            .filter(entity -> entity instanceof EntityFireball)
            .map(entity -> (EntityFireball)entity)
            .collect(Collectors.toList());
         this.candidateFireballs.removeIf(fireballx -> !loadedFireballs.contains(fireballx));
         this.ignoredCloseFireballs.removeIf(fireballx -> !loadedFireballs.contains(fireballx));

         for (EntityFireball fireball : loadedFireballs) {
            if (!this.candidateFireballs.contains(fireball) && !this.ignoredCloseFireballs.contains(fireball)) {
               if (RotationUtils.distanceToEntity(fireball) > 3.0) {
                  this.candidateFireballs.add(fireball);
               } else {
                  this.ignoredCloseFireballs.add(fireball);
               }
            }
         }

         if (minecraft.thePlayer.capabilities.allowFlying) {
            this.targetFireball = null;
         } else {
            this.targetFireball = this.candidateFireballs
               .stream()
               .filter(this::isValidTarget)
               .min(Comparator.comparingDouble(RotationUtils::distanceToEntity))
               .orElse(null);
         }
      }
   }

   @EventHandler(
      priority = -100
   )
   public void onRotationUpdate(RotationUpdateEvent event) {
      if (this.isEnabled() && EntityUtils.isLoaded(this.targetFireball)) {
         if (this.rotations.getValue() && !ItemUtils.isHoldingBlocks() && !ItemUtils.isHoldingBow() && !ItemUtils.isHoldingFireball()) {
            float previousYaw = event.getPreviousYaw();
            float previousPitch = event.getPreviousPitch();
            float[] targetRotation = RotationUtils.aimAtBounds(this.targetFireball.getEntityBoundingBox());
            float yaw = RotationUtils.snapToMouseSensitivity(RotationUtils.nearestEquivalentAngle(targetRotation[0], previousYaw), previousYaw);
            float pitch = RotationUtils.snapToMouseSensitivity(targetRotation[1], previousPitch);
            pitch = MathHelper.clamp_float(pitch, -90.0F, 90.0F);
            event.setRotation(yaw, pitch, 0);
            if (this.moveFix.getValue() != 0) {
               event.setMovementYaw(yaw, 0);
            }
         }

         if (!MyauClient.packetActionTracker.sentUseEntity && !MyauClient.packetActionTracker.sentDigging && !MyauClient.packetActionTracker.sentBlockPlacement
            )
          {
            this.swing();
            if (RotationUtils.distanceToEntity(this.targetFireball) <= (double)this.range.getValue().floatValue()) {
               PacketUtils.send(new C02PacketUseEntity(this.targetFireball, Action.ATTACK));
               PlayerUtils.attackLocally(this.targetFireball);
            }
         }
      }
   }

   @EventHandler
   public void onMovementInput(MovementInputEvent event) {
      if (this.isEnabled()
         && this.moveFix.getValue() == 1
         && RotationState.isActive()
         && RotationState.movementPriority() == 0.0F
         && MovementUtils.hasMovementInput()) {
         MovementUtils.correctMovementForYaw(RotationState.movementYaw());
      }
   }

   @EventHandler
   public void onRenderWorld(RenderWorldEvent event) {
      if (this.isEnabled() && this.showTarget.getValue() != 0 && EntityUtils.isLoaded(this.targetFireball)) {
         Color color = Color.WHITE;
         switch (this.showTarget.getValue()) {
            case 1:
               double approachDotProduct = (this.targetFireball.posX - this.targetFireball.lastTickPosX)
                     * (minecraft.thePlayer.posX - this.targetFireball.posX)
                  + (this.targetFireball.posY - this.targetFireball.lastTickPosY)
                     * (
                        minecraft.thePlayer.posY
                           + (double)minecraft.thePlayer.getEyeHeight()
                           - this.targetFireball.posY
                           - (double)this.targetFireball.height / 2.0
                     )
                  + (this.targetFireball.posZ - this.targetFireball.lastTickPosZ)
                     * (minecraft.thePlayer.posZ - this.targetFireball.posZ);
               color = new Color(approachDotProduct < 0.0 ? 16733525 : 5635925);
               break;
            case 2:
               color = ((HUDModule)MyauClient.moduleManager.modules.get(HUDModule.class)).getColor(System.currentTimeMillis());
         }

         RenderUtils.beginOverlay();
         RenderUtils.fillEntityBounds(this.targetFireball, color.getRed(), color.getGreen(), color.getBlue(), 63, 0.1F);
         RenderUtils.endOverlay();
      }
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      this.candidateFireballs.clear();
      this.ignoredCloseFireballs.clear();
   }
}
