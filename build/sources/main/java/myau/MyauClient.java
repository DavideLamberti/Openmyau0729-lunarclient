package myau;

import java.lang.reflect.Field;
import java.util.ArrayList;

public final class MyauClient {
   public static final String CHAT_PREFIX = "&7[&bMyau&7] &r";
   public static final String VERSION = "1.0.0";
   public static EventBus eventBus;
   public static RotationSmoother rotationSmoother;
   public static GroundSpoofManager groundSpoofManager;
   public static HurtTimeTracker hurtTimeTracker;
   public static OutgoingPacketBuffer outgoingPacketBuffer;
   public static IncomingPacketBuffer incomingPacketBuffer;
   public static PacketDelayManager packetDelayManager;
   public static PacketActionTracker packetActionTracker;
   public static FriendList friendList;
   public static EnemyList enemyList;
   public static SettingManager settingManager;
   public static ModuleManager moduleManager;
   public static CommandManager commandManager;
   private static boolean initialized;

   private MyauClient() {
   }

   public static synchronized void initialize() {
      if (!initialized) {
         eventBus = new EventBus();
         rotationSmoother = new RotationSmoother();
         groundSpoofManager = new GroundSpoofManager();
         hurtTimeTracker = new HurtTimeTracker();
         outgoingPacketBuffer = new OutgoingPacketBuffer();
         incomingPacketBuffer = new IncomingPacketBuffer();
         packetDelayManager = new PacketDelayManager();
         packetActionTracker = new PacketActionTracker();
         friendList = new FriendList();
         enemyList = new EnemyList();
         settingManager = new SettingManager();
         moduleManager = new ModuleManager();
         commandManager = new CommandManager();
         registerInfrastructureListeners();
         registerModules();
         registerCommands();
         friendList.load();
         enemyList.load();
         initialized = true;
      }
   }

   private static void registerInfrastructureListeners() {
      eventBus.register(rotationSmoother);
      eventBus.register(groundSpoofManager);
      eventBus.register(hurtTimeTracker);
      eventBus.register(outgoingPacketBuffer);
      eventBus.register(incomingPacketBuffer);
      eventBus.register(packetDelayManager);
      eventBus.register(commandManager);
   }

   private static void registerModules() {
      registerModule(new AimAssistModule());
      registerModule(new AntiDebuffModule());
      registerModule(new AntiFireballModule());
      registerModule(new AntiObbyTrapModule());
      registerModule(new AntiObfuscateModule());
      registerModule(new AntiVoidModule());
      registerModule(new AutoClickerModule());
      registerModule(new AutoHealModule());
      registerModule(new AutoToolModule());
      registerModule(new BedESPModule());
      registerModule(new BedNukerModule());
      registerModule(new BedTrackerModule());
      registerModule(new BlinkModule());
      registerModule(new ChamsModule());
      registerModule(new ChestESPModule());
      registerModule(new ChestStealerModule());
      registerModule(new EagleModule());
      registerModule(new ESPModule());
      registerModule(new FastPlaceModule());
      registerModule(new FlyModule());
      registerModule(new FullbrightModule());
      registerModule(new GhostHandModule());
      registerModule(new GuiModule());
      registerModule(new HitSelectModule());
      registerModule(new HUDModule());
      registerModule(new IndicatorsModule());
      registerModule(new InvManagerModule());
      registerModule(new InvWalkModule());
      registerModule(new ItemESPModule());
      registerModule(new JesusModule());
      registerModule(new KeepSprintModule());
      registerModule(new KillAuraModule());
      registerModule(new LagRangeModule());
      registerModule(new LightningTrackerModule());
      registerModule(new LongJumpModule());
      registerModule(new MCFModule());
      registerModule(new NameTagsModule());
      registerModule(new NickHiderModule());
      registerModule(new NoFallModule());
      registerModule(new NoHitDelayModule());
      registerModule(new NoHurtCamModule());
      registerModule(new NoJumpDelayModule());
      registerModule(new NoRotateModule());
      registerModule(new NoSlowModule());
      registerModule(new ReachModule());
      registerModule(new SafeWalkModule());
      registerModule(new ScaffoldModule());
      registerModule(new SpammerModule());
      registerModule(new SpeedModule());
      registerModule(new SpeedMineModule());
      registerModule(new SprintModule());
      registerModule(new TargetHUDModule());
      registerModule(new TargetStrafeModule());
      registerModule(new TimerModule());
      registerModule(new TracersModule());
      registerModule(new TrajectoriesModule());
      registerModule(new VelocityModule());
      registerModule(new ViewClipModule());
      registerModule(new WTapModule());
      registerModule(new XrayModule());
   }

   private static void registerModule(Module module) {
      moduleManager.modules.put(module.getClass(), module);
      eventBus.register(module);
      ArrayList<Setting<?>> settings = new ArrayList<>();

      for (Field field : module.getClass().getFields()) {
         if (Setting.class.isAssignableFrom(field.getType())) {
            try {
               Setting<?> setting = (Setting<?>)field.get(module);
               if (setting != null) {
                  setting.setModule(module);
                  settings.add(setting);
               }
            } catch (IllegalAccessException var7) {
               throw new IllegalStateException("Unable to register setting " + field.getName(), var7);
            }
         }
      }

      settingManager.settingsByModule.put(module.getClass(), settings);
   }

   private static void registerCommands() {
      commandManager.commands.add(new BindCommand());
      commandManager.commands.add(new ConfigCommand());
      commandManager.commands.add(new DenickCommand());
      commandManager.commands.add(new EnemyCommand());
      commandManager.commands.add(new FriendCommand());
      commandManager.commands.add(new GuiCommand());
      commandManager.commands.add(new HelpCommand());
      commandManager.commands.add(new HideCommand());
      commandManager.commands.add(new ItemNameCommand());
      commandManager.commands.add(new ModuleListCommand());
      commandManager.commands.add(new ModuleSettingsCommand());
      commandManager.commands.add(new PlayerListCommand());
      commandManager.commands.add(new ShowCommand());
      commandManager.commands.add(new ToggleCommand());
      commandManager.commands.add(new UsernameCommand());
      commandManager.commands.add(new VClipCommand());
   }
}
