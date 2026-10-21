package myau;

import myau.ui.ClickGui;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class GuiModule extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private ClickGui clickGui;

    public GuiModule() {
        super("ClickGui", false, Keyboard.KEY_RSHIFT, true);
    }

    @Override
    public void onEnable() {
        this.setEnabled(false);
        if (clickGui == null) {
            clickGui = new ClickGui();
        }
        mc.displayGuiScreen(clickGui);
    }
}
