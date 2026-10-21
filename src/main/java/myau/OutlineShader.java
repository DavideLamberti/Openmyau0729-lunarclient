package myau;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL20;

public class OutlineShader extends ShaderProgram {
   private static final Minecraft minecraft = Minecraft.getMinecraft();
   private static final String FRAGMENT_SHADER_SOURCE = "#version 120\nuniform sampler2D texture;\nuniform vec2 size;\nuniform float radius;\nvoid main(void) {\ngl_FragColor = vec4(0.0);\nvec4 xy = texture2D(texture, gl_TexCoord[0].xy);\nif (xy.a == 0.0) {\nfor (float x = -radius; x <= radius; x++) {\nfor (float y = -radius; y <= radius; y++) {\nvec4 color = texture2D(texture, gl_TexCoord[0].xy + vec2(size.x * x, size.y * y));\nif (color.a != 0.0) {\ngl_FragColor = color;\n}\n}\n}\n}\n}\n";

   public OutlineShader() {
      super(
         "#version 120\nuniform sampler2D texture;\nuniform vec2 size;\nuniform float radius;\nvoid main(void) {\ngl_FragColor = vec4(0.0);\nvec4 xy = texture2D(texture, gl_TexCoord[0].xy);\nif (xy.a == 0.0) {\nfor (float x = -radius; x <= radius; x++) {\nfor (float y = -radius; y <= radius; y++) {\nvec4 color = texture2D(texture, gl_TexCoord[0].xy + vec2(size.x * x, size.y * y));\nif (color.a != 0.0) {\ngl_FragColor = color;\n}\n}\n}\n}\n}\n"
      );
   }

   @Override
   protected void registerUniforms() {
      this.registerUniform("texture");
      this.registerUniform("size");
      this.registerUniform("radius");
   }

   @Override
   protected void updateUniforms() {
      GL20.glUniform1i(this.getUniformLocation("texture"), 0);
      GL20.glUniform2f(this.getUniformLocation("size"), 1.0F / (float)minecraft.displayWidth, 1.0F / (float)minecraft.displayHeight);
      GL20.glUniform1f(this.getUniformLocation("radius"), 1.5F);
   }
}
