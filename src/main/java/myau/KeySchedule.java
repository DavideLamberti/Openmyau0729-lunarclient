package myau;

import java.util.ArrayList;
import java.util.Vector;

public class KeySchedule implements KeyTransform {
   private long a;
   private int[] b;
   private KeyTransform c;
   private long d;
   private long[] j;
   private static long[] k = new long[64];
   static int[] e;
   private static ArrayList f;
   private static Vector m;
   private static int i;
   private static Object o;
   private static int h = 52;
   private static int l = 128;
   private static int n = 17;

   public static Object g() {
      return o;
   }

   public static KeyTransform a(long var0, long var2, Object var4) {
      KeyScheduleChain.a(var0 > 0L);
      KeyTransform var5 = d(var0);
      KeyTransform var6 = d(var2);
      KeyTransform var7 = KeyScheduleChain.a(var5, var6);
      if (var4 != null) {
         m.add(var4);
      }

      return var7;
   }

   static KeyTransform c(long var0) {
      int var2 = (int)a(var0, h, 63, e, k);
      if (var2 < i) {
         return (KeyTransform)f.get(var2);
      } else {
         if (f.size() % l == 0) {
            e = (int[])e.clone();
         }

         KeySchedule var3 = new KeySchedule(var0);
         f.add(var3);
         return var3;
      }
   }

   private static KeyTransform d(long var0) {
      return new KeySchedule(var0);
   }

   static void a(KeyScheduleChain var0) {
      i = f.size();
      c();
      var0.d();
   }

   static void b(KeyScheduleChain var0) {
      c();
      int[] var10000 = e = new int[64];
      var10000[0] = -25;
      var10000[1] = -12;
      var10000[2] = -57;
      var10000[3] = -57;
      var10000[4] = -29;
      var10000[5] = -38;
      var10000[6] = -51;
      var10000[7] = -20;
      var10000[8] = -29;
      var10000[9] = -17;
      var10000[10] = -39;
      var10000[11] = -31;
      var10000[12] = -26;
      var10000[13] = 12;
      var10000[14] = -27;
      var10000[15] = -24;
      var10000[16] = -37;
      var10000[17] = -15;
      var10000[18] = -12;
      var10000[19] = -2;
      var10000[20] = -35;
      var10000[21] = 2;
      var10000[22] = -18;
      var10000[23] = -31;
      var10000[24] = -37;
      var10000[25] = 25;
      var10000[26] = 17;
      var10000[27] = 20;
      var10000[28] = -20;
      var10000[29] = -29;
      var10000[30] = 12;
      var10000[31] = -19;
      var10000[32] = 15;
      var10000[33] = 29;
      var10000[34] = -28;
      var10000[35] = -10;
      var10000[36] = -11;
      var10000[37] = 29;
      var10000[38] = 26;
      var10000[39] = 24;
      var10000[40] = 18;
      var10000[41] = 27;
      var10000[42] = 31;
      var10000[43] = 38;
      var10000[44] = -19;
      var10000[45] = 10;
      var10000[46] = -6;
      var10000[47] = 11;
      var10000[48] = 20;
      var10000[49] = 39;
      var10000[50] = 19;
      var10000[51] = -5;
      var10000[52] = 6;
      var10000[53] = 37;
      var10000[54] = 31;
      var10000[55] = 35;
      var10000[56] = 5;
      var10000[57] = 51;
      var10000[58] = 29;
      var10000[59] = 57;
      var10000[60] = 57;
      var10000[61] = 37;
      var10000[62] = 28;
      var10000[63] = 19;
      var0.c();
   }

   private KeySchedule(long var1) {
      this.a = var1;
      this.b = e;
      this.j = k;
   }

   @Override
   public long a(long var1) {
      long var3 = this.a(8, 55);
      long var5 = this.a ^ var1 ^ this.d;
      this.a = var5;
      if (this.c != null) {
         this.c.a(var1);
      }

      return var3;
   }

   @Override
   public void b(long var1) {
      this.d = var1;
   }

   @Override
   public void a(KeyTransform var1) {
      if (this != var1) {
         if (this.c == null) {
            this.c = var1;
         } else {
            this.c.a(var1);
         }
      }
   }

   @Override
   public int hashCode() {
      return (int)this.a(8);
   }

   @Override
   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else {
         return var1 instanceof KeySchedule ? this.a(56) == ((KeySchedule)var1).a(56) : false;
      }
   }

   @Override
   public boolean b(KeyTransform var1) {
      if (this == var1) {
         return true;
      } else {
         return var1 instanceof KeySchedule ? this.a(56, 63) - ((KeySchedule)var1).a(56, 63) <= 0L : true;
      }
   }

   @Override
   public int[] b() {
      return this.b;
   }

   private long a(int var1) {
      return this.a(0, var1 - 1);
   }

   private long a(int var1, int var2) {
      return a(this.a, var1, var2, this.b, this.j);
   }

   private static long a(long var0, int var2, int var3, int[] var4, long[] var5) {
      long var6 = 0L;
      int var8 = var4.length;

      for (int var9 = 0; var9 < var8; var9++) {
         long var10 = var0 & k[var9];
         long var12 = (long)var4[var9];
         if (var10 != 0L) {
            if (var12 > 0L) {
               var10 >>>= (int)var12;
            } else if (var12 < 0L) {
               var10 <<= (int)(~var12 + 1L);
            }

            var6 |= var10;
         }
      }

      byte var13 = 64;
      long var14 = var6;
      long var11 = (long)(var13 - 1 - var3);
      if (var11 > 0L) {
         var14 = var6 << (int)var11;
      }

      long var15 = (long)(var2 + var13 - 1 - var3);
      if (var15 > 0L) {
         var14 >>>= (int)var15;
      }

      return var14;
   }

   private static void c() {
      byte var0 = 0;
      a(0, f.size() - 1, f, new ArrayList(f), var0);
   }

   private static void a(int var0, int var1, ArrayList var2, ArrayList var3, int var4) {
      if (var0 < var1) {
         int var5 = var0 + (var1 - var0) / 2;
         if (++var4 < n) {
            a(var0, var5, var2, var3, var4);
            a(var5 + 1, var1, var2, var3, var4);
         }

         a(var0, var5, var1, var2, var3);
      }
   }

   private static void a(int var0, int var1, int var2, ArrayList var3, ArrayList var4) {
      int var5 = var0;
      int var6 = var1 + 1;

      for (int var7 = var0; var7 <= var2; var7++) {
         var4.set(var7, var3.get(var7));
      }

      while (var5 <= var1 && var6 <= var2) {
         KeyTransform var8;
         if (((KeyTransform)var4.get(var5)).b((KeyTransform)var4.get(var6))) {
            var8 = (KeyTransform)var4.get(var5++);
         } else {
            var8 = (KeyTransform)var4.get(var6++);
         }

         var3.set(var0, var8);
         var0++;
      }

      while (var5 <= var1) {
         var3.set(var0, var4.get(var5));
         var0++;
         var5++;
      }
   }

   private static void a0() {
      e = new int[]{
         -18,
         -53,
         -12,
         -20,
         -6,
         -40,
         -9,
         -21,
         -54,
         -12,
         6,
         -37,
         -20,
         -39,
         12,
         9,
         -39,
         -24,
         18,
         -32,
         -43,
         12,
         -17,
         20,
         -11,
         -31,
         -27,
         -10,
         21,
         -11,
         -6,
         -28,
         20,
         -10,
         -15,
         11,
         6,
         10,
         -12,
         17,
         11,
         24,
         -15,
         10,
         -2,
         40,
         2,
         -14,
         37,
         15,
         12,
         32,
         39,
         27,
         53,
         39,
         31,
         15,
         -2,
         28,
         2,
         14,
         54,
         43
      };
      f.add(new KeySchedule(-3999374383051014309L));
      f.add(new KeySchedule(2635815572757692884L));
      f.add(new KeySchedule(-4201638327099795734L));
      f.add(new KeySchedule(-6084282267381091521L));
      f.add(new KeySchedule(-345115378139750885L));
      f.add(new KeySchedule(-2124082391386771209L));
      f.add(new KeySchedule(-1759172714370106885L));
      f.add(new KeySchedule(-3686706567445308596L));
      f.add(new KeySchedule(3715965199099150851L));
      f.add(new KeySchedule(-2298658932971603379L));
      f.add(new KeySchedule(-8172901954262910548L));
      f.add(new KeySchedule(-2821879101695812788L));
      f.add(new KeySchedule(-5370808326808087915L));
      f.add(new KeySchedule(-1945804772819054815L));
      f.add(new KeySchedule(-316830849736993621L));
      f.add(new KeySchedule(3612787457363174729L));
      f.add(new KeySchedule(8071572089419868903L));
      f.add(new KeySchedule(-4875953383501070316L));
      f.add(new KeySchedule(-7791301094550753767L));
      f.add(new KeySchedule(1693573864236271283L));
      f.add(new KeySchedule(-2127652052053112315L));
      f.add(new KeySchedule(2478051500473096501L));
      f.add(new KeySchedule(7082095136913281866L));
      f.add(new KeySchedule(2452768778527371994L));
      f.add(new KeySchedule(-8273803834636857855L));
      f.add(new KeySchedule(-4856427576610911736L));
      f.add(new KeySchedule(-9076070838668588999L));
      f.add(new KeySchedule(-2104118883927442512L));
      f.add(new KeySchedule(2683655057904500800L));
      f.add(new KeySchedule(-3136029715258262930L));
      f.add(new KeySchedule(-6576102777707248689L));
      f.add(new KeySchedule(-8938456447235782037L));
      f.add(new KeySchedule(-5375191778047007197L));
      f.add(new KeySchedule(3131306998315763251L));
      f.add(new KeySchedule(1655060967011306745L));
      f.add(new KeySchedule(-3404132210384114049L));
      f.add(new KeySchedule(-9193409413297990343L));
      f.add(new KeySchedule(8364701052372393591L));
      f.add(new KeySchedule(3548079997902023552L));
      f.add(new KeySchedule(-1030907178823566138L));
      f.add(new KeySchedule(-5341747663881449199L));
      f.add(new KeySchedule(-4932564287628987104L));
      f.add(new KeySchedule(-6542301897721328669L));
      f.add(new KeySchedule(873129710726329975L));
      f.add(new KeySchedule(3652735291078655733L));
      f.add(new KeySchedule(-5319019816921056418L));
      f.add(new KeySchedule(-8212665736677234681L));
      f.add(new KeySchedule(-7752112399151000263L));
      f.add(new KeySchedule(7935832191907187381L));
      f.add(new KeySchedule(-8003066670716260012L));
      f.add(new KeySchedule(-3794055881765285343L));
      f.add(new KeySchedule(2019660640804696290L));
      f.add(new KeySchedule(2697092282866204685L));
      f.add(new KeySchedule(3005840499562293028L));
      f.add(new KeySchedule(6139952993912133825L));
      f.add(new KeySchedule(-5356585094301217554L));
      f.add(new KeySchedule(-5581197723935563526L));
      f.add(new KeySchedule(6502975012667014354L));
      f.add(new KeySchedule(-5084239332071671902L));
      f.add(new KeySchedule(-3622095810097029666L));
      f.add(new KeySchedule(-7662456213495311635L));
      f.add(new KeySchedule(59177057996654411L));
      f.add(new KeySchedule(9219023297813728179L));
      f.add(new KeySchedule(-1346807132700643334L));
      f.add(new KeySchedule(3666529585368018259L));
      f.add(new KeySchedule(-4179528956473469195L));
      f.add(new KeySchedule(-8308043374254557172L));
      f.add(new KeySchedule(-7680775144900118563L));
      f.add(new KeySchedule(6456159142673163907L));
      f.add(new KeySchedule(-6605666641103884859L));
      f.add(new KeySchedule(3496681951330221902L));
      f.add(new KeySchedule(-3819904937965517659L));
      f.add(new KeySchedule(8855457680796459612L));
      f.add(new KeySchedule(3205352678222114009L));
      f.add(new KeySchedule(3296672218419604529L));
      f.add(new KeySchedule(5259722187059237298L));
      f.add(new KeySchedule(3932175085156307130L));
      f.add(new KeySchedule(-4819116252298986175L));
      f.add(new KeySchedule(954758209738248044L));
      f.add(new KeySchedule(-7069537311115398988L));
      f.add(new KeySchedule(-7057085792149136628L));
      f.add(new KeySchedule(3927724276651185126L));
      f.add(new KeySchedule(-1027727818399538221L));
      f.add(new KeySchedule(-7167297021433159635L));
      f.add(new KeySchedule(-8529917528576348615L));
      f.add(new KeySchedule(2128259995935888749L));
      f.add(new KeySchedule(7267233294896728296L));
      f.add(new KeySchedule(-3489583660158387830L));
      f.add(new KeySchedule(-2047828950885448132L));
      f.add(new KeySchedule(7665218173198151414L));
      f.add(new KeySchedule(5111056004565573820L));
      f.add(new KeySchedule(8468909919991391935L));
      f.add(new KeySchedule(-1150215272025389428L));
      f.add(new KeySchedule(1890015179385749999L));
      f.add(new KeySchedule(7566450594538473003L));
      f.add(new KeySchedule(2690119573695258220L));
      f.add(new KeySchedule(-1395351495194269996L));
      f.add(new KeySchedule(-6000251910642934414L));
      f.add(new KeySchedule(273911664860367544L));
      f.add(new KeySchedule(-3889487035185601443L));
      f.add(new KeySchedule(-635774116832943596L));
      f.add(new KeySchedule(677433524823024379L));
      f.add(new KeySchedule(-3478227399189834092L));
      f.add(new KeySchedule(4863112230885542362L));
      f.add(new KeySchedule(-5793953934548265354L));
      f.add(new KeySchedule(3780200014541809262L));
      f.add(new KeySchedule(-8864296512430998465L));
      f.add(new KeySchedule(-7347065460425306776L));
      f.add(new KeySchedule(-8501043066004941648L));
      f.add(new KeySchedule(-6578703668750678398L));
      f.add(new KeySchedule(-7776458814990526157L));
      f.add(new KeySchedule(20095734939810235L));
      f.add(new KeySchedule(-1518939928782266764L));
      f.add(new KeySchedule(-3006583385090408861L));
      f.add(new KeySchedule(2328666425774469516L));
      f.add(new KeySchedule(2746219019933722575L));
      f.add(new KeySchedule(3512464936742950548L));
      f.add(new KeySchedule(2458318232475056821L));
      f.add(new KeySchedule(471677102632994699L));
      f.add(new KeySchedule(-4382342558488459947L));
      f.add(new KeySchedule(1708543250816105815L));
      f.add(new KeySchedule(3399713896502416751L));
      f.add(new KeySchedule(3864967713950563319L));
      f.add(new KeySchedule(1419929279146806527L));
      f.add(new KeySchedule(-805114384150927159L));
      f.add(new KeySchedule(4099577474513554204L));
      f.add(new KeySchedule(-5671708692229249352L));
      f.add(new KeySchedule(-484636125315824466L));
      f.add(new KeySchedule(1481501342793257742L));
      f.add(new KeySchedule(-5534123091076826516L));
      f.add(new KeySchedule(-6873493939184034310L));
      f.add(new KeySchedule(7675182159634106909L));
      f.add(new KeySchedule(5033424766294213731L));
      f.add(new KeySchedule(-8635411787752476793L));
      f.add(new KeySchedule(5548676912397557153L));
      f.add(new KeySchedule(8706465747431468448L));
      f.add(new KeySchedule(7843698170605475658L));
      f.add(new KeySchedule(-6413395735938200860L));
      f.add(new KeySchedule(5590665867080514906L));
      f.add(new KeySchedule(1367499282690259961L));
      f.add(new KeySchedule(-8337016051122524468L));
      f.add(new KeySchedule(860263337526953956L));
      f.add(new KeySchedule(3917002920137879804L));
      f.add(new KeySchedule(-405123942251216579L));
      f.add(new KeySchedule(-6152301939922811219L));
      f.add(new KeySchedule(-9216801203635992984L));
      f.add(new KeySchedule(3071156329597016680L));
      f.add(new KeySchedule(-4696395008550099569L));
      f.add(new KeySchedule(-698233060173933563L));
      f.add(new KeySchedule(8715346885796873149L));
      f.add(new KeySchedule(-901681568300906700L));
      f.add(new KeySchedule(6605627917133191106L));
      f.add(new KeySchedule(2369631954637843274L));
      f.add(new KeySchedule(-8426303952641529937L));
      e = (int[])e.clone();
      f.add(new KeySchedule(3673231069305525798L));
      f.add(new KeySchedule(2547734160528529856L));
      f.add(new KeySchedule(2732062510333214310L));
      f.add(new KeySchedule(-3127982728548144948L));
      f.add(new KeySchedule(9094190267632744026L));
      f.add(new KeySchedule(-4264822580090095692L));
      f.add(new KeySchedule(7152733761689632529L));
      f.add(new KeySchedule(8419921476056310070L));
      f.add(new KeySchedule(-5222000008797703712L));
      f.add(new KeySchedule(-4712409225076894032L));
      f.add(new KeySchedule(-8801963715072819203L));
      f.add(new KeySchedule(-8216375880313076966L));
      f.add(new KeySchedule(8918553783561534088L));
      f.add(new KeySchedule(-3008949224280104909L));
      f.add(new KeySchedule(3442678967284685821L));
      f.add(new KeySchedule(2078823242618795798L));
      f.add(new KeySchedule(-3719953388461462148L));
      f.add(new KeySchedule(-4160482350428426064L));
      f.add(new KeySchedule(4846019104676565815L));
      f.add(new KeySchedule(-209488412181894398L));
      f.add(new KeySchedule(364450675934108418L));
      f.add(new KeySchedule(978809621769412026L));
      f.add(new KeySchedule(-6548430720567432482L));
      f.add(new KeySchedule(7348521110700609675L));
      f.add(new KeySchedule(5489523925530199680L));
      f.add(new KeySchedule(6740124085452209633L));
      f.add(new KeySchedule(4872251730058560855L));
      f.add(new KeySchedule(4549755808379078281L));
      f.add(new KeySchedule(5294114559881963075L));
      f.add(new KeySchedule(6517245778501891820L));
      f.add(new KeySchedule(4921490691612394159L));
      f.add(new KeySchedule(4887016561873697550L));
      f.add(new KeySchedule(-362730124731588533L));
      f.add(new KeySchedule(-7876338896591619620L));
      f.add(new KeySchedule(3975496225827620912L));
      f.add(new KeySchedule(-7377900282947781115L));
      f.add(new KeySchedule(-3754288378549963527L));
      f.add(new KeySchedule(-7223287645873588464L));
      f.add(new KeySchedule(4756338164380609495L));
      f.add(new KeySchedule(2039651034496430540L));
      f.add(new KeySchedule(3590938162097566900L));
      f.add(new KeySchedule(-3244924518724623099L));
      f.add(new KeySchedule(2537088516055634144L));
      f.add(new KeySchedule(-5847204945933753325L));
      f.add(new KeySchedule(-5913379630712049402L));
      f.add(new KeySchedule(3905472406407931288L));
      f.add(new KeySchedule(-8251543112743060333L));
      f.add(new KeySchedule(-1833017341294394278L));
      f.add(new KeySchedule(-8202626884789684158L));
      f.add(new KeySchedule(376862443447811592L));
      f.add(new KeySchedule(-621515565018355125L));
      f.add(new KeySchedule(-2135312165276204407L));
      f.add(new KeySchedule(-2108025156290202812L));
      f.add(new KeySchedule(-6754614308341620628L));
      f.add(new KeySchedule(-4041114030146523853L));
      f.add(new KeySchedule(-1367362566001379990L));
      f.add(new KeySchedule(1488888977206030798L));
      f.add(new KeySchedule(-9180073169927504167L));
      f.add(new KeySchedule(6380762810007717984L));
      f.add(new KeySchedule(-8783394199516458263L));
      f.add(new KeySchedule(5914404420407825364L));
      f.add(new KeySchedule(-3691192088369006929L));
      f.add(new KeySchedule(2675975299405749350L));
      f.add(new KeySchedule(98748842931917951L));
      f.add(new KeySchedule(-3478995522334059491L));
      f.add(new KeySchedule(-727237759185166062L));
      f.add(new KeySchedule(-3632276134891693047L));
      f.add(new KeySchedule(-5151526142374264036L));
      f.add(new KeySchedule(5796878663600742249L));
      f.add(new KeySchedule(5889062566989048339L));
      f.add(new KeySchedule(-5082002772177052531L));
      f.add(new KeySchedule(8840556759958868627L));
      f.add(new KeySchedule(8892709042655520423L));
      f.add(new KeySchedule(-5336356347851929064L));
      f.add(new KeySchedule(1487512823959388787L));
      f.add(new KeySchedule(9076822354414300425L));
      f.add(new KeySchedule(-856601303189811156L));
      f.add(new KeySchedule(7123808983547915610L));
      f.add(new KeySchedule(-5293459863427318892L));
      f.add(new KeySchedule(3404567882872843575L));
      f.add(new KeySchedule(-8507088331481799431L));
      f.add(new KeySchedule(-6963048979344516470L));
      f.add(new KeySchedule(-1034928441256421686L));
      f.add(new KeySchedule(7565030495261091800L));
      f.add(new KeySchedule(8637798413900591737L));
      f.add(new KeySchedule(-3189729258514400818L));
      f.add(new KeySchedule(-4925117395835007316L));
      f.add(new KeySchedule(6877733629953862182L));
      f.add(new KeySchedule(-8057997388241165986L));
      f.add(new KeySchedule(-3574500965225047718L));
      f.add(new KeySchedule(-5276569892802615721L));
      f.add(new KeySchedule(6558949050323068323L));
      f.add(new KeySchedule(-5161790084289887940L));
      f.add(new KeySchedule(-348775370401228551L));
      f.add(new KeySchedule(3193636622744711022L));
      f.add(new KeySchedule(-3273855612432237203L));
      f.add(new KeySchedule(-4791392017781171215L));
      f.add(new KeySchedule(7294079257708219249L));
      f.add(new KeySchedule(-7332321250217948805L));
      f.add(new KeySchedule(-2870159960644331815L));
      f.add(new KeySchedule(-3461931722996154142L));
      f.add(new KeySchedule(1844469158549425100L));
      f.add(new KeySchedule(200981544447046510L));
      f.add(new KeySchedule(3786540518299713294L));
      f.add(new KeySchedule(1536291378449022027L));
      f.add(new KeySchedule(5961668168410525590L));
      f.add(new KeySchedule(5281527689526274822L));
      f.add(new KeySchedule(-8081341482149604075L));
      f.add(new KeySchedule(6867121017536831503L));
      f.add(new KeySchedule(-2156364223605057539L));
      f.add(new KeySchedule(5900863483904808379L));
      f.add(new KeySchedule(5647994790776979200L));
      f.add(new KeySchedule(-4501824512644582456L));
      f.add(new KeySchedule(876747549247459645L));
      f.add(new KeySchedule(2880320514615844984L));
      f.add(new KeySchedule(7468576507846107754L));
      f.add(new KeySchedule(5992616001513517519L));
      f.add(new KeySchedule(7016829571194968715L));
      f.add(new KeySchedule(7048184335570080276L));
      f.add(new KeySchedule(-6327855009865872494L));
      f.add(new KeySchedule(-8948532652443243682L));
      f.add(new KeySchedule(262270709595533120L));
      f.add(new KeySchedule(-8251679454297785197L));
      f.add(new KeySchedule(3942470836489084617L));
      f.add(new KeySchedule(8424537064296209678L));
      f.add(new KeySchedule(-2830651495403701127L));
      f.add(new KeySchedule(-3647992623075685509L));
      f.add(new KeySchedule(-6317999099590849495L));
      f.add(new KeySchedule(-3352112957065237881L));
      f.add(new KeySchedule(8949830040107108300L));
      f.add(new KeySchedule(-1994792867018132375L));
      f.add(new KeySchedule(6148312577388756946L));
      f.add(new KeySchedule(3702423574002024950L));
      f.add(new KeySchedule(5144484651149476356L));
      f.add(new KeySchedule(7335904341035336974L));
      f.add(new KeySchedule(8696169195653201727L));
      f.add(new KeySchedule(-4195687286336343638L));
      f.add(new KeySchedule(-8059078077413263647L));
      f.add(new KeySchedule(-6621073522493083867L));
      f.add(new KeySchedule(5297607784597042769L));
      f.add(new KeySchedule(4970768749411441997L));
      f.add(new KeySchedule(-2855930885284914524L));
      f.add(new KeySchedule(-6398874446434648827L));
      f.add(new KeySchedule(-5807208294091523838L));
      f.add(new KeySchedule(7817692216780700131L));
      f.add(new KeySchedule(7643036709623343228L));
      f.add(new KeySchedule(895928748372793003L));
      f.add(new KeySchedule(366319495653381243L));
      f.add(new KeySchedule(852527303618087838L));
      f.add(new KeySchedule(-4898174316265720255L));
      f.add(new KeySchedule(5860841045304039320L));
      f.add(new KeySchedule(-650495557149109197L));
      f.add(new KeySchedule(-6310699747427213738L));
      f.add(new KeySchedule(2892983082656877198L));
      f.add(new KeySchedule(-4258652268643839974L));
      f.add(new KeySchedule(-8925289266455052608L));
      f.add(new KeySchedule(8314750055508099498L));
      f.add(new KeySchedule(5652418312786999276L));
      f.add(new KeySchedule(-5219679262542087083L));
      f.add(new KeySchedule(8179742458147160697L));
      f.add(new KeySchedule(-1919974634907702403L));
      f.add(new KeySchedule(-7593794461017128734L));
      f.add(new KeySchedule(6308194791109346004L));
      f.add(new KeySchedule(6558958306362531576L));
      f.add(new KeySchedule(-4230838472344165055L));
      f.add(new KeySchedule(2854046355887010228L));
      f.add(new KeySchedule(-402109988002277403L));
      f.add(new KeySchedule(552264457053821336L));
      f.add(new KeySchedule(-6382963207604895543L));
      f.add(new KeySchedule(4731611735408922649L));
      f.add(new KeySchedule(-1857586363095412982L));
      f.add(new KeySchedule(-1977884679546440082L));
      f.add(new KeySchedule(8674814879613632095L));
      f.add(new KeySchedule(4545974866540528434L));
      f.add(new KeySchedule(7804539653786343663L));
      f.add(new KeySchedule(-6395069787373552162L));
      f.add(new KeySchedule(2375605840768419618L));
      f.add(new KeySchedule(-7414727486300959586L));
      f.add(new KeySchedule(-7610952973349638764L));
      f.add(new KeySchedule(9119522203976746211L));
      f.add(new KeySchedule(-6970467112538019001L));
      e = (int[])e.clone();
      f.add(new KeySchedule(-7533286181874820396L));
      f.add(new KeySchedule(-2719120026161751838L));
      f.add(new KeySchedule(3331822160685974436L));
      f.add(new KeySchedule(-3210852832905526457L));
      f.add(new KeySchedule(-5077932903430992487L));
      f.add(new KeySchedule(-6476932421746030946L));
      f.add(new KeySchedule(-2920595704922348427L));
      f.add(new KeySchedule(7289630512580714479L));
      f.add(new KeySchedule(-1774311381738832724L));
      f.add(new KeySchedule(-6508572832922198196L));
      f.add(new KeySchedule(-5604521861564149045L));
      f.add(new KeySchedule(6772559742409148932L));
      f.add(new KeySchedule(6907566830110511641L));
      f.add(new KeySchedule(-8725071003205835340L));
      f.add(new KeySchedule(1715292805146013451L));
      f.add(new KeySchedule(4827178415828771771L));
      f.add(new KeySchedule(-9153380495897636689L));
      f.add(new KeySchedule(5562142829352606560L));
      f.add(new KeySchedule(6973862732952171252L));
      f.add(new KeySchedule(-8331752161496226467L));
      f.add(new KeySchedule(7863495656602258190L));
      f.add(new KeySchedule(6120065052621191851L));
      f.add(new KeySchedule(-227852268230510724L));
      f.add(new KeySchedule(-3678567045432280184L));
      f.add(new KeySchedule(-6150405487126220584L));
      f.add(new KeySchedule(-1162460864962319971L));
      f.add(new KeySchedule(-7188658216855876280L));
      f.add(new KeySchedule(-6263281010504980042L));
      f.add(new KeySchedule(8365523427874449537L));
      f.add(new KeySchedule(3579189987868543550L));
      f.add(new KeySchedule(5273180875939925678L));
      f.add(new KeySchedule(-7262832059252340708L));
      f.add(new KeySchedule(-4938844302509855130L));
      f.add(new KeySchedule(-5246356358889324746L));
      f.add(new KeySchedule(490091225118073242L));
      f.add(new KeySchedule(-239116452176840942L));
      f.add(new KeySchedule(1802929662651334538L));
      f.add(new KeySchedule(-386798156355285033L));
      f.add(new KeySchedule(5672116785578683930L));
      f.add(new KeySchedule(-8925284868442095936L));
      f.add(new KeySchedule(1983227435417760299L));
      f.add(new KeySchedule(-7405951530580570690L));
      f.add(new KeySchedule(9046834915999646234L));
      f.add(new KeySchedule(6675694910681946093L));
      f.add(new KeySchedule(-1087291204923273213L));
      f.add(new KeySchedule(5008459877913112156L));
      f.add(new KeySchedule(3816616981890090786L));
      f.add(new KeySchedule(-6555943565980708597L));
      f.add(new KeySchedule(702057000635880841L));
      f.add(new KeySchedule(3254555270340737874L));
      f.add(new KeySchedule(1878323328869600687L));
      f.add(new KeySchedule(-64165593761026091L));
      f.add(new KeySchedule(-839790102571098643L));
      f.add(new KeySchedule(3409528372364193226L));
      f.add(new KeySchedule(-6016967517464810424L));
      f.add(new KeySchedule(7120288233204805814L));
      f.add(new KeySchedule(-4658837202997812721L));
      f.add(new KeySchedule(-402609742613836379L));
      f.add(new KeySchedule(-6570441896046957760L));
      f.add(new KeySchedule(5429602692830207614L));
      f.add(new KeySchedule(-3447302263955737231L));
      f.add(new KeySchedule(8902026089731472655L));
      f.add(new KeySchedule(-5234518185675550356L));
      f.add(new KeySchedule(1946040126898709220L));
      f.add(new KeySchedule(3193503131189380069L));
      f.add(new KeySchedule(-3974217508417031964L));
      f.add(new KeySchedule(-1243613986277152008L));
      f.add(new KeySchedule(-5551450882315750380L));
      f.add(new KeySchedule(-558195594432306048L));
      f.add(new KeySchedule(-3867909115955868005L));
      f.add(new KeySchedule(7727824283281287505L));
      f.add(new KeySchedule(4160753225039118183L));
      f.add(new KeySchedule(-3304750440796085190L));
      f.add(new KeySchedule(8357093070761281392L));
      f.add(new KeySchedule(-424079065547754543L));
      f.add(new KeySchedule(-7580899967274389512L));
      f.add(new KeySchedule(-262605424420543526L));
      f.add(new KeySchedule(5008747395596614830L));
      f.add(new KeySchedule(-3259322206316505814L));
      f.add(new KeySchedule(-6400179506012843398L));
      f.add(new KeySchedule(-5904848953524077257L));
      f.add(new KeySchedule(-397128208890293834L));
      f.add(new KeySchedule(8514481222357783549L));
      f.add(new KeySchedule(1221379120449337280L));
      f.add(new KeySchedule(-4425827814018476042L));
      f.add(new KeySchedule(7617163118662819923L));
      f.add(new KeySchedule(-4646002106030041747L));
      f.add(new KeySchedule(2893470554905131486L));
      f.add(new KeySchedule(3757323014807000839L));
      f.add(new KeySchedule(3224437444817870527L));
      f.add(new KeySchedule(9022244604684170445L));
      f.add(new KeySchedule(-2616048171313995841L));
      f.add(new KeySchedule(3097703662775347161L));
      f.add(new KeySchedule(-159783105350725389L));
      f.add(new KeySchedule(-323140486319127592L));
      f.add(new KeySchedule(5025333814821303319L));
      f.add(new KeySchedule(7752778006295485707L));
      e = (int[])e.clone();
      f.add(new KeySchedule(-4237624001746533235L));
      f.add(new KeySchedule(-4866691186427520773L));
      f.add(new KeySchedule(-7044267516247661557L));
      f.add(new KeySchedule(-8464004857253055363L));
      f.add(new KeySchedule(-3642936416017944507L));
      f.add(new KeySchedule(4579460465106414460L));
      f.add(new KeySchedule(-8458140267117367900L));
      f.add(new KeySchedule(8252620987163232218L));
      f.add(new KeySchedule(-4247104442684898041L));
      f.add(new KeySchedule(2608339780334312115L));
      f.add(new KeySchedule(-7521708352542549129L));
      f.add(new KeySchedule(-5107209798952782733L));
      f.add(new KeySchedule(2712265565814677593L));
      f.add(new KeySchedule(-4784582901818547101L));
      f.add(new KeySchedule(-231136741245803423L));
      f.add(new KeySchedule(-1694588418537026341L));
      f.add(new KeySchedule(-8356134663842405033L));
      f.add(new KeySchedule(-4393316943917959870L));
      f.add(new KeySchedule(-6959246556025389279L));
      f.add(new KeySchedule(-6985710853843318127L));
      f.add(new KeySchedule(-4113939395668555354L));
      f.add(new KeySchedule(-4529522694328749572L));
      f.add(new KeySchedule(3984403464596491454L));
      f.add(new KeySchedule(-2469976831292672985L));
      f.add(new KeySchedule(3906323964527787591L));
      f.add(new KeySchedule(434275999149630489L));
      f.add(new KeySchedule(-7264794839914604848L));
      f.add(new KeySchedule(-6824630196835047752L));
      f.add(new KeySchedule(3970538740650922851L));
      f.add(new KeySchedule(-873889934231667527L));
      f.add(new KeySchedule(-119401184677978395L));
      f.add(new KeySchedule(-3753027035267925526L));
      f.add(new KeySchedule(-3231513388537294588L));
      f.add(new KeySchedule(472472091829664960L));
      f.add(new KeySchedule(2176793936156334605L));
      f.add(new KeySchedule(4121444528225945076L));
      f.add(new KeySchedule(7899467655917890085L));
      f.add(new KeySchedule(4942301564652941014L));
      f.add(new KeySchedule(300789833693399410L));
      f.add(new KeySchedule(-539964627738240220L));
      f.add(new KeySchedule(7150142864876773127L));
      f.add(new KeySchedule(799714593349330042L));
      f.add(new KeySchedule(5951742755111966197L));
      f.add(new KeySchedule(6417712246439723476L));
      f.add(new KeySchedule(3822756066721964777L));
      f.add(new KeySchedule(3631357279716277941L));
      f.add(new KeySchedule(-6482940904335338189L));
      f.add(new KeySchedule(-5276064188239540357L));
      f.add(new KeySchedule(-1858271582641293508L));
      f.add(new KeySchedule(7530753876343662556L));
      f.add(new KeySchedule(-3447297863760694159L));
      f.add(new KeySchedule(5440777521601359467L));
      f.add(new KeySchedule(5607059902701297280L));
      f.add(new KeySchedule(-1061262144333611368L));
      f.add(new KeySchedule(-3812093018919640993L));
      f.add(new KeySchedule(8624948069131107432L));
      f.add(new KeySchedule(-8405651899082853927L));
      f.add(new KeySchedule(1830534325890731881L));
      f.add(new KeySchedule(-3554608974875015431L));
      f.add(new KeySchedule(2405884335777562635L));
      f.add(new KeySchedule(7959426245048403152L));
      f.add(new KeySchedule(-1034794210274830134L));
      f.add(new KeySchedule(5506332109091797004L));
      f.add(new KeySchedule(3992008415664169019L));
      f.add(new KeySchedule(2635534903254866964L));
      f.add(new KeySchedule(4585698702657998872L));
      f.add(new KeySchedule(5888247725751441394L));
      f.add(new KeySchedule(1108095230732848620L));
      f.add(new KeySchedule(3193384736894527845L));
      f.add(new KeySchedule(2998549481683160721L));
      f.add(new KeySchedule(-7007560242759987172L));
      f.add(new KeySchedule(-6159663028633461133L));
      f.add(new KeySchedule(-7867736443005283326L));
      f.add(new KeySchedule(-2353301072942565139L));
      f.add(new KeySchedule(-8427680561903435273L));
      f.add(new KeySchedule(-3040216205307514977L));
      f.add(new KeySchedule(490830313225091287L));
      f.add(new KeySchedule(-3365713333855569030L));
      f.add(new KeySchedule(-7456770342416402017L));
      f.add(new KeySchedule(1407131635899358700L));
      f.add(new KeySchedule(3617378369731617944L));
      f.add(new KeySchedule(-2696161098869243577L));
      f.add(new KeySchedule(-6066147721482023330L));
      f.add(new KeySchedule(-8914075888682492712L));
      f.add(new KeySchedule(-600644884325294204L));
      f.add(new KeySchedule(5756283923216479683L));
      f.add(new KeySchedule(7508383409238487439L));
      f.add(new KeySchedule(4812066133541055139L));
      f.add(new KeySchedule(6278901144224049444L));
      f.add(new KeySchedule(-4712523042462846826L));
      f.add(new KeySchedule(-1607148256211461800L));
      f.add(new KeySchedule(2470157788769309436L));
      f.add(new KeySchedule(1603914358614792346L));
      f.add(new KeySchedule(-5788872043431844966L));
      f.add(new KeySchedule(501435359441733751L));
      f.add(new KeySchedule(1200106452730824110L));
      f.add(new KeySchedule(8984355343958686098L));
      f.add(new KeySchedule(-5391415630699248593L));
      f.add(new KeySchedule(-5973590825844427196L));
      f.add(new KeySchedule(-7010503477266034210L));
      f.add(new KeySchedule(3293075066996970967L));
      f.add(new KeySchedule(-9081705635302539454L));
      f.add(new KeySchedule(-2869777011768401513L));
      f.add(new KeySchedule(8131713722846573982L));
      f.add(new KeySchedule(-8850789467253201933L));
      f.add(new KeySchedule(-6009804594372622175L));
      f.add(new KeySchedule(-4276941077858185450L));
      f.add(new KeySchedule(845112613249757228L));
      f.add(new KeySchedule(-8973278575537522022L));
      f.add(new KeySchedule(7515931191740501532L));
      f.add(new KeySchedule(4532398588731863227L));
      f.add(new KeySchedule(-3791101760942846312L));
      f.add(new KeySchedule(-2876234801908756054L));
      f.add(new KeySchedule(5379884992595520788L));
      f.add(new KeySchedule(4408545073765837905L));
      f.add(new KeySchedule(-147556666317545230L));
      f.add(new KeySchedule(-2645022663140384272L));
      f.add(new KeySchedule(917461809351642306L));
      f.add(new KeySchedule(-9000218028725190042L));
      f.add(new KeySchedule(-5351077851674035222L));
      f.add(new KeySchedule(-261148541395122058L));
      f.add(new KeySchedule(9193338829931578205L));
      f.add(new KeySchedule(378281968853645593L));
      f.add(new KeySchedule(-2253607011502897029L));
      f.add(new KeySchedule(-3659679916151495033L));
      f.add(new KeySchedule(-2951867919521794651L));
      f.add(new KeySchedule(1231183021644317826L));
      f.add(new KeySchedule(3592143942717699113L));
      f.add(new KeySchedule(-672250057089815437L));
      f.add(new KeySchedule(-5975523437801356087L));
      f.add(new KeySchedule(529234551741373183L));
      f.add(new KeySchedule(1651622373584704420L));
      f.add(new KeySchedule(26895394137460819L));
      f.add(new KeySchedule(-8222476335338168145L));
      f.add(new KeySchedule(4324984864093169188L));
      f.add(new KeySchedule(-7686640894604045854L));
      f.add(new KeySchedule(3716793354766422304L));
      f.add(new KeySchedule(-3139959532505997764L));
      f.add(new KeySchedule(-565392090336172397L));
      f.add(new KeySchedule(1822705716347574076L));
      f.add(new KeySchedule(4464849080824141358L));
      f.add(new KeySchedule(6793892191870930550L));
      e = (int[])e.clone();
      f.add(new KeySchedule(5009140985497285268L));
      f.add(new KeySchedule(-622297815301607491L));
      f.add(new KeySchedule(4915818885145960206L));
      f.add(new KeySchedule(7429037873844069937L));
      f.add(new KeySchedule(1884090456869740950L));
      f.add(new KeySchedule(-194690609315832435L));
      f.add(new KeySchedule(-7915604483903025493L));
      f.add(new KeySchedule(-5819834937828161357L));
      f.add(new KeySchedule(-9212565767408412614L));
      f.add(new KeySchedule(7563168358772402023L));
      f.add(new KeySchedule(1295100328903726847L));
      f.add(new KeySchedule(4036523410205616320L));
      f.add(new KeySchedule(-5510126190444115098L));
      f.add(new KeySchedule(-6361311618680735162L));
      f.add(new KeySchedule(-6101465128795843029L));
      f.add(new KeySchedule(896859453353933535L));
      f.add(new KeySchedule(-2182290345862071423L));
      f.add(new KeySchedule(-7582932975018404863L));
      f.add(new KeySchedule(-3019815014886405951L));
      f.add(new KeySchedule(8433992520790445277L));
      f.add(new KeySchedule(-4405883054303607589L));
      f.add(new KeySchedule(5524896029930442667L));
      f.add(new KeySchedule(-4238845630738412000L));
      f.add(new KeySchedule(-3995617651858151212L));
      f.add(new KeySchedule(-7765729681033999484L));
      f.add(new KeySchedule(288432882924839439L));
      f.add(new KeySchedule(5796092938710692224L));
      f.add(new KeySchedule(3205749146988116329L));
      f.add(new KeySchedule(3601891281168283319L));
      f.add(new KeySchedule(-3662862559701349488L));
      f.add(new KeySchedule(3319319260308064419L));
      f.add(new KeySchedule(1089535430290107758L));
      f.add(new KeySchedule(4294479471716346134L));
      f.add(new KeySchedule(-6367483332809815980L));
      f.add(new KeySchedule(4147280231830805373L));
      f.add(new KeySchedule(8414777671138546495L));
      f.add(new KeySchedule(2040206593476183576L));
      f.add(new KeySchedule(-8157820529974528212L));
      f.add(new KeySchedule(-5886000318112024876L));
      f.add(new KeySchedule(-2964164196322972822L));
      f.add(new KeySchedule(-319342259540957904L));
      f.add(new KeySchedule(8445671925321887158L));
      f.add(new KeySchedule(-2302587790201444957L));
      f.add(new KeySchedule(-9002979441433856378L));
      f.add(new KeySchedule(399320039560482752L));
      f.add(new KeySchedule(7883117603277930831L));
      f.add(new KeySchedule(-18356774893618083L));
      f.add(new KeySchedule(2002678733661298625L));
      f.add(new KeySchedule(5862198536382745706L));
      f.add(new KeySchedule(-7912134798721493633L));
      f.add(new KeySchedule(6001564550010565218L));
      f.add(new KeySchedule(-7647476801368089127L));
      f.add(new KeySchedule(5372672186308653742L));
      f.add(new KeySchedule(5912866710206968613L));
      f.add(new KeySchedule(-6185728601532871418L));
      f.add(new KeySchedule(6449261633225365337L));
      f.add(new KeySchedule(-1329814494419065815L));
      f.add(new KeySchedule(-7774863093774505209L));
      f.add(new KeySchedule(7245181353635888659L));
      f.add(new KeySchedule(-4829278369377023548L));
      f.add(new KeySchedule(-4301678812454805086L));
      f.add(new KeySchedule(3688305409254768801L));
      f.add(new KeySchedule(-6567020513254769129L));
      f.add(new KeySchedule(-6997887547837331825L));
      f.add(new KeySchedule(6085707062888897669L));
      f.add(new KeySchedule(7526396776739779019L));
      f.add(new KeySchedule(1736392399140554508L));
      f.add(new KeySchedule(2109168835052444000L));
      f.add(new KeySchedule(-3178759896009488363L));
      f.add(new KeySchedule(1621393413240958337L));
      f.add(new KeySchedule(8890887996059974944L));
      f.add(new KeySchedule(3294955667037670677L));
      f.add(new KeySchedule(106144604698752106L));
      f.add(new KeySchedule(-7704658621988538327L));
      f.add(new KeySchedule(-3545967894254671928L));
      f.add(new KeySchedule(-7270044536312117803L));
      f.add(new KeySchedule(-7053244048903368717L));
      f.add(new KeySchedule(-6690277893590062375L));
      f.add(new KeySchedule(-470719273716933449L));
      f.add(new KeySchedule(-2786360809058273094L));
      f.add(new KeySchedule(-6824024070580594427L));
      f.add(new KeySchedule(-8577324903856290314L));
      f.add(new KeySchedule(-8356235678326037143L));
      f.add(new KeySchedule(-2010112834185564159L));
      f.add(new KeySchedule(-509814482853118666L));
      f.add(new KeySchedule(-7811036611018439555L));
      f.add(new KeySchedule(-8821289934386699162L));
      f.add(new KeySchedule(8305489136712720464L));
      f.add(new KeySchedule(-1619221311032482678L));
      f.add(new KeySchedule(-850359990107723130L));
      f.add(new KeySchedule(601755884121643864L));
      f.add(new KeySchedule(3516834468549648367L));
      f.add(new KeySchedule(-5682707943734351217L));
      f.add(new KeySchedule(-8590712371746150470L));
      f.add(new KeySchedule(8164481061249671073L));
      f.add(new KeySchedule(-8711234630436754518L));
      f.add(new KeySchedule(-537832514879302754L));
      f.add(new KeySchedule(3323180258607657022L));
      f.add(new KeySchedule(2512421110576724147L));
      f.add(new KeySchedule(2398768520203092047L));
      f.add(new KeySchedule(6938404435918200105L));
      f.add(new KeySchedule(2814277896675684817L));
      f.add(new KeySchedule(1179287690141650983L));
      f.add(new KeySchedule(-5352207872443374531L));
      f.add(new KeySchedule(-264311130043689807L));
      f.add(new KeySchedule(3814719763027523949L));
      f.add(new KeySchedule(5454522054590099516L));
      f.add(new KeySchedule(-3221009864464647030L));
      f.add(new KeySchedule(3287937365590710029L));
      f.add(new KeySchedule(3254062965990132693L));
      f.add(new KeySchedule(500405902266229670L));
      f.add(new KeySchedule(-5825873928188790962L));
      f.add(new KeySchedule(-6867752502153546282L));
      f.add(new KeySchedule(-6518965479486615763L));
      f.add(new KeySchedule(866649394284872096L));
      f.add(new KeySchedule(-2971959272712301318L));
      f.add(new KeySchedule(-1124270688794684876L));
      f.add(new KeySchedule(-721255290059708478L));
      f.add(new KeySchedule(-1569191611516039439L));
      f.add(new KeySchedule(5558525814284809266L));
      f.add(new KeySchedule(8793524166691188089L));
      f.add(new KeySchedule(1033598726097625069L));
      f.add(new KeySchedule(1038533763881817654L));
      f.add(new KeySchedule(-348102457641917312L));
      f.add(new KeySchedule(2374978600306257190L));
      f.add(new KeySchedule(-6094291567366470277L));
      f.add(new KeySchedule(1697916934016892880L));
      f.add(new KeySchedule(-7158010789186338369L));
      f.add(new KeySchedule(-6632799806045759775L));
      f.add(new KeySchedule(8138474541503638720L));
      f.add(new KeySchedule(206839239269182624L));
      f.add(new KeySchedule(-8568648321357685327L));
      f.add(new KeySchedule(66167332332047L));
      f.add(new KeySchedule(7574415826170566262L));
      f.add(new KeySchedule(-2795531525884162864L));
      f.add(new KeySchedule(-3292018607358069956L));
      f.add(new KeySchedule(4348574668899271683L));
      f.add(new KeySchedule(-4716222496261766775L));
      f.add(new KeySchedule(-8909563136762527665L));
      f.add(new KeySchedule(8474809817823639384L));
      f.add(new KeySchedule(7417085961883235032L));
      f.add(new KeySchedule(-3005814447132502567L));
      f.add(new KeySchedule(-7511208569222538070L));
      f.add(new KeySchedule(5333486063689141155L));
      f.add(new KeySchedule(4099573076499548700L));
      f.add(new KeySchedule(-2266119453744662674L));
      f.add(new KeySchedule(-3647465724210027027L));
      f.add(new KeySchedule(-75424101038549661L));
      f.add(new KeySchedule(-2901896266808918281L));
      f.add(new KeySchedule(5522706853816080993L));
      f.add(new KeySchedule(-2232461554244226772L));
      f.add(new KeySchedule(185369646570303527L));
      f.add(new KeySchedule(928452963610842478L));
      f.add(new KeySchedule(8142759230982351325L));
      f.add(new KeySchedule(-1176595218449638636L));
      f.add(new KeySchedule(2796047529816937312L));
      f.add(new KeySchedule(4780503131785138139L));
      f.add(new KeySchedule(-3631860662457175630L));
      f.add(new KeySchedule(8651348149383621712L));
      f.add(new KeySchedule(3943561875580450549L));
      f.add(new KeySchedule(689448401415080669L));
      f.add(new KeySchedule(-3894779667284726732L));
      f.add(new KeySchedule(4087804990290586618L));
      f.add(new KeySchedule(-325610580188809234L));
      f.add(new KeySchedule(-4391174194491548787L));
      f.add(new KeySchedule(-3942349964025597033L));
      f.add(new KeySchedule(-8479422458506636597L));
      f.add(new KeySchedule(-6923750338963652402L));
      f.add(new KeySchedule(8391444399914243417L));
      f.add(new KeySchedule(-8005895561149389656L));
      f.add(new KeySchedule(-443859588370908405L));
      f.add(new KeySchedule(-2764241499264769129L));
      f.add(new KeySchedule(-9040886269172364989L));
      f.add(new KeySchedule(-3680257025566430066L));
      f.add(new KeySchedule(2580595163347582250L));
      e = (int[])e.clone();
      f.add(new KeySchedule(-3695517019792454466L));
      f.add(new KeySchedule(-788793722418626288L));
      f.add(new KeySchedule(-399722199760349134L));
      f.add(new KeySchedule(-4294093773587418174L));
      f.add(new KeySchedule(-7959176760816014369L));
      f.add(new KeySchedule(-7406409095728700593L));
      f.add(new KeySchedule(4221652918272807026L));
      f.add(new KeySchedule(-5501650668636565113L));
      f.add(new KeySchedule(3944147539868806508L));
      f.add(new KeySchedule(1611255693311508221L));
      f.add(new KeySchedule(534195182647644698L));
      f.add(new KeySchedule(4401239183314036049L));
      f.add(new KeySchedule(6850478522002181863L));
      f.add(new KeySchedule(-8242243293708931163L));
      f.add(new KeySchedule(-2106489298785509603L));
      f.add(new KeySchedule(-3605792483280637340L));
      f.add(new KeySchedule(6766645640305075193L));
      f.add(new KeySchedule(4650144867650376016L));
      f.add(new KeySchedule(-4700235430542775118L));
      f.add(new KeySchedule(-7388242579403667765L));
      f.add(new KeySchedule(-5289634068139021028L));
      f.add(new KeySchedule(9040558509349968404L));
      f.add(new KeySchedule(3932279248126770734L));
      f.add(new KeySchedule(-1844479266874437052L));
      f.add(new KeySchedule(-5406135968440210145L));
      f.add(new KeySchedule(-3246271564693054011L));
      f.add(new KeySchedule(2744706872881334916L));
      f.add(new KeySchedule(-5007171086894992976L));
      f.add(new KeySchedule(7305290205569930603L));
      f.add(new KeySchedule(554412479200286052L));
      f.add(new KeySchedule(-8116838143690537133L));
      f.add(new KeySchedule(4661795781974562279L));
      f.add(new KeySchedule(-5835338151194374579L));
      f.add(new KeySchedule(5920929071788239257L));
      f.add(new KeySchedule(-5521496896056813846L));
      f.add(new KeySchedule(-6159699005519757804L));
      f.add(new KeySchedule(-3327244867036748630L));
      f.add(new KeySchedule(-1934736871745377765L));
      f.add(new KeySchedule(5493095258285188992L));
      f.add(new KeySchedule(3248832520200940873L));
      f.add(new KeySchedule(-342971587449392741L));
      f.add(new KeySchedule(7863750871312524107L));
      f.add(new KeySchedule(2353138924403472169L));
      f.add(new KeySchedule(8002310869879678646L));
      f.add(new KeySchedule(-556433307811393164L));
      f.add(new KeySchedule(5021776175347990308L));
      f.add(new KeySchedule(-3608469938017385556L));
      f.add(new KeySchedule(-4366415576038999583L));
      f.add(new KeySchedule(769023661929705674L));
      f.add(new KeySchedule(8704419456050971584L));
      f.add(new KeySchedule(2066895956660328070L));
      f.add(new KeySchedule(8680017998982209571L));
      f.add(new KeySchedule(3698510812156460518L));
      f.add(new KeySchedule(-8300144460021049509L));
      f.add(new KeySchedule(7551937956097330675L));
      f.add(new KeySchedule(2975680287896653047L));
      f.add(new KeySchedule(7436599526701445758L));
      f.add(new KeySchedule(984588050032029167L));
      f.add(new KeySchedule(3706139738309191736L));
      f.add(new KeySchedule(3185912076898311432L));
      f.add(new KeySchedule(-6296421408635065473L));
      f.add(new KeySchedule(8149218881291964747L));
      f.add(new KeySchedule(5337607108519197440L));
      f.add(new KeySchedule(4633522949306480895L));
      f.add(new KeySchedule(-915280999240937123L));
      f.add(new KeySchedule(-8646914131125693320L));
      f.add(new KeySchedule(1477335666605749306L));
      f.add(new KeySchedule(-1292175832023527636L));
      f.add(new KeySchedule(-9049581148636923904L));
      f.add(new KeySchedule(936860233778332200L));
      f.add(new KeySchedule(5747949312999973035L));
      f.add(new KeySchedule(4974981998986820662L));
      f.add(new KeySchedule(6119580385615713253L));
      f.add(new KeySchedule(5949342377356424L));
      f.add(new KeySchedule(1285805131562227641L));
      f.add(new KeySchedule(7630046846621058797L));
      f.add(new KeySchedule(6405646052586589742L));
      f.add(new KeySchedule(3703592021119895673L));
      f.add(new KeySchedule(4167420620652451348L));
      f.add(new KeySchedule(8721843686104419990L));
      f.add(new KeySchedule(-8908010788072571521L));
      f.add(new KeySchedule(-7696537013951906488L));
      f.add(new KeySchedule(-4640378034192217867L));
      f.add(new KeySchedule(-7515955198372431675L));
      f.add(new KeySchedule(-7183237408067385357L));
      f.add(new KeySchedule(-5477186918265909965L));
      f.add(new KeySchedule(7065950609194771436L));
      f.add(new KeySchedule(-2345358888071977770L));
      f.add(new KeySchedule(-6443942680379625758L));
      f.add(new KeySchedule(7185023005888659503L));
      f.add(new KeySchedule(5851644356016085187L));
      f.add(new KeySchedule(753352646775017378L));
      f.add(new KeySchedule(-1732851547910546586L));
      e = (int[])e.clone();
      f.add(new KeySchedule(1516209456380807042L));
      f.add(new KeySchedule(-5402647823627054294L));
      f.add(new KeySchedule(7084730030454241937L));
      f.add(new KeySchedule(-6877729082767027386L));
      f.add(new KeySchedule(-4837987070664375457L));
      f.add(new KeySchedule(3693260540435823557L));
      f.add(new KeySchedule(-3845835373671082808L));
      f.add(new KeySchedule(5751457964509000395L));
      f.add(new KeySchedule(284538649657772774L));
      f.add(new KeySchedule(5021631039813123876L));
      f.add(new KeySchedule(-3386856649587403011L));
      f.add(new KeySchedule(-3759152267931515071L));
      f.add(new KeySchedule(-6368846926526895843L));
      f.add(new KeySchedule(3270708669501689604L));
      f.add(new KeySchedule(-5980839704812020039L));
      f.add(new KeySchedule(-3657021810321465196L));
      f.add(new KeySchedule(2727866665094221176L));
      f.add(new KeySchedule(4663714828133602262L));
      f.add(new KeySchedule(1050738930905550418L));
      f.add(new KeySchedule(4309606839456019600L));
      f.add(new KeySchedule(-3705687990128557731L));
      f.add(new KeySchedule(6029981411494387249L));
      f.add(new KeySchedule(-3890259341234516014L));
      f.add(new KeySchedule(4226013261433152426L));
      f.add(new KeySchedule(-1946178096605159151L));
      f.add(new KeySchedule(4761029653592834756L));
      f.add(new KeySchedule(5253612278530360737L));
      f.add(new KeySchedule(-7598239328911835427L));
      f.add(new KeySchedule(-7752632817218462987L));
      f.add(new KeySchedule(-4277475456520933585L));
      f.add(new KeySchedule(-5823745182356850735L));
      f.add(new KeySchedule(8600250871970187994L));
      f.add(new KeySchedule(1924232946748525836L));
      f.add(new KeySchedule(-2131486256681797382L));
      f.add(new KeySchedule(8845454764003352737L));
      f.add(new KeySchedule(6768947515944247645L));
      f.add(new KeySchedule(2866315537447697358L));
      f.add(new KeySchedule(-6290462740770799523L));
      f.add(new KeySchedule(-3731007803882722243L));
      f.add(new KeySchedule(-8696515720052301581L));
      f.add(new KeySchedule(-4947901345957108975L));
      f.add(new KeySchedule(4213305291864430770L));
      f.add(new KeySchedule(-8336556463588215436L));
      f.add(new KeySchedule(31742252584452809L));
      f.add(new KeySchedule(-8135838412586745062L));
      f.add(new KeySchedule(6067625189940146729L));
      f.add(new KeySchedule(7391713955055580245L));
      f.add(new KeySchedule(-1598623168936794612L));
      f.add(new KeySchedule(-7631615855701994965L));
      f.add(new KeySchedule(-3664381222937367336L));
      f.add(new KeySchedule(-7107631978796626602L));
      f.add(new KeySchedule(6393134691723179233L));
      f.add(new KeySchedule(-7145916300226441342L));
      f.add(new KeySchedule(-3722630958134847055L));
      f.add(new KeySchedule(-3287210050459873212L));
      f.add(new KeySchedule(-1624992801177069758L));
      f.add(new KeySchedule(-7932998856987602309L));
      f.add(new KeySchedule(-6534002130601350120L));
      f.add(new KeySchedule(-7413138543590758055L));
      f.add(new KeySchedule(-7740024141038984880L));
      f.add(new KeySchedule(1693665877900161055L));
      f.add(new KeySchedule(-6653653841671083752L));
      f.add(new KeySchedule(2333576275930803940L));
      f.add(new KeySchedule(8190345279179105822L));
      f.add(new KeySchedule(-2660651311614390042L));
      f.add(new KeySchedule(7497696257313229744L));
      f.add(new KeySchedule(4415771389753819889L));
      f.add(new KeySchedule(8739525062805579638L));
      f.add(new KeySchedule(1816295161385589203L));
      f.add(new KeySchedule(-2800860345871309236L));
      f.add(new KeySchedule(1911526585383528793L));
      f.add(new KeySchedule(-6114417142607477570L));
      f.add(new KeySchedule(-8529682442107945215L));
      f.add(new KeySchedule(9188549209515630953L));
      f.add(new KeySchedule(2866315539562674894L));
      f.add(new KeySchedule(6369537209705353834L));
      f.add(new KeySchedule(-8576284855427235529L));
      f.add(new KeySchedule(-2391738614229099873L));
      f.add(new KeySchedule(-5487964791497099196L));
      f.add(new KeySchedule(7112142381840397639L));
      f.add(new KeySchedule(-7985027815967373453L));
      f.add(new KeySchedule(5047750449385743171L));
      f.add(new KeySchedule(-4877291904999295452L));
      f.add(new KeySchedule(-4700478568685569138L));
      f.add(new KeySchedule(2344481523500363070L));
      f.add(new KeySchedule(-4014185165296482698L));
      f.add(new KeySchedule(-8415110175963988431L));
      f.add(new KeySchedule(5897219572309431768L));
      f.add(new KeySchedule(4357529641420516336L));
      f.add(new KeySchedule(5698650388385033830L));
      f.add(new KeySchedule(-3878397137218909141L));
      f.add(new KeySchedule(7725876884024207216L));
      f.add(new KeySchedule(2500576743548164062L));
      f.add(new KeySchedule(4403295730328990055L));
      f.add(new KeySchedule(2113009781552449970L));
      f.add(new KeySchedule(-2702990727447955866L));
      f.add(new KeySchedule(6858748259764594747L));
      f.add(new KeySchedule(-3084526465690500575L));
      f.add(new KeySchedule(-6694859715902017252L));
      f.add(new KeySchedule(6163674112978351128L));
      f.add(new KeySchedule(-2819978313605032019L));
      f.add(new KeySchedule(7397042353189585964L));
      f.add(new KeySchedule(6524212329395031061L));
      f.add(new KeySchedule(2865901942288568867L));
      f.add(new KeySchedule(-4501828908576115768L));
      f.add(new KeySchedule(5208189581450937927L));
      f.add(new KeySchedule(25906642307001753L));
      f.add(new KeySchedule(-5943919143698678874L));
      f.add(new KeySchedule(-4313059432347589620L));
      f.add(new KeySchedule(1178195059857835826L));
      f.add(new KeySchedule(2738002151871127319L));
      f.add(new KeySchedule(-3755529053625857582L));
      f.add(new KeySchedule(-773154863840184636L));
      f.add(new KeySchedule(-3470926291941292223L));
      f.add(new KeySchedule(2452298501070315116L));
      f.add(new KeySchedule(-7038368626258854017L));
      f.add(new KeySchedule(6631340580651543615L));
      f.add(new KeySchedule(-8596285820723264008L));
      f.add(new KeySchedule(-3242222839277751425L));
      f.add(new KeySchedule(-4629872395996335383L));
      f.add(new KeySchedule(6970284206053798384L));
      f.add(new KeySchedule(1727803106724200663L));
      f.add(new KeySchedule(4489313740857049212L));
      f.add(new KeySchedule(-9091104787286799688L));
      f.add(new KeySchedule(-1948280147764023918L));
      f.add(new KeySchedule(-905213755122365314L));
      f.add(new KeySchedule(7384068167320895626L));
      f.add(new KeySchedule(2329397857383001429L));
      f.add(new KeySchedule(8974927150171724199L));
      f.add(new KeySchedule(4378336351945801978L));
      f.add(new KeySchedule(-187009397377197810L));
      f.add(new KeySchedule(6708526465831048746L));
      f.add(new KeySchedule(-8417575606998493232L));
      f.add(new KeySchedule(4797783534885597782L));
      f.add(new KeySchedule(-2323041494516990657L));
      f.add(new KeySchedule(-6047146684561356507L));
      f.add(new KeySchedule(1521474737695272172L));
      f.add(new KeySchedule(-5518626825930402523L));
      f.add(new KeySchedule(-2961062546929459672L));
      f.add(new KeySchedule(896810719411729267L));
      f.add(new KeySchedule(-4733159239205247354L));
      f.add(new KeySchedule(1091160385817543081L));
      f.add(new KeySchedule(8856455560449025506L));
      e = (int[])e.clone();
      f.add(new KeySchedule(4267265379943229532L));
      f.add(new KeySchedule(-7352503904967666200L));
      f.add(new KeySchedule(-552096438165140553L));
      f.add(new KeySchedule(-7843910853039479348L));
      f.add(new KeySchedule(7624745680920030640L));
      f.add(new KeySchedule(4012084778434961634L));
      f.add(new KeySchedule(-1190138700278620840L));
      f.add(new KeySchedule(7058731978157069607L));
      f.add(new KeySchedule(-123833269922103639L));
      f.add(new KeySchedule(7394742165549510907L));
      f.add(new KeySchedule(-1696576156212523287L));
      f.add(new KeySchedule(7460970057569729321L));
      f.add(new KeySchedule(1122298645589056054L));
      f.add(new KeySchedule(4127621101915490418L));
      f.add(new KeySchedule(1410321286570705706L));
      f.add(new KeySchedule(7838400413842464656L));
      f.add(new KeySchedule(6314273705263021570L));
      f.add(new KeySchedule(-6735884417295015219L));
      f.add(new KeySchedule(-167493548937135677L));
      f.add(new KeySchedule(-2182401602697383627L));
      f.add(new KeySchedule(-5875906251753627537L));
      f.add(new KeySchedule(-4773397870713338180L));
      f.add(new KeySchedule(-7971801481576536546L));
      f.add(new KeySchedule(-7043954534624393093L));
      f.add(new KeySchedule(-4982340004725208954L));
      f.add(new KeySchedule(-26601242205706585L));
      f.add(new KeySchedule(9097838794270229034L));
      f.add(new KeySchedule(-7394420942591621311L));
      f.add(new KeySchedule(3283671915844765828L));
      f.add(new KeySchedule(-9126415497139681521L));
      f.add(new KeySchedule(-1961643624447424558L));
      f.add(new KeySchedule(8233548487763575248L));
      f.add(new KeySchedule(267703021942211091L));
      f.add(new KeySchedule(5601724994268613468L));
      f.add(new KeySchedule(9160633600552915443L));
      f.add(new KeySchedule(-6217695595651473789L));
      f.add(new KeySchedule(6116692205234476521L));
      f.add(new KeySchedule(-856430939527809314L));
      f.add(new KeySchedule(-2951660448253680260L));
      e = new int[]{
         -52,
         -49,
         -4,
         -43,
         -23,
         -10,
         4,
         -21,
         -41,
         -16,
         -4,
         -18,
         -28,
         -44,
         4,
         10,
         -40,
         -16,
         -21,
         -26,
         -24,
         -34,
         -2,
         -19,
         2,
         16,
         -9,
         23,
         21,
         18,
         -30,
         -17,
         -2,
         16,
         2,
         9,
         -18,
         -4,
         -15,
         21,
         28,
         4,
         19,
         -18,
         24,
         26,
         43,
         -4,
         17,
         41,
         49,
         4,
         52,
         15,
         18,
         34,
         40,
         44,
         -5,
         -3,
         30,
         18,
         3,
         5
      };
   }

   static {
      long var0 = 1L;

      for (int var2 = 0; var2 < 64; var2++) {
         k[var2] = var0;
         var0 <<= 1;
      }

      o = new Object();
      m = new Vector();
      f = new ArrayList();
      a0();
      i = f.size();
      c();
   }
}
