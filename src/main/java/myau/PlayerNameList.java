package myau;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;
import myau.mixin.AccessorMinecraft;
import net.minecraft.client.Minecraft;

public abstract class PlayerNameList {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private static final String CREATED_LOG_MESSAGE = "Created: {}";
   private final ArrayList<String> entries = new ArrayList<>();
   private final File file;
   private final Color color;

   protected PlayerNameList(File file, Color color) {
      this.file = file;
      this.color = color;
   }

   public void load() {
      if (!this.file.exists()) {
         try {
            Files.createDirectories(this.file.toPath().getParent());
            if (this.file.createNewFile()) {
               ((AccessorMinecraft)minecraft).getLogger().info("Created: {}", new Object[]{this.file.getName()});
            }
         } catch (Exception var32) {
            ((AccessorMinecraft)minecraft).getLogger().error(var32.getMessage());
         }
      }

      try (
         FileReader input = new FileReader(this.file);
         BufferedReader reader = new BufferedReader(input);
      ) {
         this.entries.addAll(reader.lines().map(String::trim).collect(Collectors.toList()));
      } catch (Exception var36) {
         ((AccessorMinecraft)minecraft).getLogger().error(var36.getMessage());
      }
   }

   public void save() {
      try (
         FileWriter writer = new FileWriter(this.file);
         PrintWriter output = new PrintWriter(writer);
      ) {
         output.print(String.join("\n", this.entries));
      } catch (Exception var33) {
         ((AccessorMinecraft)minecraft).getLogger().error(var33.getMessage());
      }
   }

   public String add(String name) {
      if (this.contains(name)) {
         return null;
      } else {
         this.entries.add(name);
         this.save();
         return name;
      }
   }

   public String remove(String name) {
      Iterator<String> iterator = this.entries.iterator();

      while (iterator.hasNext()) {
         String entry = iterator.next();
         if (entry.equalsIgnoreCase(name)) {
            iterator.remove();
            this.save();
            return entry;
         }
      }

      return null;
   }

   public void clear() {
      this.entries.clear();
      this.save();
   }

   public boolean contains(String name) {
      return this.entries.stream().anyMatch(entry -> entry.equalsIgnoreCase(name));
   }

   public ArrayList<String> entries() {
      return this.entries;
   }

   public Color color() {
      return this.color;
   }
}
