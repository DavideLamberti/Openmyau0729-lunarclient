package myau;

import java.util.HashMap;
import java.util.Map;
import org.lwjgl.opengl.GL20;

public abstract class ShaderProgram {
   private static final String VERTEX_SHADER_SOURCE = "#version 120\nvoid main(void) {\ngl_TexCoord[0] = gl_MultiTexCoord0;\ngl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n}\n";
   private final Map<String, Integer> uniformLocations = new HashMap<>();
   private int programId;

   protected ShaderProgram(String fragmentShaderSource) {
      this.createProgram(fragmentShaderSource);
   }

   private int compileShader(String source, int shaderType) {
      int shaderId = GL20.glCreateShader(shaderType);
      GL20.glShaderSource(shaderId, source);
      GL20.glCompileShader(shaderId);
      return GL20.glGetShaderi(shaderId, 35713) == 0 ? -1 : shaderId;
   }

   private void createProgram(String fragmentShaderSource) {
      this.programId = GL20.glCreateProgram();
      GL20.glAttachShader(
         this.programId,
         this.compileShader(
            "#version 120\nvoid main(void) {\ngl_TexCoord[0] = gl_MultiTexCoord0;\ngl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n}\n", 35633
         )
      );
      GL20.glAttachShader(this.programId, this.compileShader(fragmentShaderSource, 35632));
      GL20.glLinkProgram(this.programId);
      if (GL20.glGetProgrami(this.programId, 35714) == 0) {
         this.programId = -1;
      } else {
         this.registerUniforms();
      }
   }

   protected final int getUniformLocation(String name) {
      return this.uniformLocations.get(name);
   }

   protected final void registerUniform(String name) {
      this.uniformLocations.put(name, GL20.glGetUniformLocation(this.programId, name));
   }

   protected abstract void registerUniforms();

   protected abstract void updateUniforms();

   public final void use() {
      GL20.glUseProgram(this.programId);
      this.updateUniforms();
   }

   public final void stop() {
      GL20.glUseProgram(0);
   }
}
