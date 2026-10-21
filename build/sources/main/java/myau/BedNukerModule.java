package myau;

import com.google.common.base.CaseFormat;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import myau.mixin.AccessorPlayerControllerMP;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockBed.EnumPartType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

public class BedNukerModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private final ScheduledExecutorService bedScanExecutor = Executors.newScheduledThreadPool(1);
   private final ElapsedTimer bedBreakTimer = new ElapsedTimer();
   private final Color lowProgressColor = new Color(ChatColor.RED.getRgb());
   private final Color midProgressColor = new Color(ChatColor.YELLOW.getRgb());
   private final Color highProgressColor = new Color(ChatColor.GREEN.getRgb());
   private BlockPos targetBlock = null;
   private BlockPos whitelistedBed = null;
   private int breakPhase = 0;
   private int breakTicks = 0;
   private float accumulatedBreakDamage = 0.0F;
   private boolean targetIsBed = false;
   private int previousHotbarSlot = -1;
   private boolean interactionLocked = false;
   private boolean diggingInProgress = false;
   private boolean awaitingSpawnTeleport = false;
   public final ModeSetting mode = new ModeSetting("mode", 0, new String[]{"LEGIT", "SWAP"});
   public final FloatSetting range = new FloatSetting("range", 4.5F, 3.0F, 6.0F);
   public final PercentSetting speed = new PercentSetting("speed", 0);
   public final BooleanSetting groundSpoof = new BooleanSetting("ground-spoof", false);
   public final ModeSetting ignoreVelocity = new ModeSetting("ignore-velocity", 0, new String[]{"NONE", "CANCEL", "DELAY"});
   public final BooleanSetting surroundings = new BooleanSetting("surroundings", true);
   public final BooleanSetting toolCheck = new BooleanSetting("tool-check", true);
   public final BooleanSetting whitelist = new BooleanSetting("whitelist", true);
   public final BooleanSetting swing = new BooleanSetting("swing", true);
   public final ModeSetting moveFix = new ModeSetting("move-fix", 1, new String[]{"NONE", "SILENT", "STRICT"});
   public final ModeSetting showTarget = new ModeSetting("show-target", 1, new String[]{"NONE", "DEFAULT", "HUD"});
   public final ModeSetting showProgress = new ModeSetting("show-progress", 1, new String[]{"NONE", "DEFAULT", "HUD"});
   private static final long BED_SCAN_COOLDOWN_MILLIS = 500L;

   private void resetBreakState() {
      if (this.targetBlock != null) {
         minecraft.theWorld.sendBlockBreakProgress(minecraft.thePlayer.getEntityId(), this.targetBlock, -1);
      }

      this.targetBlock = null;
      this.breakPhase = 0;
      this.breakTicks = 0;
      this.accumulatedBreakDamage = 0.0F;
      this.targetIsBed = false;
      this.interactionLocked = false;
      this.diggingInProgress = false;
   }

   private float getBreakProgress() {
      if (this.targetBlock == null) {
         return 0.0F;
      } else {
         float breakDamage = this.accumulatedBreakDamage;
         if (this.groundSpoof.getValue()) {
            IBlockState state = minecraft.theWorld.getBlockState(this.targetBlock);
            int bestToolSlot = ItemUtils.findBestToolForBlock(minecraft.thePlayer.inventory.currentItem, state.getBlock());
            breakDamage = (float)this.breakTicks * this.getRelativeBlockHardness(state, this.targetBlock, bestToolSlot, true);
         }

         float completionThreshold = 1.0F - 0.3F * ((float)this.speed.getValue().intValue() / 100.0F);
         return Math.min(1.0F, breakDamage / completionThreshold);
      }
   }

   private void restoreHotbarSlot() {
      if (this.previousHotbarSlot != -1) {
         minecraft.thePlayer.inventory.currentItem = this.previousHotbarSlot;
         this.syncHeldItem();
         this.previousHotbarSlot = -1;
      }
   }

   private void syncHeldItem() {
      AccessorPlayerControllerMP controller = (AccessorPlayerControllerMP)minecraft.playerController;
      if (controller.getCurrentPlayerItem() != minecraft.thePlayer.inventory.currentItem) {
         minecraft.thePlayer.stopUsingItem();
      }

      controller.callSyncCurrentPlayItem();
   }

   private boolean hasRequiredTool(Block block) {
      Material material = block.getMaterial();
      if (material != Material.iron && material != Material.anvil && material != Material.rock) {
         return true;
      } else {
         for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = minecraft.thePlayer.inventory.getStackInSlot(slot);
            if (stack != null && stack.getItem() instanceof ItemPickaxe) {
               return true;
            }
         }

         return false;
      }
   }

   private EnumFacing findBreakFace(BlockPos position) {
      double deltaX = (double)position.getX() + 0.5 - minecraft.thePlayer.posX;
      double deltaY = (double)position.getY() + 0.25 - minecraft.thePlayer.posY - (double)minecraft.thePlayer.getEyeHeight();
      double deltaZ = (double)position.getZ() + 0.5 - minecraft.thePlayer.posZ;
      float[] rotations = RotationUtils.rotationsFromDelta(deltaX, deltaY, deltaZ);
      MovingObjectPosition hit = RotationUtils.traceBlocks(rotations[0], rotations[1], 8.0, 1.0F);
      return hit == null ? EnumFacing.UP : hit.sideHit;
   }

   private float getDigSpeedForSlot(IBlockState blockState, int slot, boolean onGround) {
      ItemStack stack = minecraft.thePlayer.inventory.getStackInSlot(slot);
      float digSpeed = stack == null ? 1.0F : stack.getItem().getDigSpeed(stack, blockState);
      if (digSpeed > 1.0F && stack != null) {
         int efficiencyLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack);
         if (efficiencyLevel > 0) {
            digSpeed += (float)(efficiencyLevel * efficiencyLevel + 1);
         }
      }

      if (minecraft.thePlayer.isPotionActive(Potion.digSpeed)) {
         int amplifier = minecraft.thePlayer.getActivePotionEffect(Potion.digSpeed).getAmplifier();
         digSpeed *= 1.0F + (float)(amplifier + 1) * 0.2F;
      }

      if (minecraft.thePlayer.isPotionActive(Potion.digSlowdown)) {
         switch (minecraft.thePlayer.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) {
            case 0:
               digSpeed *= 0.3F;
               break;
            case 1:
               digSpeed *= 0.09F;
               break;
            case 2:
               digSpeed *= 0.0027F;
               break;
            default:
               digSpeed *= 8.1E-4F;
         }
      }

      if (minecraft.thePlayer.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(minecraft.thePlayer)) {
         digSpeed /= 5.0F;
      }

      if (!onGround) {
         digSpeed /= 5.0F;
      }

      return digSpeed;
   }

   private boolean canHarvestBlock(Block block, int slot) {
      if (block.getMaterial().isToolNotRequired()) {
         return true;
      } else {
         ItemStack stack = minecraft.thePlayer.inventory.getStackInSlot(slot);
         return stack != null && stack.canHarvestBlock(block);
      }
   }

   private float getRelativeBlockHardness(IBlockState blockState, BlockPos position, int slot, boolean onGround) {
      Block block = blockState.getBlock();
      float hardness = block.getBlockHardness(minecraft.theWorld, position);
      if (hardness < 0.0F) {
         return 0.0F;
      } else {
         float divisor = this.canHarvestBlock(block, slot) ? 30.0F : 100.0F;
         return this.getDigSpeedForSlot(blockState, slot, onGround) / hardness / divisor;
      }
   }

   private float getBestBreakSpeed(BlockPos position) {
      IBlockState state = minecraft.theWorld.getBlockState(position);
      int bestToolSlot = ItemUtils.findBestToolForBlock(minecraft.thePlayer.inventory.currentItem, state.getBlock());
      return this.getRelativeBlockHardness(state, position, bestToolSlot, minecraft.thePlayer.onGround);
   }

   private BlockPos findSurroundingBlock(BlockPos bedPosition) {
      IBlockState bedState = minecraft.theWorld.getBlockState(bedPosition);
      if (!(bedState.getBlock() instanceof BlockBed)) {
         return null;
      } else {
         EnumPartType part = (EnumPartType)bedState.getValue(BlockBed.PART);
         EnumFacing facing = (EnumFacing)bedState.getValue(BlockBed.FACING);
         BlockPos otherHalf = bedPosition.offset(part == EnumPartType.HEAD ? facing.getOpposite() : facing);
         BlockPos[] bedHalves = new BlockPos[]{bedPosition, otherHalf};
         ArrayList<BlockPos> surroundingBlocks = new ArrayList<>();
         EnumFacing[] surroundingFaces = new EnumFacing[]{EnumFacing.UP, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};

         for (BlockPos bedHalf : bedHalves) {
            for (EnumFacing surroundingFace : surroundingFaces) {
               BlockPos surroundingPosition = bedHalf.offset(surroundingFace);
               Block surroundingBlock = minecraft.theWorld.getBlockState(surroundingPosition).getBlock();
               if (BlockUtils.isReplaceable(surroundingBlock)) {
                  return null;
               }

               if (!(surroundingBlock instanceof BlockBed)) {
                  surroundingBlocks.add(surroundingPosition);
               }
            }
         }

         if (surroundingBlocks.isEmpty()) {
            return null;
         } else {
            surroundingBlocks.sort(this::compareSurroundingBlocks);
            return surroundingBlocks.get(0);
         }
      }
   }

   private BlockPos findTargetBlock() {
      return this.findTargetBlockNear(
         minecraft.thePlayer.posX,
         minecraft.thePlayer.posY + (double)minecraft.thePlayer.getEyeHeight(),
         minecraft.thePlayer.posZ
      );
   }

   private BlockPos findTargetBlockNear(double originX, double originY, double originZ) {
      ArrayList<BlockPos> beds = new ArrayList<>();
      int blockX = MathHelper.floor_double(originX);
      int blockY = MathHelper.floor_double(originY);
      int blockZ = MathHelper.floor_double(originZ);

      for (int x = blockX - 6; x <= blockX + 6; x++) {
         for (int y = blockY - 6; y <= blockY + 6; y++) {
            for (int z = blockZ - 6; z <= blockZ + 6; z++) {
               BlockPos position = new BlockPos(x, y, z);
               if ((!this.whitelist.getValue() || this.whitelistedBed == null || !(this.whitelistedBed.distanceSq(position) < 10.0))
                  && minecraft.theWorld.getBlockState(position).getBlock() instanceof BlockBed
                  && PlayerUtils.isBlockWithinReachFrom(position, originX, originY, originZ, this.range.getValue().doubleValue())) {
                  beds.add(position);
               }
            }
         }
      }

      if (beds.isEmpty()) {
         return null;
      } else {
         beds.sort(Comparator.comparingDouble(BedNukerModule::distanceFromEyesSquared));

         for (BlockPos bed : beds) {
            if (!this.surroundings.getValue()) {
               return bed;
            }

            BlockPos surroundingBlock = this.findSurroundingBlock(bed);
            if (surroundingBlock == null) {
               return bed;
            }

            if (!this.toolCheck.getValue() || this.hasRequiredTool(minecraft.theWorld.getBlockState(surroundingBlock).getBlock())) {
               return surroundingBlock;
            }
         }

         return null;
      }
   }

   private void swingOrSendAnimation() {
      if (this.swing.getValue()) {
         minecraft.thePlayer.swingItem();
      } else {
         PacketUtils.send(new C0APacketAnimation());
      }
   }

   private Color getProgressColor(int displayMode) {
      switch (displayMode) {
         case 1:
            float progress = this.getBreakProgress();
            if (progress <= 0.5F) {
               return ColorUtils.interpolate(progress / 0.5F, this.lowProgressColor, this.midProgressColor);
            }

            return ColorUtils.interpolate((progress - 0.5F) / 0.5F, this.midProgressColor, this.highProgressColor);
         case 2:
            return ((HUDModule)MyauClient.moduleManager.modules.get(HUDModule.class)).getColor(System.currentTimeMillis());
         default:
            return new Color(-1);
      }
   }

   public BedNukerModule() {
      super("BedNuker", false, 0);
   }

   public boolean isControllingInteractions() {
      return this.targetBlock != null && this.interactionLocked;
   }

   public boolean isDigging() {
      return this.targetBlock != null && this.diggingInProgress;
   }

   @EventHandler(
      priority = 50
   )
   public void onPreTick(PreTickEvent event) {
      if (this.isEnabled()) {
         if (this.targetBlock != null) {
            boolean invalidTarget = minecraft.theWorld.isAirBlock(this.targetBlock)
               || !PlayerUtils.isBlockWithinReach(this.targetBlock, this.range.getValue().doubleValue());
            if (!invalidTarget && !this.targetIsBed) {
               BlockPos newTarget = this.findTargetBlock();
               invalidTarget = newTarget != null && minecraft.theWorld.getBlockState(newTarget).getBlock() instanceof BlockBed;
            }

            if (invalidTarget) {
               this.restoreHotbarSlot();
               this.resetBreakState();
            }
         }

         if (this.targetBlock != null) {
            IBlockState blockState = minecraft.theWorld.getBlockState(this.targetBlock);
            int toolSlot = ItemUtils.findBestToolForBlock(minecraft.thePlayer.inventory.currentItem, blockState.getBlock());
            if (this.mode.getValue() == 0 && this.previousHotbarSlot == -1) {
               this.previousHotbarSlot = minecraft.thePlayer.inventory.currentItem;
               minecraft.thePlayer.inventory.currentItem = toolSlot;
               this.syncHeldItem();
            }

            switch (this.breakPhase) {
               case 0:
                  if (!minecraft.thePlayer.isUsingItem()) {
                     this.swingOrSendAnimation();
                     PacketUtils.send(new C07PacketPlayerDigging(Action.START_DESTROY_BLOCK, this.targetBlock, this.findBreakFace(this.targetBlock)));
                     this.swingOrSendAnimation();
                     minecraft.effectRenderer.addBlockHitEffects(this.targetBlock, this.findBreakFace(this.targetBlock));
                     this.breakPhase = 1;
                  }
                  break;
               case 1:
                  if (this.mode.getValue() != 0) {
                     this.interactionLocked = false;
                  }

                  this.diggingInProgress = true;
                  this.breakTicks++;
                  this.accumulatedBreakDamage = this.accumulatedBreakDamage
                     + this.getRelativeBlockHardness(blockState, this.targetBlock, toolSlot, minecraft.thePlayer.onGround);
                  boolean effectiveGround = minecraft.thePlayer.onGround || this.groundSpoof.getValue();
                  float projectedDamage = (float)this.breakTicks * this.getRelativeBlockHardness(blockState, this.targetBlock, toolSlot, effectiveGround);
                  minecraft.effectRenderer.addBlockHitEffects(this.targetBlock, this.findBreakFace(this.targetBlock));
                  float threshold = 1.0F - 0.3F * ((float)this.speed.getValue().intValue() / 100.0F);
                  if (!(this.accumulatedBreakDamage >= threshold) && !(projectedDamage >= threshold)) {
                     break;
                  }

                  if (this.mode.getValue() != 0) {
                     this.interactionLocked = true;
                     this.previousHotbarSlot = minecraft.thePlayer.inventory.currentItem;
                     minecraft.thePlayer.inventory.currentItem = toolSlot;
                     this.syncHeldItem();
                     if (minecraft.thePlayer.isUsingItem()) {
                        this.previousHotbarSlot = minecraft.thePlayer.inventory.currentItem;
                        minecraft.thePlayer.inventory.currentItem = (minecraft.thePlayer.inventory.currentItem + 1) % 9;
                        this.syncHeldItem();
                     }
                  }

                  this.diggingInProgress = false;
                  PacketUtils.send(new C07PacketPlayerDigging(Action.STOP_DESTROY_BLOCK, this.targetBlock, this.findBreakFace(this.targetBlock)));
                  this.swingOrSendAnimation();
                  IBlockState currentState = minecraft.theWorld.getBlockState(this.targetBlock);
                  Block currentBlock = currentState.getBlock();
                  if (currentBlock.getMaterial() != Material.air) {
                     minecraft.theWorld.playAuxSFX(2001, this.targetBlock, Block.getStateId(currentState));
                     minecraft.theWorld.setBlockToAir(this.targetBlock);
                  }

                  if (currentBlock instanceof BlockBed) {
                     this.bedBreakTimer.reset();
                  }

                  this.breakPhase = 2;
                  break;
               case 2:
                  this.restoreHotbarSlot();
                  this.resetBreakState();
                  break;
               default:
                  this.resetBreakState();
            }

            if (this.targetBlock != null) {
               return;
            }
         }

         if (minecraft.thePlayer.capabilities.allowEdit && this.bedBreakTimer.hasElapsed(500L)) {
            this.targetBlock = this.findTargetBlock();
            this.breakPhase = 0;
            this.breakTicks = 0;
            this.accumulatedBreakDamage = 0.0F;
            this.targetIsBed = this.targetBlock != null && minecraft.theWorld.getBlockState(this.targetBlock).getBlock() instanceof BlockBed;
            this.restoreHotbarSlot();
            if (this.targetBlock != null) {
               this.interactionLocked = true;
            }
         }

         if (this.targetBlock == null) {
            MyauClient.incomingPacketBuffer.setEnabled(false, IncomingBufferOwner.BED_NUKER);
         }
      }
   }

   @EventHandler
   public void onRotationUpdate(RotationUpdateEvent event) {
      if (this.isEnabled() && this.isControllingInteractions()) {
         double deltaX = (double)this.targetBlock.getX() + 0.5 - minecraft.thePlayer.posX;
         double deltaY = (double)this.targetBlock.getY()
            + 0.5
            - minecraft.thePlayer.posY
            - (double)minecraft.thePlayer.getEyeHeight();
         double deltaZ = (double)this.targetBlock.getZ() + 0.5 - minecraft.thePlayer.posZ;
         float previousYaw = event.getPreviousYaw();
         float previousPitch = event.getPreviousPitch();
         float[] rotation = RotationUtils.rotationsFromDelta(deltaX, deltaY, deltaZ);
         rotation[0] = RotationUtils.snapToMouseSensitivity(rotation[0], previousYaw);
         rotation[1] = RotationUtils.snapToMouseSensitivity(rotation[1], previousPitch);
         rotation[1] = MathHelper.clamp_float(rotation[1], -90.0F, 90.0F);
         event.setRotation(rotation[0], rotation[1], 5);
         if (this.moveFix.getValue() != 0) {
            event.setMovementYaw(rotation[0], 5);
         }
      }
   }

   @EventHandler
   public void onPreWalkingUpdate(PreWalkingUpdateEvent event) {
      if (this.isEnabled() && this.isDigging()) {
         if (!MyauClient.packetActionTracker.sentUseEntity
            && !MyauClient.packetActionTracker.sentDigging
            && !MyauClient.packetActionTracker.sentBlockPlacement
            && !MyauClient.packetActionTracker.sentAnimation) {
            this.swingOrSendAnimation();
         }
      }
   }

   @EventHandler
   public void onMovementInput(MovementInputEvent event) {
      if (this.isEnabled() && this.moveFix.getValue() != 0) {
         if (RotationState.isActive() && RotationState.movementPriority() == 5.0F && MovementUtils.hasMovementInput()) {
            MovementUtils.correctMovementForYaw(RotationState.movementYaw());
         }
      }
   }

   @EventHandler(
      priority = 50
   )
   public void onVelocity(VelocityEvent event) {
      if (this.isEnabled() && !event.isCancelled() && !(event.getVelocityY() <= 0.0)) {
         if (this.ignoreVelocity.getValue() == 1 && this.targetBlock != null) {
            event.setCancelled(true);
            event.setVelocityX(minecraft.thePlayer.motionX);
            event.setVelocityY(minecraft.thePlayer.motionY);
            event.setVelocityZ(minecraft.thePlayer.motionZ);
         }
      }
   }

   @EventHandler
   public void onRenderOverlay(RenderOverlayEvent event) {
      if (this.isEnabled() && this.targetBlock != null) {
         if (!this.targetIsBed || !this.surroundings.getValue()) {
            if (this.showProgress.getValue() != 0) {
               HUDModule hud = (HUDModule)MyauClient.moduleManager.modules.get(HUDModule.class);
               float scale = hud.scale.getValue();
               String message = String.format("%d%%", (int)(this.getBreakProgress() * 100.0F));
               GlStateManager.pushMatrix();
               GlStateManager.scale(scale, scale, 0.0F);
               GlStateManager.disableDepth();
               GlStateManager.enableBlend();
               GlStateManager.blendFunc(770, 771);
               int textWidth = minecraft.fontRendererObj.getStringWidth(message);
               ScaledResolution resolution = new ScaledResolution(minecraft);
               minecraft.fontRendererObj
                  .drawString(
                     message,
                     (float)resolution.getScaledWidth() / 2.0F / scale - (float)textWidth / 2.0F,
                     (float)resolution.getScaledHeight() / 5.0F * 2.0F / scale,
                     this.getProgressColor(this.showProgress.getValue()).getRGB() & 16777215 | -1090519040,
                     hud.shadow.getValue()
                  );
               GlStateManager.disableBlend();
               GlStateManager.enableDepth();
               GlStateManager.popMatrix();
            }
         }
      }
   }

   @EventHandler(
      priority = -50
   )
   public void onRenderWorld(RenderWorldEvent event) {
      if (this.isEnabled() && this.targetBlock != null && !minecraft.theWorld.isAirBlock(this.targetBlock)) {
         minecraft.theWorld.sendBlockBreakProgress(minecraft.thePlayer.getEntityId(), this.targetBlock, (int)(this.getBreakProgress() * 10.0F) - 1);
         if (this.showTarget.getValue() != 0) {
            BedESPModule bedESP = (BedESPModule)MyauClient.moduleManager.modules.get(BedESPModule.class);
            Color color = this.getProgressColor(this.showTarget.getValue());
            double height = this.targetIsBed ? bedESP.getBedHeight() : 1.0;
            RenderUtils.beginOverlay();
            RenderUtils.fillBlockBounds(this.targetBlock, height, color.getRed(), color.getGreen(), color.getBlue(), 63);
            RenderUtils.endOverlay();
         }
      }
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      this.awaitingSpawnTeleport = false;
   }

   @EventHandler
   public void onPacketReceive(PacketReceiveEvent event) {
      if (!event.isCancelled()) {
         if (event.getPacket() instanceof S02PacketChat) {
            String message = ((S02PacketChat)event.getPacket()).getChatComponent().getFormattedText();
            if (message.contains("§e§lProtect your bed and destroy the enemy bed")
               || message.contains("§e§lDestroy the enemy bed and then eliminate them")
               || message.contains("§e§lEvery few seconds brings a new surprise")) {
               this.awaitingSpawnTeleport = true;
            }
         }

         if (event.getPacket() instanceof S08PacketPlayerPosLook && this.awaitingSpawnTeleport) {
            this.awaitingSpawnTeleport = false;
            this.whitelistedBed = null;
            this.bedScanExecutor.schedule(this::scanNearbyBedForWhitelist, 1L, TimeUnit.SECONDS);
         }

         if (this.isEnabled()
            && this.targetBlock != null
            && this.ignoreVelocity.getValue() == 2
            && MyauClient.incomingPacketBuffer.owner() != IncomingBufferOwner.BED_NUKER) {
            if (event.getPacket() instanceof S12PacketEntityVelocity) {
               S12PacketEntityVelocity velocityPacket = (S12PacketEntityVelocity)event.getPacket();
               if (velocityPacket.getEntityID() == minecraft.thePlayer.getEntityId() && velocityPacket.getMotionY() > 0) {
                  MyauClient.incomingPacketBuffer.setOwner(IncomingBufferOwner.BED_NUKER);
                  MyauClient.incomingPacketBuffer.queuedPackets.offer(velocityPacket);
                  event.setCancelled(true);
               }
            } else {
               if (event.getPacket() instanceof S27PacketExplosion) {
                  S27PacketExplosion explosionPacket = (S27PacketExplosion)event.getPacket();
                  if (explosionPacket.func_149149_c() != 0.0F || explosionPacket.func_149144_d() != 0.0F || explosionPacket.func_149147_e() != 0.0F) {
                     MyauClient.incomingPacketBuffer.setOwner(IncomingBufferOwner.BED_NUKER);
                     MyauClient.incomingPacketBuffer.queuedPackets.offer(explosionPacket);
                     event.setCancelled(true);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onLeftClick(LeftClickEvent event) {
      if (this.isEnabled()) {
         if (this.isControllingInteractions()) {
            event.setCancelled(true);
         } else {
            MovingObjectPosition hit = minecraft.objectMouseOver;
            if (this.targetBlock != null && hit != null && hit.typeOfHit == MovingObjectType.BLOCK) {
               event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler
   public void onRightClick(RightClickEvent event) {
      if (this.isEnabled() && this.isControllingInteractions()) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onBlockClick(BlockClickEvent event) {
      if (this.isEnabled()) {
         if (this.isControllingInteractions()) {
            event.setCancelled(true);
         } else {
            MovingObjectPosition hit = minecraft.objectMouseOver;
            if (this.targetBlock != null && hit != null && hit.typeOfHit == MovingObjectType.BLOCK) {
               event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler
   public void onHotbarChange(HotbarChangeEvent event) {
      if (this.isEnabled() && this.previousHotbarSlot != -1) {
         this.previousHotbarSlot = event.getTargetSlot(this.previousHotbarSlot);
         event.setCancelled(true);
      }
   }

   @Override
   public void onDisable() {
      this.resetBreakState();
      this.previousHotbarSlot = -1;
      MyauClient.incomingPacketBuffer.setEnabled(false, IncomingBufferOwner.BED_NUKER);
   }

   @Override
   public String[] getHudInfo() {
      return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeName())};
   }

   private void scanNearbyBedForWhitelist() {
      if (minecraft.thePlayer != null && minecraft.theWorld != null) {
         int centerX = MathHelper.floor_double(minecraft.thePlayer.posX);
         int centerY = MathHelper.floor_double(minecraft.thePlayer.posY + (double)minecraft.thePlayer.getEyeHeight());
         int centerZ = MathHelper.floor_double(minecraft.thePlayer.posZ);
         BlockPos closestBed = null;
         int closestDistance = Integer.MAX_VALUE;

         for (int x = centerX - 25; x <= centerX + 25; x++) {
            for (int y = centerY - 25; y <= centerY + 25; y++) {
               for (int z = centerZ - 25; z <= centerZ + 25; z++) {
                  BlockPos position = new BlockPos(x, y, z);
                  if (minecraft.theWorld.getBlockState(position).getBlock() instanceof BlockBed) {
                     int distance = Math.abs(x - centerX) + Math.abs(y - centerY) + Math.abs(z - centerZ);
                     if (distance < closestDistance) {
                        closestBed = position;
                        closestDistance = distance;
                     }
                  }
               }
            }
         }

         if (closestBed != null) {
            this.whitelistedBed = closestBed;
         }
      }
   }

   private static double distanceFromEyesSquared(BlockPos blockPos) {
      return blockPos.distanceSqToCenter(
         minecraft.thePlayer.posX,
         minecraft.thePlayer.posY + (double)minecraft.thePlayer.getEyeHeight(),
         minecraft.thePlayer.posZ
      );
   }

   private int compareSurroundingBlocks(BlockPos first, BlockPos second) {
      int speedComparison = Float.compare(this.getBestBreakSpeed(second), this.getBestBreakSpeed(first));
      return speedComparison != 0 ? speedComparison : Double.compare(distanceFromEyesSquared(first), distanceFromEyesSquared(second));
   }
}
