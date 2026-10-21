package myau;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

public class BedTrackerModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private final ScheduledExecutorService scannerExecutor = Executors.newScheduledThreadPool(1);
   private final LinkedHashMap<String, Long> lastAlertMillisByPlayer = new LinkedHashMap<>();
   private final LinkedHashSet<EntityEnderPearl> seenPearls = new LinkedHashSet<>();
   private final LinkedHashSet<String> ignoredTeammates = new LinkedHashSet<>();
   private final Color inactiveColor = new Color(ChatColor.WHITE.getRgb());
   private final Color farColor = new Color(ChatColor.RED.getRgb());
   private final Color warningColor = new Color(ChatColor.YELLOW.getRgb());
   private final Color safeColor = new Color(ChatColor.GREEN.getRgb());
   private BlockPos trackedBed = null;
   private long lastMacroMillis = -1L;
   private boolean waitingForSpawnTeleport = false;
   public final BooleanSetting alerts = new BooleanSetting("alerts", true);
   public final IntegerSetting alertsRange = new IntegerSetting("alerts-range", 48, 8, 128, this.alerts::getValue);
   public final BooleanSetting alertsOnPearl = new BooleanSetting("alerts-on-pearl", true);
   public final ModeSetting alertsSound = new ModeSetting("alerts-sound", 1, new String[]{"NONE", "MYAU", "ANVIL"}, this::isAlertsSoundVisible);
   public final IntegerSetting alertsFrequency = new IntegerSetting("alerts-frequency", 5, 1, 30, this::isAlertsFrequencyVisible);
   public final BooleanSetting macro = new BooleanSetting("macro", false);
   public final IntegerSetting macroRange = new IntegerSetting("macro-range", 24, 8, 128, this.macro::getValue);
   public final BooleanSetting macroOnPearl = new BooleanSetting("macro-on-pearl", false);
   public final StringSetting macroText = new StringSetting("macro-text", "/lobby", this::isMacroTextVisible);
   public final IntegerSetting macroDelay = new IntegerSetting("macro-delay", 1, 1, 10, this::isMacroDelayVisible);
   public final BooleanSetting hud = new BooleanSetting("hud", true);
   public final ModeSetting hudPositionX = new ModeSetting("hud-position-x", 0, new String[]{"LEFT", "MIDDLE", "RIGHT"}, this.hud::getValue);
   public final ModeSetting hudPositionY = new ModeSetting("hud-position-y", 0, new String[]{"TOP", "MIDDLE", "BOTTOM"}, this.hud::getValue);
   public final IntegerSetting hudOffsetX = new IntegerSetting("hud-offset-x", 2, 0, 255, this.hud::getValue);
   public final IntegerSetting hudOffsetY = new IntegerSetting("hud-offset-y", 2, 0, 255, this.hud::getValue);
   public final FloatSetting hudScale = new FloatSetting("hud-scale", 1.0F, 0.5F, 1.5F, this.hud::getValue);
   public final BooleanSetting hudShadow = new BooleanSetting("hud-shadow", true, this.hud::getValue);

   public BedTrackerModule() {
      super("BedTracker", false, 0, true);
   }

   private void playAlertSound() {
      switch (this.alertsSound.getValue()) {
         case 1:
            SoundUtils.play("mob.cat.meow");
            break;
         case 2:
            SoundUtils.play("random.anvil_land");
      }
   }

   private Color getDistanceColor(int distance) {
      if (distance < 0) {
         return this.inactiveColor;
      } else if (distance <= 100) {
         return this.safeColor;
      } else if (distance <= 114) {
         return ColorUtils.interpolate((float)(114 - distance) / 14.0F, this.warningColor, this.safeColor);
      } else {
         return distance > 128 ? this.farColor : ColorUtils.interpolate((float)(128 - distance) / 14.0F, this.farColor, this.warningColor);
      }
   }

   private boolean isBed(BlockPos position) {
      return position != null && minecraft.theWorld.getBlockState(position).getBlock() == Blocks.bed;
   }

   private boolean claimPlayerAlert(String playerName, long nowMillis) {
      Long previousAlertMillis = this.lastAlertMillisByPlayer.get(playerName);
      if (previousAlertMillis != null && previousAlertMillis + (long)this.alertsFrequency.getValue().intValue() * 1000L > nowMillis) {
         return false;
      } else {
         this.lastAlertMillisByPlayer.put(playerName, Long.valueOf(nowMillis));
         return true;
      }
   }

   private boolean claimMacro(long nowMillis) {
      if (this.lastMacroMillis + (long)this.macroDelay.getValue().intValue() * 1000L > nowMillis) {
         return false;
      } else {
         this.lastMacroMillis = nowMillis;
         return true;
      }
   }

   @EventHandler
   public void onPostTick(PostTickEvent event) {
      if (this.isEnabled() && this.isBed(this.trackedBed)) {
         long nowMillis = System.currentTimeMillis();
         boolean sentAlert = false;
         boolean runMacro = false;

         for (Entity entity : minecraft.theWorld.loadedEntityList) {
            if (entity instanceof EntityEnderPearl) {
               EntityEnderPearl pearl = (EntityEnderPearl)entity;
               if (this.seenPearls.add(pearl)) {
                  if (this.alertsOnPearl.getValue()) {
                     ChatUtils.addFormattedMessage(String.format("%s%s: &fDetected &5Ender Pearl&r &e&l⚠&r", "&7[&bMyau&7] &r", this.getName()));
                     sentAlert = true;
                  }

                  if (this.macroOnPearl.getValue() && this.claimMacro(nowMillis)) {
                     runMacro = true;
                  }
               }
            }
         }

         for (Entity entityx : minecraft.theWorld.loadedEntityList) {
            if (entityx instanceof EntityPlayer) {
               EntityPlayer player = (EntityPlayer)entityx;
               if (!EntityUtils.isBot(player) && !this.ignoredTeammates.contains(player.getName())) {
                  if (EntityUtils.isTeammate(player)) {
                     this.ignoredTeammates.add(player.getName());
                  } else {
                     double distance = player.getDistance(
                        (double)this.trackedBed.getX() + 0.5,
                        (double)this.trackedBed.getY() + 0.5,
                        (double)this.trackedBed.getZ() + 0.5
                     );
                     String playerName = player.getName();
                     String displayName = player.getDisplayName().getFormattedText();
                     ItemStack heldItem = player.getHeldItem();
                     boolean holdsEnderPearl = heldItem != null && heldItem.getItem() instanceof ItemEnderPearl;
                     if (this.alerts.getValue() && distance < (double)this.alertsRange.getValue().intValue() && this.claimPlayerAlert(playerName, nowMillis)) {
                        ChatUtils.addFormattedMessage(
                           String.format(
                              "%s%s: %s&r &fis %d blocks away from your bed &e&l⚠&r", "&7[&bMyau&7] &r", this.getName(), displayName, (int)distance + 1
                           )
                        );
                        sentAlert = true;
                     }

                     if (this.alertsOnPearl.getValue() && holdsEnderPearl && this.claimPlayerAlert(playerName, nowMillis)) {
                        ChatUtils.addFormattedMessage(String.format("%s%s: %s&r &fhas &5Ender Pearl&r &e&l⚠&r", "&7[&bMyau&7] &r", this.getName(), displayName));
                        sentAlert = true;
                     }

                     boolean macroThreat = this.macro.getValue() && distance < (double)this.macroRange.getValue().intValue()
                        || this.macroOnPearl.getValue() && holdsEnderPearl;
                     if (macroThreat && this.claimMacro(nowMillis)) {
                        runMacro = true;
                     }
                  }
               }
            }
         }

         if (sentAlert) {
            this.playAlertSound();
         }

         if (runMacro) {
            ChatUtils.addRawMessage(
               String.format(ChatColor.formatText("%s%s: &fRunning &6%s&r"), ChatColor.formatText("&7[&bMyau&7] &r"), this.getName(), this.macroText.getValue())
            );
            ChatUtils.sendMessage(this.macroText.getValue());
         }
      }
   }

   @EventHandler(
      priority = -50
   )
   public void onRenderOverlay(RenderOverlayEvent event) {
      if (this.isEnabled()
         && this.hud.getValue()
         && minecraft.theWorld != null
         && minecraft.thePlayer != null
         && !minecraft.gameSettings.showDebugInfo
         && (minecraft.currentScreen == null || minecraft.currentScreen instanceof GuiChat)) {
         boolean hasBed = this.isBed(this.trackedBed);
         int distance = 0;
         if (hasBed) {
            double deltaX = minecraft.thePlayer.posX - (double)this.trackedBed.getX();
            double deltaZ = minecraft.thePlayer.posZ - (double)this.trackedBed.getZ();
            distance = (int)Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) + 1;
         }

         String status = hasBed ? "&atrue&r" : "&cfalse&r";
         String distanceText = "";
         if (hasBed) {
            String warning = distance >= 128 ? " &c&l⚠&r" : "";
            distanceText = String.format(" &7| &fDistance: &r%d%s", distance, warning);
         }

         String message = ChatColor.formatText(String.format("&fBed: %s%s", status, distanceText));
         ScaledResolution resolution = new ScaledResolution(minecraft);
         float textWidth = (float)minecraft.fontRendererObj.getStringWidth(message);
         float textHeight = (float)minecraft.fontRendererObj.FONT_HEIGHT - 1.0F;
         float scale = this.hudScale.getValue();
         float x = (float)this.hudOffsetX.getValue().intValue() / scale;
         float y = (float)this.hudOffsetY.getValue().intValue() / scale;
         switch (this.hudPositionX.getValue()) {
            case 0:
               x++;
               break;
            case 1:
               x += (float)resolution.getScaledWidth() / scale / 2.0F - textWidth / 2.0F;
               break;
            case 2:
               x = (float)resolution.getScaledWidth() / scale - textWidth - x - 1.0F;
         }

         switch (this.hudPositionY.getValue()) {
            case 0:
               y++;
               break;
            case 1:
               y += (float)resolution.getScaledHeight() / scale / 2.0F - textHeight / 2.0F;
               break;
            case 2:
               y = (float)resolution.getScaledHeight() / scale - textHeight - y - 1.0F;
         }

         GlStateManager.pushMatrix();
         GlStateManager.scale(scale, scale, 1.0F);
         GlStateManager.translate(x, y, 0.0F);
         GlStateManager.disableDepth();
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(770, 771);
         minecraft.fontRendererObj.drawString(message, 0.0F, 0.0F, this.getDistanceColor(distance).getRGB(), this.hudShadow.getValue());
         GlStateManager.disableBlend();
         GlStateManager.enableDepth();
         GlStateManager.popMatrix();
      }
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      this.waitingForSpawnTeleport = false;
   }

   @EventHandler
   public void onPacketReceive(PacketReceiveEvent event) {
      if (this.isEnabled()) {
         if (event.getPacket() instanceof S02PacketChat) {
            String message = ((S02PacketChat)event.getPacket()).getChatComponent().getFormattedText();
            if (isBedWarsStartMessage(message)) {
               this.lastAlertMillisByPlayer.clear();
               this.seenPearls.clear();
               this.ignoredTeammates.clear();
               this.trackedBed = null;
               this.waitingForSpawnTeleport = true;
            }
         }

         if (event.getPacket() instanceof S08PacketPlayerPosLook && this.waitingForSpawnTeleport) {
            this.waitingForSpawnTeleport = false;
            this.scannerExecutor.schedule(this::scanForNearestBed, 3L, TimeUnit.SECONDS);
         }
      }
   }

   private static boolean isBedWarsStartMessage(String message) {
      return message.contains("§e§lProtect your bed and destroy the enemy bed")
         || message.contains("§e§lDestroy the enemy bed and then eliminate them")
         || message.contains("§e§lEvery few seconds brings a new surprise");
   }

   private void scanForNearestBed() {
      if (minecraft.theWorld != null && minecraft.thePlayer != null) {
         int playerX = MathHelper.floor_double(minecraft.thePlayer.posX);
         int playerY = MathHelper.floor_double(minecraft.thePlayer.posY + (double)minecraft.thePlayer.getEyeHeight());
         int playerZ = MathHelper.floor_double(minecraft.thePlayer.posZ);
         BlockPos nearestBed = null;
         int nearestDistance = -1;

         for (int x = playerX - 25; x <= playerX + 25; x++) {
            for (int y = playerY - 25; y <= playerY + 25; y++) {
               for (int z = playerZ - 25; z <= playerZ + 25; z++) {
                  BlockPos candidate = new BlockPos(x, y, z);
                  if (this.isBed(candidate)) {
                     int distance = Math.abs(x - playerX) + Math.abs(y - playerY) + Math.abs(z - playerZ);
                     if (nearestDistance < 0 || nearestDistance > distance) {
                        nearestBed = candidate;
                        nearestDistance = distance;
                     }
                  }
               }
            }
         }

         if (nearestBed != null) {
            this.trackedBed = nearestBed;
            ChatUtils.addFormattedMessage(
               String.format(
                  "%s%s: &fWhitelisted your bed at (%d, %d, %d) &a&l✔&r",
                  "&7[&bMyau&7] &r",
                  this.getName(),
                  nearestBed.getX(),
                  nearestBed.getY(),
                  nearestBed.getZ()
               )
            );
            SoundUtils.play("note.pling");
         }
      }
   }

   @Override
   public void onDisable() {
      this.lastAlertMillisByPlayer.clear();
      this.seenPearls.clear();
      this.ignoredTeammates.clear();
      this.trackedBed = null;
   }

   private boolean isMacroDelayVisible() {
      return this.macro.getValue() || this.macroOnPearl.getValue();
   }

   private boolean isMacroTextVisible() {
      return this.macro.getValue() || this.macroOnPearl.getValue();
   }

   private boolean isAlertsFrequencyVisible() {
      return this.alerts.getValue() || this.alertsOnPearl.getValue();
   }

   private boolean isAlertsSoundVisible() {
      return this.alerts.getValue() || this.alertsOnPearl.getValue();
   }
}
