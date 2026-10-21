package myau;

import com.google.common.base.CaseFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition;
import net.minecraft.util.AxisAlignedBB;

public class AntiVoidModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private boolean overVoid = false;
   private boolean previouslyOverVoid = false;
   private double[] lastSafePosition = null;
   public final ModeSetting mode = new ModeSetting("mode", 0, new String[]{"BLINK"});
   public final FloatSetting distance = new FloatSetting("distance", 5.0F, 0.0F, 16.0F);

   public AntiVoidModule() {
      super("AntiVoid", false, 0);
   }

   private void releaseBufferAndClearSafePosition() {
      MyauClient.outgoingPacketBuffer.setEnabled(false, OutgoingBufferOwner.ANTI_VOID);
      this.lastSafePosition = null;
   }

   private boolean canStartBuffering() {
      LongJumpModule longJump = (LongJumpModule)MyauClient.moduleManager.modules.get(LongJumpModule.class);
      return !longJump.hasJumpStarted();
   }

   @EventHandler
   public void onPreWalkingUpdate(PreWalkingUpdateEvent event) {
      if (this.isEnabled()) {
         this.overVoid = !minecraft.thePlayer.capabilities.allowFlying && PlayerUtils.isOverVoid();
         if (this.mode.getValue() == 0) {
            if (!this.overVoid) {
               this.releaseBufferAndClearSafePosition();
            }

            if (this.lastSafePosition != null) {
               float halfWidth = minecraft.thePlayer.width / 2.0F;
               float playerHeight = minecraft.thePlayer.height;
               AxisAlignedBB savedBounds = new AxisAlignedBB(
                  this.lastSafePosition[0] - (double)halfWidth,
                  this.lastSafePosition[1],
                  this.lastSafePosition[2] - (double)halfWidth,
                  this.lastSafePosition[0] + (double)halfWidth,
                  this.lastSafePosition[1] + (double)playerHeight,
                  this.lastSafePosition[2] + (double)halfWidth
               );
               if (PlayerUtils.isBoundsOverVoid(savedBounds)) {
                  this.releaseBufferAndClearSafePosition();
               }
            }

            if (!this.previouslyOverVoid && this.overVoid && this.canStartBuffering()) {
               MyauClient.outgoingPacketBuffer.setEnabled(false, OutgoingBufferOwner.AUTO_BLOCK);
               if (MyauClient.outgoingPacketBuffer.setEnabled(true, OutgoingBufferOwner.ANTI_VOID)) {
                  this.lastSafePosition = new double[]{
                     minecraft.thePlayer.prevPosX, minecraft.thePlayer.prevPosY, minecraft.thePlayer.prevPosZ
                  };
               }
            }

            if (MyauClient.outgoingPacketBuffer.owner() == OutgoingBufferOwner.ANTI_VOID
               && this.lastSafePosition != null
               && this.lastSafePosition[1] - (double)this.distance.getValue().floatValue() > minecraft.thePlayer.posY) {
               MyauClient.outgoingPacketBuffer
                  .queuedPackets
                  .offerFirst(
                     new C04PacketPlayerPosition(
                        this.lastSafePosition[0], this.lastSafePosition[1] - RandomUtils.randomDouble(10.0, 20.0), this.lastSafePosition[2], false
                     )
                  );
               this.releaseBufferAndClearSafePosition();
            }
         }

         this.previouslyOverVoid = this.overVoid;
      }
   }

   @EventHandler
   public void onKeyPress(KeyPressEvent event) {
      if (event.getKeyCode() == minecraft.gameSettings.keyBindUseItem.getKeyCode()) {
         ItemStack selectedStack = minecraft.thePlayer.inventory.getCurrentItem();
         if (selectedStack != null && selectedStack.getItem() instanceof ItemEnderPearl) {
            this.releaseBufferAndClearSafePosition();
         }
      }
   }

   @Override
   public void onEnable() {
      this.overVoid = false;
      this.previouslyOverVoid = false;
      this.releaseBufferAndClearSafePosition();
   }

   @Override
   public void onDisable() {
      MyauClient.outgoingPacketBuffer.setEnabled(false, OutgoingBufferOwner.ANTI_VOID);
   }

   @Override
   public void onSettingChanged(String settingName) {
      if (this.isEnabled()) {
         this.onDisable();
      }
   }

   @Override
   public String[] getHudInfo() {
      return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeName())};
   }
}
