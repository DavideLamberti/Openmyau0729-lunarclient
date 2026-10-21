package myau;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import myau.mixin.AccessorRenderManager;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;

public class ItemESPModule extends Module {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   public final PercentSetting opacity = new PercentSetting("opacity", 25);
   public final BooleanSetting outline = new BooleanSetting("outline", false);
   public final BooleanSetting itemCount = new BooleanSetting("item-count", true);
   public final BooleanSetting autoScale = new BooleanSetting("auto-scale", true);
   public final BooleanSetting emeralds = new BooleanSetting("emeralds", true);
   public final BooleanSetting diamonds = new BooleanSetting("diamonds", true);
   public final BooleanSetting gold = new BooleanSetting("gold", true);
   public final BooleanSetting iron = new BooleanSetting("iron", true);

   public ItemESPModule() {
      super("ItemESP", false, 0);
   }

   private boolean shouldShowItem(int id) {
      if (this.emeralds.getValue() && this.isEmerald(id)) {
         return true;
      } else if (this.diamonds.getValue() && this.isDiamond(id)) {
         return true;
      } else {
         return this.gold.getValue() && this.isGold(id) ? true : this.iron.getValue() && this.isIron(id);
      }
   }

   private boolean isEmerald(int id) {
      Item item = Item.getItemById(id);
      Block block = Block.getBlockFromItem(item);
      return item == Items.emerald || block == Blocks.emerald_block || block == Blocks.emerald_ore;
   }

   private boolean isDiamond(int id) {
      Item item = Item.getItemById(id);
      Block block = Block.getBlockFromItem(item);
      return item == Items.diamond
         || item == Items.diamond_sword
         || item == Items.diamond_pickaxe
         || item == Items.diamond_shovel
         || item == Items.diamond_axe
         || item == Items.diamond_hoe
         || item == Items.diamond_helmet
         || item == Items.diamond_chestplate
         || item == Items.diamond_leggings
         || item == Items.diamond_boots
         || block == Blocks.diamond_block
         || block == Blocks.diamond_ore;
   }

   private boolean isGold(int id) {
      Item item = Item.getItemById(id);
      Block block = Block.getBlockFromItem(item);
      return item == Items.gold_ingot
         || item == Items.gold_nugget
         || item == Items.golden_apple
         || block == Blocks.gold_block
         || block == Blocks.gold_ore;
   }

   private boolean isIron(int id) {
      Item item = Item.getItemById(id);
      Block block = Block.getBlockFromItem(item);
      return item == Items.iron_ingot || block == Blocks.iron_block || block == Blocks.iron_ore;
   }

   private Color colorFor(int id) {
      if (this.isEmerald(id)) {
         return new Color(ChatColor.GREEN.getRgb());
      } else if (this.isDiamond(id)) {
         return new Color(ChatColor.AQUA.getRgb());
      } else {
         return this.isGold(id) ? new Color(ChatColor.YELLOW.getRgb()) : new Color(this.isIron(id) ? ChatColor.WHITE.getRgb() : ChatColor.GRAY.getRgb());
      }
   }

   private int sortPriority(int id) {
      if (this.isEmerald(id)) {
         return 4;
      } else if (this.isDiamond(id)) {
         return 3;
      } else if (this.isGold(id)) {
         return 2;
      } else {
         return this.isIron(id) ? 1 : 0;
      }
   }

   private int compareItemEntries(Entry<DroppedItemGroup, Integer> first, Entry<DroppedItemGroup, Integer> second) {
      int firstPriority = this.sortPriority(first.getKey().itemId);
      int secondPriority = this.sortPriority(second.getKey().itemId);
      return Integer.compare(firstPriority, secondPriority);
   }

   @EventHandler
   public void onRenderWorld(RenderWorldEvent event) {
      if (this.isEnabled()) {
         LinkedHashMap<DroppedItemGroup, Integer> counts = new LinkedHashMap<>();

         for (Entity entity : EntityUtils.entitiesBackToFront()) {
            if (entity.ticksExisted >= 3 && (entity.ignoreFrustumCheck || RenderUtils.isEntityInFrustum(entity, 0.125)) && entity instanceof EntityItem) {
               EntityItem droppedItem = (EntityItem)entity;
               ItemStack stack = droppedItem.getEntityItem();
               if (stack.stackSize > 0) {
                  int itemId = Item.getIdFromItem(stack.getItem());
                  if (this.shouldShowItem(itemId)) {
                     double worldX = MathUtils.lerpDouble(droppedItem.lastTickPosX, droppedItem.posX, (double)event.getPartialTicks());
                     double worldY = MathUtils.lerpDouble(droppedItem.lastTickPosY, droppedItem.posY, (double)event.getPartialTicks());
                     double worldZ = MathUtils.lerpDouble(droppedItem.lastTickPosZ, droppedItem.posZ, (double)event.getPartialTicks());
                     DroppedItemGroup group = new DroppedItemGroup(itemId, worldX, worldY, worldZ);
                     Integer previousCount = counts.get(group);
                     counts.put(group, Integer.valueOf(stack.stackSize + (previousCount == null ? 0 : previousCount)));
                  }
               }
            }
         }

         for (Entry<DroppedItemGroup, Integer> entry : counts.entrySet().stream().sorted(this::compareItemEntries).collect(Collectors.toList())) {
            DroppedItemGroup group = entry.getKey();
            Color tint = this.colorFor(group.itemId);
            double x = group.x - ((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosX();
            double y = group.y - ((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosY();
            double z = group.z - ((AccessorRenderManager)minecraft.getRenderManager()).getRenderPosZ();
            double distance = minecraft.getRenderViewEntity().getDistance(group.x, group.y, group.z);
            double size = 0.5 + 0.375 * ((Math.max(6.0, this.autoScale.getValue() ? distance : 6.0) - 6.0) / 28.0);
            AxisAlignedBB bounds = new AxisAlignedBB(x - size * 0.5, y, z - size * 0.5, x + size * 0.5, y + size, z + size * 0.5);
            RenderUtils.beginOverlay();
            if (this.opacity.getValue() > 0) {
               RenderUtils.fillBounds(
                  bounds, tint.getRed(), tint.getGreen(), tint.getBlue(), (int)((float)this.opacity.getValue().intValue() / 100.0F * 255.0F)
               );
               GlStateManager.resetColor();
            }

            if (this.outline.getValue()) {
               RenderUtils.outlineBounds(bounds, tint.getRed(), tint.getGreen(), tint.getBlue(), 255, 1.5F);
               GlStateManager.resetColor();
            }

            RenderUtils.endOverlay();
            if (this.itemCount.getValue()) {
               GlStateManager.pushMatrix();
               GlStateManager.translate(x, y + size * 0.5, z);
               GlStateManager.rotate(minecraft.getRenderManager().playerViewY * -1.0F, 0.0F, 1.0F, 0.0F);
               float var10001 = minecraft.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F;
               GlStateManager.rotate(minecraft.getRenderManager().playerViewX, var10001, 0.0F, 0.0F);
               double textScale = -0.04375 - 0.0328125 * ((Math.max(6.0, this.autoScale.getValue() ? distance : 6.0) - 6.0) / 28.0);
               GlStateManager.scale(textScale, textScale, 1.0);
               GlStateManager.disableDepth();
               String label = String.format("%d", entry.getValue());
               RenderUtils.drawOutlinedText(
                  label,
                  ((float)minecraft.fontRendererObj.getStringWidth(label) / 2.0F - 0.5F) * -1.0F,
                  ((float)(minecraft.fontRendererObj.FONT_HEIGHT / 2) - 0.5F) * -1.0F,
                  tint.getRGB()
               );
               GlStateManager.enableDepth();
               GlStateManager.resetColor();
               GlStateManager.popMatrix();
            }
         }
      }
   }
}
