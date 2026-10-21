package myau;

import java.awt.Color;
import java.io.File;

public class FriendList extends PlayerNameList {
   public FriendList() {
      super(configurationFile(), new Color(ChatColor.DARK_GREEN.getRgb()));
   }

   private static File configurationFile() {
      return new File("./config/Myau/", "friends.txt");
   }
}
