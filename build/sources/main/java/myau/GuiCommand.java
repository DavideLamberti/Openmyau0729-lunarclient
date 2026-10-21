package myau;

import java.util.ArrayList;
import java.util.Arrays;

public class GuiCommand extends Command {
    public GuiCommand() {
        super(new ArrayList<>(Arrays.asList("gui", "clickgui")));
    }

    @Override
    public void execute(ArrayList<String> arguments) {
        Module guiModule = MyauClient.moduleManager.getModule(GuiModule.class);
        if (guiModule != null) {
            guiModule.setEnabled(true);
        }
    }

    @Override
    public ArrayList<String> completeArguments(ArrayList<String> arguments) {
        return new ArrayList<>();
    }
}
