package myau;

public class WindowClickEvent extends CancellableEvent {
   private final int windowId;
   private final int slotId;
   private final int mouseButton;
   private final int clickMode;

   public WindowClickEvent(int windowId, int slotId, int mouseButton, int clickMode) {
      this.windowId = windowId;
      this.slotId = slotId;
      this.mouseButton = mouseButton;
      this.clickMode = clickMode;
   }
}
