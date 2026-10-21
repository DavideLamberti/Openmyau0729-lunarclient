package myau;

import java.awt.Color;
import org.lwjgl.opengl.GL20;

public class ColorMaskShader extends ShaderProgram {
   private static final String FRAGMENT_SHADER_SOURCE = "#version 120\nuniform sampler2D texture;\nuniform vec4 color;\nvoid main() {\nvec4 st = texture2D(texture, gl_TexCoord[0].st);\ngl_FragColor = vec4(color.rgb, st.a > 0.0 ? color.a : 0.0);\n}\n";

   public ColorMaskShader() {
      super(
         "#version 120\nuniform sampler2D texture;\nuniform vec4 color;\nvoid main() {\nvec4 st = texture2D(texture, gl_TexCoord[0].st);\ngl_FragColor = vec4(color.rgb, st.a > 0.0 ? color.a : 0.0);\n}\n"
      );
   }

   @Override
   protected void registerUniforms() {
      this.registerUniform("texture");
      this.registerUniform("color");
   }

   @Override
   protected void updateUniforms() {
      GL20.glUniform1i(this.getUniformLocation("texture"), 0);
      GL20.glUniform4f(this.getUniformLocation("color"), 1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void setColor(Color color) {
      GL20.glUniform4f(
         this.getUniformLocation("color"),
         (float)color.getRed() / 255.0F,
         (float)color.getGreen() / 255.0F,
         (float)color.getBlue() / 255.0F,
         (float)color.getAlpha() / 255.0F
      );
   }
}
