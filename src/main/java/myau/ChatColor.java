package myau;

public enum ChatColor {
   BLACK('0', -16777216),
   DARK_BLUE('1', -16777046),
   DARK_GREEN('2', -16733696),
   DARK_AQUA('3', -16733526),
   DARK_RED('4', -5636096),
   DARK_PURPLE('5', -5635926),
   GOLD('6', -22016),
   GRAY('7', -5592406),
   DARK_GRAY('8', -11184811),
   BLUE('9', -11184641),
   GREEN('a', -11141291),
   AQUA('b', -11141121),
   RED('c', -43691),
   LIGHT_PURPLE('d', -43521),
   YELLOW('e', -171),
   WHITE('f', -1),
   MAGIC('k', 0),
   BOLD('l', 0),
   STRIKETHROUGH('m', 0),
   UNDERLINE('n', 0),
   ITALIC('o', 0),
   RESET('r', 0);

   public static final char COLOR_CHAR = '§';
   private static final String VALID_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr";
   private final String formattingCode;
   private final int colorValue;

   private ChatColor(char code, int colorValue) {
      this.colorValue = colorValue;
      this.formattingCode = new String(new char[]{'§', code});
   }

   @Override
   public String toString() {
      return this.formattingCode;
   }

   public int getRgb() {
      return this.colorValue;
   }

   public static String formatText(String text) {
      char[] characters = text.toCharArray();

      for (int index = 0; index < characters.length - 1; index++) {
         if (characters[index] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(characters[index + 1]) >= 0) {
            characters[index] = 167;
            characters[index + 1] = Character.toLowerCase(characters[index + 1]);
         }
      }

      return new String(characters);
   }
}
