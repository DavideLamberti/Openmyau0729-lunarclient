package myau.ui;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import myau.*;
import myau.ui.components.CategoryComponent;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

public class ClickGui extends GuiScreen {
    private static final int LAYOUT_VERSION = 2;
    private static ClickGui instance;
    private final File configFile = new File("./config/Myau/", "clickgui.txt");
    private final ArrayList<CategoryComponent> categoryList;
    private boolean positionsLoaded;

    public ClickGui() {
        instance = this;

        List<Module> combatModules = new ArrayList<>();
        combatModules.add(MyauClient.moduleManager.getModule(AimAssistModule.class));
        combatModules.add(MyauClient.moduleManager.getModule(AutoClickerModule.class));
        combatModules.add(MyauClient.moduleManager.getModule(KillAuraModule.class));
        combatModules.add(MyauClient.moduleManager.getModule(WTapModule.class));
        combatModules.add(MyauClient.moduleManager.getModule(VelocityModule.class));
        combatModules.add(MyauClient.moduleManager.getModule(ReachModule.class));
        combatModules.add(MyauClient.moduleManager.getModule(TargetStrafeModule.class));
        combatModules.add(MyauClient.moduleManager.getModule(NoHitDelayModule.class));
        combatModules.add(MyauClient.moduleManager.getModule(AntiFireballModule.class));
        combatModules.add(MyauClient.moduleManager.getModule(LagRangeModule.class));
        combatModules.add(MyauClient.moduleManager.getModule(HitSelectModule.class));

        List<Module> movementModules = new ArrayList<>();
        movementModules.add(MyauClient.moduleManager.getModule(AntiVoidModule.class));
        movementModules.add(MyauClient.moduleManager.getModule(FlyModule.class));
        movementModules.add(MyauClient.moduleManager.getModule(SpeedModule.class));
        movementModules.add(MyauClient.moduleManager.getModule(LongJumpModule.class));
        movementModules.add(MyauClient.moduleManager.getModule(SprintModule.class));
        movementModules.add(MyauClient.moduleManager.getModule(SafeWalkModule.class));
        movementModules.add(MyauClient.moduleManager.getModule(JesusModule.class));
        movementModules.add(MyauClient.moduleManager.getModule(BlinkModule.class));
        movementModules.add(MyauClient.moduleManager.getModule(NoFallModule.class));
        movementModules.add(MyauClient.moduleManager.getModule(NoSlowModule.class));
        movementModules.add(MyauClient.moduleManager.getModule(KeepSprintModule.class));
        movementModules.add(MyauClient.moduleManager.getModule(EagleModule.class));
        movementModules.add(MyauClient.moduleManager.getModule(NoJumpDelayModule.class));

        List<Module> renderModules = new ArrayList<>();
        renderModules.add(MyauClient.moduleManager.getModule(ESPModule.class));
        renderModules.add(MyauClient.moduleManager.getModule(ChamsModule.class));
        renderModules.add(MyauClient.moduleManager.getModule(FullbrightModule.class));
        renderModules.add(MyauClient.moduleManager.getModule(TracersModule.class));
        renderModules.add(MyauClient.moduleManager.getModule(NameTagsModule.class));
        renderModules.add(MyauClient.moduleManager.getModule(XrayModule.class));
        renderModules.add(MyauClient.moduleManager.getModule(TargetHUDModule.class));
        renderModules.add(MyauClient.moduleManager.getModule(IndicatorsModule.class));
        renderModules.add(MyauClient.moduleManager.getModule(BedESPModule.class));
        renderModules.add(MyauClient.moduleManager.getModule(ItemESPModule.class));
        renderModules.add(MyauClient.moduleManager.getModule(ViewClipModule.class));
        renderModules.add(MyauClient.moduleManager.getModule(NoHurtCamModule.class));
        renderModules.add(MyauClient.moduleManager.getModule(HUDModule.class));
        renderModules.add(MyauClient.moduleManager.getModule(GuiModule.class));
        renderModules.add(MyauClient.moduleManager.getModule(ChestESPModule.class));
        renderModules.add(MyauClient.moduleManager.getModule(TrajectoriesModule.class));

        List<Module> playerModules = new ArrayList<>();
        playerModules.add(MyauClient.moduleManager.getModule(AutoHealModule.class));
        playerModules.add(MyauClient.moduleManager.getModule(AutoToolModule.class));
        playerModules.add(MyauClient.moduleManager.getModule(ChestStealerModule.class));
        playerModules.add(MyauClient.moduleManager.getModule(InvManagerModule.class));
        playerModules.add(MyauClient.moduleManager.getModule(InvWalkModule.class));
        playerModules.add(MyauClient.moduleManager.getModule(ScaffoldModule.class));
        playerModules.add(MyauClient.moduleManager.getModule(SpeedMineModule.class));
        playerModules.add(MyauClient.moduleManager.getModule(FastPlaceModule.class));
        playerModules.add(MyauClient.moduleManager.getModule(GhostHandModule.class));
        playerModules.add(MyauClient.moduleManager.getModule(MCFModule.class));
        playerModules.add(MyauClient.moduleManager.getModule(AntiDebuffModule.class));

        List<Module> miscModules = new ArrayList<>();
        miscModules.add(MyauClient.moduleManager.getModule(SpammerModule.class));
        miscModules.add(MyauClient.moduleManager.getModule(BedNukerModule.class));
        miscModules.add(MyauClient.moduleManager.getModule(BedTrackerModule.class));
        miscModules.add(MyauClient.moduleManager.getModule(LightningTrackerModule.class));
        miscModules.add(MyauClient.moduleManager.getModule(NoRotateModule.class));
        miscModules.add(MyauClient.moduleManager.getModule(NickHiderModule.class));
        miscModules.add(MyauClient.moduleManager.getModule(AntiObbyTrapModule.class));
        miscModules.add(MyauClient.moduleManager.getModule(AntiObfuscateModule.class));
        miscModules.add(MyauClient.moduleManager.getModule(TimerModule.class));

        Comparator<Module> comparator = Comparator.comparing(m -> m.getName().toLowerCase());
        combatModules.removeIf(m -> m == null);
        movementModules.removeIf(m -> m == null);
        renderModules.removeIf(m -> m == null);
        playerModules.removeIf(m -> m == null);
        miscModules.removeIf(m -> m == null);

        combatModules.sort(comparator);
        movementModules.sort(comparator);
        renderModules.sort(comparator);
        playerModules.sort(comparator);
        miscModules.sort(comparator);

        Set<Module> registered = new HashSet<>();
        registered.addAll(combatModules);
        registered.addAll(movementModules);
        registered.addAll(renderModules);
        registered.addAll(playerModules);
        registered.addAll(miscModules);

        for (Module module : MyauClient.moduleManager.modules.values()) {
            if (!registered.contains(module)) {
                miscModules.add(module);
            }
        }
        miscModules.sort(comparator);

        this.categoryList = new ArrayList<>();
        int topOffset = 5;

        CategoryComponent combat = new CategoryComponent("Combat", combatModules);
        combat.setY(topOffset);
        categoryList.add(combat);
        topOffset += 20;

        CategoryComponent movement = new CategoryComponent("Movement", movementModules);
        movement.setY(topOffset);
        categoryList.add(movement);
        topOffset += 20;

        CategoryComponent render = new CategoryComponent("Render", renderModules);
        render.setY(topOffset);
        categoryList.add(render);
        topOffset += 20;

        CategoryComponent player = new CategoryComponent("Player", playerModules);
        player.setY(topOffset);
        categoryList.add(player);
        topOffset += 20;

        CategoryComponent misc = new CategoryComponent("Misc", miscModules);
        misc.setY(topOffset);
        categoryList.add(misc);

        loadPositions();
    }

    public static ClickGui getInstance() {
        return instance;
    }

    @Override
    public void initGui() {
        super.initGui();
        if (!positionsLoaded) {
            layoutDefaultCategories();
        }
    }

    @Override
    public void drawScreen(int x, int y, float p) {
        // A translucent native base keeps the Minecraft world visible behind the
        // frosted controls, while still preserving text contrast.
        drawRect(0, 0, this.width, this.height, 0x990A111B);
        UiRender.drawVerticalGradient(0, 0, this.width, this.height, 0x86142638, 0x7D080C15);
        UiRender.drawCircle(this.width - 35.0F, 18.0F, 75.0F, 0x167BB6FF);
        UiRender.drawCircle(15.0F, this.height - 12.0F, 55.0F, 0x127D6EFF);
        drawBranding();

        for (CategoryComponent category : categoryList) {
            category.handleDrag(x, y);
            category.render(this.fontRendererObj);

            for (Component module : category.getModules()) {
                module.update(x, y);
            }
        }

        int wheel = Mouse.getDWheel();
        if (wheel != 0) {
            int scrollDir = wheel > 0 ? 1 : -1;
            for (CategoryComponent category : categoryList) {
                category.onScroll(x, y, scrollDir);
            }
        }
    }

    @Override
    public void mouseClicked(int x, int y, int mouseButton) {
        for (CategoryComponent category : categoryList) {
            if (mouseButton == 0 && category.isHeaderHovered(x, y)) {
                if (category.isPinHovered(x, y)) {
                    category.setPin(!category.isPin());
                } else if (category.isCollapseHovered(x, y)) {
                    category.setOpened(!category.isOpened());
                } else {
                    category.beginDrag(x, y);
                }
                continue;
            }

            if (category.isOpened() && !category.getModules().isEmpty()) {
                for (Component c : category.getModules()) {
                    c.mouseDown(x, y, mouseButton);
                }
            }
        }
    }

    @Override
    public void mouseReleased(int x, int y, int mouseButton) {
        for (CategoryComponent category : categoryList) {
            if (mouseButton == 0) {
                category.mousePressed(false);
            }
            if (category.isOpened()) {
                for (Component component : category.getModules()) {
                    component.mouseReleased(x, y, mouseButton);
                }
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int key) {
        if (key == 1) {
            this.mc.displayGuiScreen(null);
        } else {
            Iterator<CategoryComponent> btnCat = categoryList.iterator();
            while (true) {
                CategoryComponent cat;
                do {
                    do {
                        if (!btnCat.hasNext()) {
                            return;
                        }
                        cat = btnCat.next();
                    } while (!cat.isOpened());
                } while (cat.getModules().isEmpty());

                for (Component component : cat.getModules()) {
                    component.keyTyped(typedChar, key);
                }
            }
        }
    }

    @Override
    public void onGuiClosed() {
        savePositions();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void savePositions() {
        JsonObject json = new JsonObject();
        json.addProperty("layoutVersion", LAYOUT_VERSION);
        for (CategoryComponent cat : categoryList) {
            JsonObject pos = new JsonObject();
            pos.addProperty("x", cat.getX());
            pos.addProperty("y", cat.getY());
            pos.addProperty("open", cat.isOpened());
            json.add(cat.getName(), pos);
        }
        if (configFile.getParentFile() != null && !configFile.getParentFile().exists()) {
            configFile.getParentFile().mkdirs();
        }
        try (FileWriter writer = new FileWriter(configFile)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPositions() {
        if (!configFile.exists()) return;
        try (FileReader reader = new FileReader(configFile)) {
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            if (!json.has("layoutVersion") || json.get("layoutVersion").getAsInt() != LAYOUT_VERSION) {
                return;
            }
            for (CategoryComponent cat : categoryList) {
                if (json.has(cat.getName())) {
                    JsonObject pos = json.getAsJsonObject(cat.getName());
                    cat.setX(pos.get("x").getAsInt());
                    cat.setY(pos.get("y").getAsInt());
                    cat.setOpened(pos.get("open").getAsBoolean());
                    positionsLoaded = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void layoutDefaultCategories() {
        int gap = 10;
        int panelWidth = categoryList.isEmpty() ? 0 : categoryList.get(0).getWidth();
        int availableColumns = panelWidth == 0 ? 3 : Math.max(3, (this.width - 16 + gap) / (panelWidth + gap));
        int columns = Math.min(categoryList.size(), availableColumns);
        int totalWidth = columns * panelWidth + (columns - 1) * gap;
        int startX = Math.max(8, (this.width - totalWidth) / 2);
        int topY = 27;

        for (int index = 0; index < categoryList.size(); ++index) {
            CategoryComponent category = categoryList.get(index);
            if (index < columns) {
                category.setLocation(startX + index * (panelWidth + gap), topY);
            } else {
                int dockY = Math.max(topY + 33, this.height - 28);
                category.setLocation(startX + (index - columns) * (panelWidth + gap), dockY);
            }
        }
    }

    private void drawBranding() {
        String title = "MYAU";
        int titleWidth = this.fontRendererObj.getStringWidth(title);
        int cardWidth = titleWidth + 42;
        int cardX = (this.width - cardWidth) / 2;
        UiRender.drawRoundedRect(cardX, 6, cardX + cardWidth, 20, 6.0F, 0xB7141820);
        UiRender.drawRoundedOutline(cardX, 6, cardX + cardWidth, 20, 6.0F, 0.7F, 0x55485366);
        UiRender.drawCircle(cardX + 9.0F, 13.0F, 3.0F, UiRender.ACCENT);
        this.fontRendererObj.drawString(title, cardX + 16, 9, UiRender.TEXT, false);
        this.fontRendererObj.drawString(MyauClient.VERSION, cardX + titleWidth + 21, 9, UiRender.TEXT_MUTED, false);
    }
}
