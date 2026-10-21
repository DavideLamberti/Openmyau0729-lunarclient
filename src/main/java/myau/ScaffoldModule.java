package myau;

import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.WorldSettings.GameType;

public class ScaffoldModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private static final double[] FACE_SAMPLE_OFFSETS = new double[]{
      0.03125, 0.09375, 0.15625, 0.21875, 0.28125, 0.34375, 0.40625, 0.46875, 0.53125, 0.59375, 0.65625, 0.71875, 0.78125, 0.84375, 0.90625, 0.96875
   };
   private int placementDelayTicks = 0;
   private int previousHotbarSlot = -1;
   private int remainingStackBlocks = -1;
   private float placementYaw = -180.0F;
   private float placementPitch = 0.0F;
   private boolean hasPlacementRotation = false;
   private float tickPlacementYaw;
   private float tickPlacementPitch;
   private int towerPhase = 0;
   private int towerCycleCount = 0;
   private int keepYState = 0;
   private int keepYLevel = 256;
   private boolean keepYRecoveryPlacement = false;
   private boolean tellyRotationActive = false;
   private EnumFacing pendingPlacementFace = null;
   public final ModeSetting rotations = new ModeSetting("rotations", 2, new String[]{"NONE", "DEFAULT", "BACKWARDS", "SIDEWAYS"});
   public final ModeSetting moveFix = new ModeSetting("move-fix", 1, new String[]{"NONE", "SILENT"});
   public final ModeSetting sprint = new ModeSetting("sprint", 0, new String[]{"NONE", "VANILLA"});
   public final PercentSetting groundMotion = new PercentSetting("ground-motion", 100);
   public final PercentSetting airMotion = new PercentSetting("air-motion", 100);
   public final PercentSetting speedMotion = new PercentSetting("speed-motion", 100);
   public final ModeSetting tower = new ModeSetting("tower", 0, new String[]{"NONE", "VANILLA", "EXTRA", "TELLY"});
   public final ModeSetting keepY = new ModeSetting("keep-y", 0, new String[]{"NONE", "VANILLA", "EXTRA", "TELLY"});
   public final BooleanSetting keepYOnPress = new BooleanSetting("keep-y-on-press", false, this::isKeepYOnPressVisible);
   public final BooleanSetting multiPlace = new BooleanSetting("multi-place", true);
   public final BooleanSetting safeWalk = new BooleanSetting("safe-walk", true);
   public final BooleanSetting swing = new BooleanSetting("swing", true);
   public final BooleanSetting itemSpoof = new BooleanSetting("item-spoof", false);
   public final BooleanSetting blockCounter = new BooleanSetting("block-counter", true);

   private boolean shouldDisableSprint() {
      if (this.isTellyTakeoff()) {
         return false;
      } else {
         boolean standardKeepY = this.keepY.getValue() == 1 || this.keepY.getValue() == 2;
         return standardKeepY && this.keepYState > 0 ? false : this.sprint.getValue() == 0;
      }
   }

   private boolean canPlaceWithActiveModules() {
      BedNukerModule bedNuker = (BedNukerModule)MyauClient.moduleManager.modules.get(BedNukerModule.class);
      if (bedNuker.isEnabled() && bedNuker.isControllingInteractions()) {
         return false;
      } else {
         LongJumpModule longJump = (LongJumpModule)MyauClient.moduleManager.modules.get(LongJumpModule.class);
         return !longJump.isEnabled() || !longJump.isAutomaticFireballMode() || longJump.hasJumpStarted();
      }
   }

   private EnumFacing findConnectingFace(BlockPos supportBlock, BlockPos desiredPlacementBlock) {
      EnumFacing bestFace = null;
      double bestDistanceSquared = 0.0;

      for (EnumFacing candidateFace : EnumFacing.VALUES) {
         if (candidateFace != EnumFacing.DOWN) {
            BlockPos neighborBlock = supportBlock.offset(candidateFace);
            if (neighborBlock.getY() <= desiredPlacementBlock.getY() && BlockUtils.isReplaceableAt(neighborBlock)) {
               double distanceSquared = neighborBlock.distanceSqToCenter(
                  (double)desiredPlacementBlock.getX() + 0.5,
                  (double)desiredPlacementBlock.getY() + 0.5,
                  (double)desiredPlacementBlock.getZ() + 0.5
               );
               if (bestFace == null || distanceSquared < bestDistanceSquared || distanceSquared == bestDistanceSquared && candidateFace == EnumFacing.UP) {
                  bestDistanceSquared = distanceSquared;
                  bestFace = candidateFace;
               }
            }
         }
      }

      return bestFace;
   }

   private BlockPlacementTarget findPlacementTarget() {
      int playerBlockY = MathHelper.floor_double(minecraft.thePlayer.posY);
      int placementY = this.keepYState != 0 && !this.keepYRecoveryPlacement ? Math.min(playerBlockY, this.keepYLevel) : playerBlockY;
      BlockPos desiredPlacementBlock = new BlockPos(
         MathHelper.floor_double(minecraft.thePlayer.posX), placementY - 1, MathHelper.floor_double(minecraft.thePlayer.posZ)
      );
      if (!BlockUtils.isReplaceableAt(desiredPlacementBlock)) {
         return null;
      } else {
         ArrayList<BlockPos> supportCandidates = new ArrayList<>();
         double blockReach = (double)minecraft.playerController.getBlockReachDistance();

         for (int offsetX = -4; offsetX <= 4; offsetX++) {
            for (int offsetY = -4; offsetY <= 0; offsetY++) {
               for (int offsetZ = -4; offsetZ <= 4; offsetZ++) {
                  BlockPos candidateBlock = desiredPlacementBlock.add(offsetX, offsetY, offsetZ);
                  if (!BlockUtils.isReplaceableAt(candidateBlock) && !BlockUtils.isInteractableAt(candidateBlock)) {
                     double distanceToCandidate = minecraft.thePlayer
                        .getDistance(
                           (double)candidateBlock.getX() + 0.5,
                           (double)candidateBlock.getY() + 0.5,
                           (double)candidateBlock.getZ() + 0.5
                        );
                     if (!(distanceToCandidate > blockReach)
                        && (this.keepYState == 0 || this.keepYRecoveryPlacement || candidateBlock.getY() < this.keepYLevel)) {
                        for (EnumFacing face : EnumFacing.VALUES) {
                           if (face != EnumFacing.DOWN && BlockUtils.isReplaceableAt(candidateBlock.offset(face))) {
                              supportCandidates.add(candidateBlock);
                           }
                        }
                     }
                  }
               }
            }
         }

         if (supportCandidates.isEmpty()) {
            return null;
         } else {
            supportCandidates.sort(Comparator.comparingDouble(supportCandidate -> targetDistanceSquared(desiredPlacementBlock, supportCandidate)));
            BlockPos selectedSupportBlock = supportCandidates.get(0);
            EnumFacing connectingFace = this.findConnectingFace(selectedSupportBlock, desiredPlacementBlock);
            return connectingFace == null ? null : new BlockPlacementTarget(selectedSupportBlock, connectingFace);
         }
      }
   }

   private MovingObjectPosition tracePlacement(BlockPos position, EnumFacing facing, float yaw, float pitch) {
      MovingObjectPosition rayHit = RotationUtils.traceBlocks(yaw, pitch, (double)minecraft.playerController.getBlockReachDistance(), 1.0F);
      return rayHit != null && rayHit.typeOfHit == MovingObjectType.BLOCK && rayHit.getBlockPos().equals(position) && rayHit.sideHit == facing
         ? rayHit
         : null;
   }

   private void placeBlock(BlockPos position, EnumFacing facing) {
      if (ItemUtils.isHoldingPlaceableBlock() && this.remainingStackBlocks > 0) {
         MovingObjectPosition rayHit = this.tracePlacement(position, facing, this.tickPlacementYaw, this.tickPlacementPitch);
         if (rayHit != null) {
            boolean placed = minecraft.playerController
               .onPlayerRightClick(
                  minecraft.thePlayer,
                  minecraft.theWorld,
                  minecraft.thePlayer.inventory.getCurrentItem(),
                  position,
                  facing,
                  rayHit.hitVec
               );
            if (placed) {
               if (minecraft.playerController.getCurrentGameType() != GameType.CREATIVE) {
                  this.remainingStackBlocks--;
               }

               if (this.swing.getValue()) {
                  minecraft.thePlayer.swingItem();
               } else {
                  PacketUtils.send(new C0APacketAnimation());
               }
            }
         }
      }
   }

   private EnumFacing facingFromYaw(float yaw) {
      if (yaw < -135.0F || yaw > 135.0F) {
         return EnumFacing.NORTH;
      } else if (yaw < -45.0F) {
         return EnumFacing.EAST;
      } else {
         return yaw < 45.0F ? EnumFacing.SOUTH : EnumFacing.WEST;
      }
   }

   private double distanceToBlockEdge(EnumFacing facing) {
      switch (facing) {
         case NORTH:
            return minecraft.thePlayer.posZ - Math.floor(minecraft.thePlayer.posZ);
         case EAST:
            return Math.ceil(minecraft.thePlayer.posX) - minecraft.thePlayer.posX;
         case SOUTH:
            return Math.ceil(minecraft.thePlayer.posZ) - minecraft.thePlayer.posZ;
         case WEST:
         default:
            return minecraft.thePlayer.posX - Math.floor(minecraft.thePlayer.posX);
      }
   }

   private float getMotionMultiplier() {
      int percent;
      if (!minecraft.thePlayer.onGround) {
         percent = this.airMotion.getValue();
      } else if (MovementUtils.speedPotionLevel() > 0) {
         percent = this.speedMotion.getValue();
      } else {
         percent = this.groundMotion.getValue();
      }

      return (float)percent / 100.0F;
   }

   private double randomEdgeApproachDistance() {
      return 0.2155 - RandomUtils.randomDouble(1.0E-4, 9.0E-4);
   }

   private float getMovementYaw() {
      return MovementUtils.movementYawForInput(minecraft.thePlayer.rotationYaw, (float)MovementUtils.forwardInput(), (float)MovementUtils.strafeInput());
   }

   private boolean isDiagonalYaw(float angle) {
      float remainder = Math.abs(angle % 90.0F);
      return remainder > 20.0F && remainder < 70.0F;
   }

   private boolean isTellyTakeoff() {
      if (!minecraft.thePlayer.onGround || !MovementUtils.hasMovementInput()) {
         return false;
      } else if (PlayerUtils.hasCollisionAbove()) {
         return false;
      } else {
         return this.keepY.getValue() == 3 && this.keepYState > 0 ? true : this.tower.getValue() == 3 && minecraft.gameSettings.keyBindJump.isKeyDown();
      }
   }

   public ScaffoldModule() {
      super("Scaffold", false, 0);
   }

   public int getPreviousHotbarSlot() {
      return this.previousHotbarSlot;
   }

   private void selectPlacementHotbarSlot() {
      ItemStack heldStack = minecraft.thePlayer.getHeldItem();
      int heldStackSize = ItemUtils.isPlaceableBlockStack(heldStack) ? heldStack.stackSize : 0;
      this.remainingStackBlocks = Math.min(this.remainingStackBlocks, heldStackSize);
      if (this.remainingStackBlocks <= 0) {
         int searchStartSlot = minecraft.thePlayer.inventory.currentItem;
         if (this.remainingStackBlocks == 0) {
            searchStartSlot--;
         }

         for (int slotCursor = searchStartSlot; slotCursor > searchStartSlot - 9; slotCursor--) {
            int hotbarSlot = (slotCursor % 9 + 9) % 9;
            ItemStack candidateStack = minecraft.thePlayer.inventory.getStackInSlot(hotbarSlot);
            if (ItemUtils.isPlaceableBlockStack(candidateStack)) {
               minecraft.thePlayer.inventory.currentItem = hotbarSlot;
               this.remainingStackBlocks = candidateStack.stackSize;
               return;
            }
         }
      }
   }

   private Vec3 findBestPlacementHit(BlockPlacementTarget placementTarget, float previousYaw, float previousPitch) {
      if (placementTarget == null) {
         return null;
      } else {
         double[] hitOffsetsX = FACE_SAMPLE_OFFSETS;
         double[] hitOffsetsY = FACE_SAMPLE_OFFSETS;
         double[] hitOffsetsZ = FACE_SAMPLE_OFFSETS;
         switch (placementTarget.face()) {
            case NORTH:
               hitOffsetsZ = new double[]{0.0};
               break;
            case EAST:
               hitOffsetsX = new double[]{1.0};
               break;
            case SOUTH:
               hitOffsetsZ = new double[]{1.0};
               break;
            case WEST:
               hitOffsetsX = new double[]{0.0};
               break;
            case DOWN:
               hitOffsetsY = new double[]{0.0};
               break;
            case UP:
               hitOffsetsY = new double[]{1.0};
         }

         float bestYaw = -180.0F;
         float bestPitch = 0.0F;
         float bestRotationCost = 0.0F;
         Vec3 bestHit = null;

         for (double hitOffsetX : hitOffsetsX) {
            for (double hitOffsetY : hitOffsetsY) {
               for (double hitOffsetZ : hitOffsetsZ) {
                  double deltaX = (double)placementTarget.position().getX() + hitOffsetX - minecraft.thePlayer.posX;
                  double deltaY = (double)placementTarget.position().getY()
                     + hitOffsetY
                     - minecraft.thePlayer.posY
                     - (double)minecraft.thePlayer.getEyeHeight();
                  double deltaZ = (double)placementTarget.position().getZ() + hitOffsetZ - minecraft.thePlayer.posZ;
                  float[] candidateRotations = RotationUtils.rotationsFromDelta(deltaX, deltaY, deltaZ);
                  candidateRotations[0] = RotationUtils.snapToMouseSensitivity(candidateRotations[0], previousYaw);
                  candidateRotations[1] = RotationUtils.snapToMouseSensitivity(candidateRotations[1], previousPitch);
                  candidateRotations[1] = MathHelper.clamp_float(candidateRotations[1], -90.0F, 90.0F);
                  MovingObjectPosition candidateHit = RotationUtils.traceBlocks(
                     candidateRotations[0], candidateRotations[1], (double)minecraft.playerController.getBlockReachDistance(), 1.0F
                  );
                  if (candidateHit != null
                     && candidateHit.typeOfHit == MovingObjectType.BLOCK
                     && candidateHit.getBlockPos().equals(placementTarget.position())
                     && candidateHit.sideHit == placementTarget.face()) {
                     float rotationCost = Math.abs(MathHelper.wrapAngleTo180_float(candidateRotations[0] - this.placementYaw))
                        + Math.abs(candidateRotations[1] - this.placementPitch);
                     if (bestYaw == -180.0F && bestPitch == 0.0F || rotationCost < bestRotationCost) {
                        bestYaw = candidateRotations[0];
                        bestPitch = candidateRotations[1];
                        bestRotationCost = rotationCost;
                        bestHit = candidateHit.hitVec;
                     }
                  }
               }
            }
         }

         if (bestYaw != -180.0F || bestPitch != 0.0F) {
            this.placementYaw = bestYaw;
            this.placementPitch = bestPitch;
            this.hasPlacementRotation = true;
         }

         return bestHit;
      }
   }

   private void applyPlacementRotation(
      RotationUpdateEvent event, BlockPlacementTarget placementTarget, float previousYaw, float previousPitch, float backwardYaw, float sidewaysYaw
   ) {
      if (this.hasPlacementRotation && MovementUtils.hasMovementInput()) {
         float backwardYawDifference = Math.abs(MathHelper.wrapAngleTo180_float(backwardYaw - this.placementYaw));
         if (backwardYawDifference < 90.0F) {
            switch (this.rotations.getValue()) {
               case 2:
                  float snappedBackwardYaw = RotationUtils.snapToMouseSensitivity(backwardYaw, previousYaw);
                  if (placementTarget != null
                     && this.tracePlacement(placementTarget.position(), placementTarget.face(), snappedBackwardYaw, this.placementPitch) != null) {
                     this.placementYaw = snappedBackwardYaw;
                  }
                  break;
               case 3:
                  float snappedSidewaysYaw = RotationUtils.snapToMouseSensitivity(sidewaysYaw, previousYaw);
                  if (placementTarget != null
                     && this.tracePlacement(placementTarget.position(), placementTarget.face(), snappedSidewaysYaw, this.placementPitch) != null) {
                     this.placementYaw = snappedSidewaysYaw;
                  }
            }
         }
      }

      if (this.rotations.getValue() != 0) {
         float requestedYaw = this.placementYaw;
         float requestedPitch = this.placementPitch;
         if (this.isTellyTakeoff()) {
            float playerYawDifference = MathHelper.wrapAngleTo180_float(minecraft.thePlayer.rotationYaw - event.getPreviousYaw());
            requestedYaw = RotationUtils.snapToMouseSensitivity(previousYaw + playerYawDifference * RandomUtils.randomFloat(0.98F, 0.99F), previousYaw);
            requestedPitch = RotationUtils.snapToMouseSensitivity(RandomUtils.randomFloat(30.0F, 80.0F), previousPitch);
            this.placementDelayTicks = 3;
            this.tellyRotationActive = true;
         } else {
            float placementYawDifference = MathHelper.wrapAngleTo180_float(this.placementYaw - event.getPreviousYaw());
            float maxYawChange = RandomUtils.randomFloat(80.0F, 90.0F);
            if (this.tellyRotationActive && minecraft.thePlayer.motionY > 0.0 && this.placementDelayTicks <= 1) {
               maxYawChange = RandomUtils.randomFloat(30.0F, 35.0F);
            }

            if (Math.abs(placementYawDifference) > maxYawChange) {
               float clampedYawChange = RotationUtils.clampAngleChange(placementYawDifference, maxYawChange);
               requestedYaw = RotationUtils.snapToMouseSensitivity(previousYaw + clampedYawChange, previousYaw);
               this.placementDelayTicks = Math.max(this.placementDelayTicks, 1);
            }
         }

         event.setRotation(requestedYaw, requestedPitch, 3);
         if (this.moveFix.getValue() == 1) {
            event.setMovementYaw(requestedYaw, 3);
         }
      }
   }

   private void placeCurrentAndAdditionalBlocks(BlockPlacementTarget placementTarget, Vec3 placementHit) {
      if (placementTarget != null && placementHit != null && this.placementDelayTicks <= 0) {
         this.placeBlock(placementTarget.position(), placementTarget.face());
         if (this.multiPlace.getValue()) {
            for (int extraPlacementIndex = 0; extraPlacementIndex < 3; extraPlacementIndex++) {
               placementTarget = this.findPlacementTarget();
               if (placementTarget == null) {
                  break;
               }

               MovingObjectPosition extraPlacementHit = RotationUtils.traceBlocks(
                  this.placementYaw, this.placementPitch, (double)minecraft.playerController.getBlockReachDistance(), 1.0F
               );
               if (extraPlacementHit != null
                  && extraPlacementHit.typeOfHit == MovingObjectType.BLOCK
                  && extraPlacementHit.getBlockPos().equals(placementTarget.position())
                  && extraPlacementHit.sideHit == placementTarget.face()) {
                  this.placeBlock(placementTarget.position(), placementTarget.face());
               } else {
                  Vec3 fallbackHit = BlockUtils.randomFacePoint(placementTarget.position(), placementTarget.face());
                  double fallbackDeltaX = fallbackHit.xCoord - minecraft.thePlayer.posX;
                  double fallbackDeltaY = fallbackHit.yCoord - minecraft.thePlayer.posY - (double)minecraft.thePlayer.getEyeHeight();
                  double fallbackDeltaZ = fallbackHit.zCoord - minecraft.thePlayer.posZ;
                  float[] fallbackRotations = RotationUtils.rotationsFromDelta(fallbackDeltaX, fallbackDeltaY, fallbackDeltaZ);
                  if (Math.abs(MathHelper.wrapAngleTo180_float(fallbackRotations[0] - this.placementYaw)) >= 120.0F
                     || Math.abs(fallbackRotations[1] - this.placementPitch) >= 60.0F) {
                     break;
                  }

                  extraPlacementHit = RotationUtils.traceBlocks(
                     fallbackRotations[0], fallbackRotations[1], (double)minecraft.playerController.getBlockReachDistance(), 1.0F
                  );
                  if (extraPlacementHit == null
                     || extraPlacementHit.typeOfHit != MovingObjectType.BLOCK
                     || !extraPlacementHit.getBlockPos().equals(placementTarget.position())
                     || extraPlacementHit.sideHit != placementTarget.face()) {
                     break;
                  }

                  this.placeBlock(placementTarget.position(), placementTarget.face());
               }
            }
         }
      }
   }

   private boolean handlePendingTowerPlacement() {
      if (this.pendingPlacementFace == null) {
         return false;
      } else {
         if (this.placementDelayTicks <= 0) {
            BlockPos beneathPlayer = new BlockPos(
               MathHelper.floor_double(minecraft.thePlayer.posX),
               MathHelper.floor_double(minecraft.thePlayer.posY) - 1,
               MathHelper.floor_double(minecraft.thePlayer.posZ)
            );
            this.placeBlock(beneathPlayer, this.pendingPlacementFace);
         }

         this.pendingPlacementFace = null;
         return true;
      }
   }

   private void handleExtraKeepYRecovery() {
      if (this.keepY.getValue() == 2 && this.keepYState > 0 && !minecraft.thePlayer.onGround) {
         int predictedBlockY = MathHelper.floor_double(minecraft.thePlayer.posY + minecraft.thePlayer.motionY);
         if (predictedBlockY <= this.keepYLevel && !(minecraft.thePlayer.posY <= (double)(this.keepYLevel + 1))) {
            this.keepYRecoveryPlacement = true;
            BlockPlacementTarget placementTarget = this.findPlacementTarget();
            if (placementTarget != null && this.placementDelayTicks <= 0) {
               this.placeBlock(placementTarget.position(), placementTarget.face());
            }
         }
      }
   }

   @EventHandler(
      priority = 50
   )
   public void onRotationUpdate(RotationUpdateEvent event) {
      if (this.isEnabled()) {
         if (this.placementDelayTicks > 0) {
            this.placementDelayTicks--;
         }

         if (minecraft.thePlayer.onGround) {
            if (this.keepYState > 0) {
               this.keepYState--;
            }

            if (this.keepYState < 0) {
               this.keepYState++;
            }

            if (this.keepYState == 0
               && this.keepY.getValue() != 0
               && (!this.keepYOnPress.getValue() || PlayerUtils.isUseItemKeyDown())
               && !minecraft.gameSettings.keyBindJump.isKeyDown()
               && !minecraft.thePlayer.isPotionActive(Potion.jump)) {
               this.keepYState = 1;
            }

            if (!this.keepYRecoveryPlacement) {
               this.keepYLevel = MathHelper.floor_double(minecraft.thePlayer.posY);
            }

            this.keepYRecoveryPlacement = false;
            this.tellyRotationActive = false;
         }

         if (this.canPlaceWithActiveModules()) {
            this.selectPlacementHotbarSlot();
            float previousYaw = event.getPreviousYaw();
            float previousPitch = event.getPreviousPitch();
            float movementYaw = this.getMovementYaw();
            float backwardYaw = movementYaw - 180.0F;
            float sidewaysYaw;
            if (this.isDiagonalYaw(movementYaw)) {
               sidewaysYaw = backwardYaw;
            } else {
               float sidewaysDirectionFactor = (movementYaw + 180.0F) % 90.0F < 45.0F ? 1.0F : -1.0F;
               sidewaysYaw = movementYaw - 135.0F * sidewaysDirectionFactor;
            }

            if (!this.hasPlacementRotation) {
               if (this.placementYaw == -180.0F && this.placementPitch == 0.0F) {
                  this.placementPitch = RotationUtils.snapToMouseSensitivity(RandomUtils.randomFloat(65.0F, 85.0F), previousPitch);
               }

               switch (this.rotations.getValue()) {
                  case 2:
                     this.placementYaw = RotationUtils.snapToMouseSensitivity(backwardYaw, previousYaw);
                     break;
                  case 3:
                  default:
                     this.placementYaw = RotationUtils.snapToMouseSensitivity(sidewaysYaw, previousYaw);
               }
            }

            BlockPlacementTarget placementTarget = this.findPlacementTarget();
            Vec3 placementHit = this.findBestPlacementHit(placementTarget, previousYaw, previousPitch);
            this.applyPlacementRotation(event, placementTarget, previousYaw, previousPitch, backwardYaw, sidewaysYaw);
            this.tickPlacementYaw = event.getYaw();
            this.tickPlacementPitch = event.getPitch();
            this.placeCurrentAndAdditionalBlocks(placementTarget, placementHit);
            if (!this.handlePendingTowerPlacement()) {
               this.handleExtraKeepYRecovery();
            }
         }
      }
   }

   @EventHandler
   public void onMoveFlying(MoveFlyingEvent event) {
      if (this.isEnabled()
         && !minecraft.thePlayer.isCollidedHorizontally
         && minecraft.thePlayer.hurtTime <= 5
         && !minecraft.thePlayer.isPotionActive(Potion.jump)
         && minecraft.gameSettings.keyBindJump.isKeyDown()
         && ItemUtils.isHoldingPlaceableBlock()) {
         int heightHundredths = (int)(minecraft.thePlayer.posY % 1.0 * 100.0);
         switch (this.tower.getValue()) {
            case 1:
               this.applyVanillaTowerMotion(heightHundredths, event);
               return;
            case 2:
               this.applyExtraTowerMotion(heightHundredths, event);
               return;
            default:
               this.towerPhase = 0;
               this.towerCycleCount = 0;
         }
      } else {
         this.towerPhase = 0;
         this.towerCycleCount = 0;
      }
   }

   @EventHandler
   public void onMovementInput(MovementInputEvent event) {
      if (this.isEnabled()) {
         if (this.moveFix.getValue() == 1 && RotationState.isActive() && RotationState.movementPriority() == 3.0F && MovementUtils.hasMovementInput()) {
            MovementUtils.correctMovementForYaw(RotationState.movementYaw());
         }

         if (minecraft.thePlayer.onGround && this.keepYState > 0 && MovementUtils.hasMovementInput()) {
            minecraft.thePlayer.movementInput.jump = true;
         }
      }
   }

   @EventHandler
   public void onLivingUpdate(LivingUpdateEvent event) {
      if (this.isEnabled()) {
         float motionMultiplier = this.getMotionMultiplier();
         if (motionMultiplier != 1.0F) {
            if (minecraft.thePlayer.movementInput.moveForward != 0.0F && minecraft.thePlayer.movementInput.moveStrafe != 0.0F) {
               float diagonalScale = 1.0F / (float)Math.sqrt(2.0);
               minecraft.thePlayer.movementInput.moveForward *= diagonalScale;
               minecraft.thePlayer.movementInput.moveStrafe *= diagonalScale;
            }

            minecraft.thePlayer.movementInput.moveForward *= motionMultiplier;
            minecraft.thePlayer.movementInput.moveStrafe *= motionMultiplier;
         }

         if (this.shouldDisableSprint()) {
            minecraft.thePlayer.setSprinting(false);
         }
      }
   }

   @EventHandler
   public void onSafeWalk(SafeWalkEvent event) {
      if (this.isEnabled() && this.safeWalk.getValue() && minecraft.thePlayer.onGround && !(minecraft.thePlayer.motionY > 0.0)) {
         if (PlayerUtils.isOffsetCollisionFree(minecraft.thePlayer.motionX, -1.0, minecraft.thePlayer.motionZ)) {
            event.setSafeWalk(true);
         }
      }
   }

   @EventHandler
   public void onRenderOverlay(RenderOverlayEvent event) {
      if (this.isEnabled() && this.blockCounter.getValue()) {
         int blockCount = 0;

         for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            ItemStack stack = minecraft.thePlayer.inventory.getStackInSlot(hotbarSlot);
            if (stack != null && stack.stackSize > 0 && stack.getItem() instanceof ItemBlock) {
               Block block = ((ItemBlock)stack.getItem()).getBlock();
               if (!BlockUtils.isInteractable(block) && BlockUtils.isSuitableFullBlock(block)) {
                  blockCount += stack.stackSize;
               }
            }
         }

         this.drawBlockCounter(blockCount);
      }
   }

   @EventHandler
   public void onLeftClick(LeftClickEvent event) {
      if (this.isEnabled()) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onRightClick(RightClickEvent event) {
      if (this.isEnabled()) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onBlockClick(BlockClickEvent event) {
      if (this.isEnabled()) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onHotbarChange(HotbarChangeEvent event) {
      if (this.isEnabled()) {
         this.previousHotbarSlot = event.getTargetSlot(this.previousHotbarSlot);
         event.setCancelled(true);
      }
   }

   @Override
   public void onEnable() {
      this.previousHotbarSlot = minecraft.thePlayer != null ? minecraft.thePlayer.inventory.currentItem : -1;
      this.remainingStackBlocks = -1;
      this.placementDelayTicks = 0;
      this.placementYaw = -180.0F;
      this.placementPitch = 0.0F;
      this.hasPlacementRotation = false;
      this.towerPhase = 0;
      this.towerCycleCount = 0;
      this.tellyRotationActive = false;
   }

   @Override
   public void onDisable() {
      if (minecraft.thePlayer != null && this.previousHotbarSlot != -1) {
         minecraft.thePlayer.inventory.currentItem = this.previousHotbarSlot;
      }
   }

   private boolean isKeepYOnPressVisible() {
      return this.keepY.getValue() != 0;
   }

   private static double targetDistanceSquared(BlockPos desiredPlacementBlock, BlockPos supportCandidate) {
      return supportCandidate.distanceSqToCenter(
         (double)desiredPlacementBlock.getX() + 0.5,
         (double)desiredPlacementBlock.getY() + 0.5,
         (double)desiredPlacementBlock.getZ() + 0.5
      );
   }

   private void applyVanillaTowerMotion(int heightHundredths, MoveFlyingEvent event) {
      switch (this.towerPhase) {
         case 0:
            if (minecraft.thePlayer.onGround) {
               this.towerPhase = 1;
               minecraft.thePlayer.motionY = -0.0784000015258789;
            }

            return;
         case 1:
            if (heightHundredths == 0 && PlayerUtils.hasCollisionBelow()) {
               this.keepYLevel = MathHelper.floor_double(minecraft.thePlayer.posY);
               this.towerPhase = 2;
               minecraft.thePlayer.motionY = 0.42F;
               if (MovementUtils.hasMovementInput()) {
                  MovementUtils.setHorizontalMotion(MovementUtils.horizontalSpeed(), MovementUtils.movementYaw());
               } else {
                  MovementUtils.setSpeedAlongMotion(0.0);
                  event.setForward(0.0F);
                  event.setStrafe(0.0F);
               }

               return;
            }

            this.towerPhase = 0;
            return;
         case 2:
            this.towerPhase = 3;
            minecraft.thePlayer.motionY = 0.75 - minecraft.thePlayer.posY % 1.0;
            return;
         case 3:
            this.towerPhase = 1;
            minecraft.thePlayer.motionY = 1.0 - minecraft.thePlayer.posY % 1.0;
            return;
         default:
            this.towerPhase = 0;
      }
   }

   private void applyExtraTowerMotion(int heightHundredths, MoveFlyingEvent event) {
      switch (this.towerPhase) {
         case 0:
            if (minecraft.thePlayer.onGround) {
               this.towerPhase = 1;
               minecraft.thePlayer.motionY = -0.0784000015258789;
            }

            return;
         case 1:
            if (heightHundredths == 0 && PlayerUtils.hasCollisionBelow()) {
               this.keepYLevel = MathHelper.floor_double(minecraft.thePlayer.posY);
               if (!MovementUtils.hasMovementInput()) {
                  this.towerCycleCount = 2;
                  MovementUtils.setSpeedAlongMotion(0.0);
                  event.setForward(0.0F);
                  event.setStrafe(0.0F);
                  EnumFacing towerFacing = this.facingFromYaw(MathHelper.wrapAngleTo180_float(this.placementYaw - 180.0F));
                  double edgeDistance = this.distanceToBlockEdge(towerFacing);
                  if (edgeDistance > 0.1) {
                     if (minecraft.thePlayer.onGround) {
                        Vec3i edgeDirection = towerFacing.getDirectionVec();
                        double approachDistance = Math.min(this.randomEdgeApproachDistance(), edgeDistance - 0.05);
                        double edgePadding = RandomUtils.randomDouble(0.02, 0.03);
                        AxisAlignedBB shiftedBounds = minecraft.thePlayer
                           .getEntityBoundingBox()
                           .offset(
                              (double)edgeDirection.getX() * (approachDistance - edgePadding),
                              0.0,
                              (double)edgeDirection.getZ() * (approachDistance - edgePadding)
                           );
                        if (minecraft.theWorld.getCollidingBoundingBoxes(minecraft.thePlayer, shiftedBounds).isEmpty()) {
                           minecraft.thePlayer.motionY = -0.0784000015258789;
                           minecraft.thePlayer
                              .setPosition(
                                 shiftedBounds.minX + (shiftedBounds.maxX - shiftedBounds.minX) / 2.0,
                                 shiftedBounds.minY,
                                 shiftedBounds.minZ + (shiftedBounds.maxZ - shiftedBounds.minZ) / 2.0
                              );
                        }

                        return;
                     }
                  } else {
                     this.towerPhase = 2;
                     this.pendingPlacementFace = towerFacing;
                     minecraft.thePlayer.motionY = 0.42F;
                  }

                  return;
               }

               this.towerPhase = 2;
               this.towerCycleCount++;
               minecraft.thePlayer.motionY = 0.42F;
               MovementUtils.setHorizontalMotion(MovementUtils.horizontalSpeed(), MovementUtils.movementYaw());
               return;
            }

            this.towerPhase = 0;
            this.towerCycleCount = 0;
            return;
         case 2:
            this.towerPhase = 3;
            minecraft.thePlayer.motionY = minecraft.thePlayer.motionY - RandomUtils.randomDouble(0.00101, 0.00109);
            return;
         case 3:
            if (this.towerCycleCount >= 4) {
               this.towerPhase = 4;
               this.towerCycleCount = 0;
            } else {
               this.towerPhase = 1;
               minecraft.thePlayer.motionY = 1.0 - minecraft.thePlayer.posY % 1.0;
            }

            return;
         case 4:
            this.towerPhase = 5;
            return;
         case 5:
            if (!PlayerUtils.hasCollisionBelow()) {
               this.towerPhase = 0;
            } else {
               this.towerPhase = 1;
               minecraft.thePlayer.motionY -= 0.08;
               minecraft.thePlayer.motionY *= 0.98F;
               minecraft.thePlayer.motionY -= 0.08;
               minecraft.thePlayer.motionY *= 0.98F;
            }

            return;
         default:
            this.towerPhase = 0;
            this.towerCycleCount = 0;
      }
   }

   private void drawBlockCounter(int blockCount) {
      HUDModule hud = (HUDModule)MyauClient.moduleManager.modules.get(HUDModule.class);
      float hudScale = hud.scale.getValue();
      GlStateManager.pushMatrix();
      GlStateManager.scale(hudScale, hudScale, 0.0F);
      GlStateManager.disableDepth();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      FontRenderer fontRenderer = minecraft.fontRendererObj;
      String counterText = String.format("%d block%s left", blockCount, blockCount != 1 ? "s" : "");
      ScaledResolution resolution = new ScaledResolution(minecraft);
      float textX = ((float)resolution.getScaledWidth() / 2.0F + (float)fontRenderer.FONT_HEIGHT * 1.5F) / hudScale;
      float textY = (float)resolution.getScaledHeight() / 2.0F / hudScale - (float)fontRenderer.FONT_HEIGHT / 2.0F + 1.0F;
      int textColor = blockCount > 0 ? 16777215 : 16733525;
      fontRenderer.drawString(counterText, textX, textY, textColor | -1090519040, hud.shadow.getValue());
      GlStateManager.disableBlend();
      GlStateManager.enableDepth();
      GlStateManager.popMatrix();
   }
}
