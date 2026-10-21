package myau;

import java.util.ArrayList;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;

public final class ScoreboardUtils {
   private static final Minecraft minecraft = Minecraft.getMinecraft();

   private ScoreboardUtils() {
   }

   public static ArrayList<String> sidebarLines() {
      if (minecraft.theWorld == null) {
         return new ArrayList<>();
      } else {
         Scoreboard scoreboard = minecraft.theWorld.getScoreboard();
         ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
         return objective == null
            ? new ArrayList<>()
            : scoreboard.getSortedScores(objective)
               .stream()
               .map(score -> formatScoreboardLine(scoreboard, score))
               .collect(Collectors.toCollection(ArrayList::new));
      }
   }

   public static boolean isHypixelScoreboard() {
      ArrayList<String> lines = sidebarLines();
      if (lines.isEmpty()) {
         return false;
      } else {
         String firstLine = lines.get(0);
         return firstLine.equals("§ewww.hypixel.ne\ud83c\udf82§et") || firstLine.equals("§ewww.hypixel.ne§g§et");
      }
   }

   public static boolean hasLobbyPlayerCount() {
      return sidebarLines().stream().anyMatch(line -> line.matches(".*Players: §a\\d+/\\d+.*"));
   }

   private static String formatScoreboardLine(Scoreboard scoreboard, Score score) {
      return ScorePlayerTeam.formatPlayerName(scoreboard.getPlayersTeam(score.getPlayerName()), score.getPlayerName());
   }
}
