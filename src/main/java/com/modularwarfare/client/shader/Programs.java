package com.modularwarfare.client.shader;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;

public class Programs {
    public static int normalProgram;
    public static int overlayProgram;
    public static int scopeBorderProgram;
    public static int depthProgram;
    public static int sunglassesProgram;
    public static int alphaDepthProgram;
    
    public static void init() {
        int normalvshader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(normalvshader,
                  "#version 120\r\n"
                  + "varying vec4 color;\r\n"
                  + "varying vec2 texcoord;\r\n"
                  + "\r\n"
                  + "void main()\r\n"
                  + "{\r\n"
                  + "    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).st;\r\n"
                  + "    color = gl_Color;\r\n"
                  + "    gl_Position = gl_ProjectionMatrix  * gl_ModelViewMatrix * gl_Vertex;"
                  + "}");
        GL20.glCompileShader(normalvshader);
        if(GL20.glGetShaderi(normalvshader, GL20.GL_COMPILE_STATUS)!=GL11.GL_TRUE) {
            throw new RuntimeException(GL20.glGetShaderInfoLog(normalvshader, 512));
        }
        
        int normalfshader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(normalfshader,
                "#version 120\r\n"
                + "\r\n"
                + "uniform sampler2D texture;\r\n"
                + "varying vec4 color;\r\n"
                + "varying vec2 texcoord;\r\n"
                + "void main(){\r\n"
                + "    vec4 src=texture2D(texture, texcoord.st);\r\n"
                + "    gl_FragColor = src*color;\r\n"
                + "}");
        GL20.glCompileShader(normalfshader);
        if(GL20.glGetShaderi(normalfshader, GL20.GL_COMPILE_STATUS)!=GL11.GL_TRUE) {
            throw new RuntimeException(GL20.glGetShaderInfoLog(normalfshader, 512));
        }
        
        normalProgram = GL20.glCreateProgram();
        GL20.glAttachShader(normalProgram, normalvshader);
        GL20.glAttachShader(normalProgram, normalfshader);
        GL20.glDeleteShader(normalvshader);
        GL20.glDeleteShader(normalfshader);
        GL20.glLinkProgram(normalProgram);
        if(GL20.glGetProgrami(normalProgram, GL20.GL_LINK_STATUS)!=GL11.GL_TRUE) {
            throw new RuntimeException(GL20.glGetProgramInfoLog(normalProgram, 512));
        }
        
        GL20.glUseProgram(normalProgram);
        GL20.glUniform1i(GL20.glGetUniformLocation(normalProgram, "texture"), 0);
        GL20.glUseProgram(0);
        
        int depthvshader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(depthvshader,
                  "#version 120\r\n"
                  + "varying vec4 color;\r\n"
                  + "varying vec2 texcoord;\r\n"
                  + "\r\n"
                  + "void main()\r\n"
                  + "{\r\n"
                  + "    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).st;\r\n"
                  + "    color = gl_Color;\r\n"
                  + "    gl_Position = gl_ProjectionMatrix  * gl_ModelViewMatrix * gl_Vertex;"
                  + "}");
        GL20.glCompileShader(depthvshader);
        if(GL20.glGetShaderi(depthvshader, GL20.GL_COMPILE_STATUS)!=GL11.GL_TRUE) {
            throw new RuntimeException(GL20.glGetShaderInfoLog(depthvshader, 512));
        }
        
        int depthfshader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(depthfshader,
                "#version 120\r\n"
                + "\r\n"
                + "uniform sampler2D texture;\r\n"
                + "varying vec4 color;\r\n"
                + "varying vec2 texcoord;\r\n"
                + "void main(){\r\n"
                + "    vec4 src=texture2D(texture, texcoord.st);\r\n"
                + "    gl_FragColor=vec4(src.r,src.r,src.r,1.0);\r\n"
                + "    gl_FragDepth=src.r;\r\n"
                + "}");
        GL20.glCompileShader(depthfshader);
        if(GL20.glGetShaderi(depthfshader, GL20.GL_COMPILE_STATUS)!=GL11.GL_TRUE) {
            throw new RuntimeException(GL20.glGetShaderInfoLog(depthfshader, 512));
        }
        
        depthProgram = GL20.glCreateProgram();
        GL20.glAttachShader(depthProgram, depthvshader);
        GL20.glAttachShader(depthProgram, depthfshader);
        GL20.glDeleteShader(depthvshader);
        GL20.glDeleteShader(depthfshader);
        GL20.glLinkProgram(depthProgram);
        if(GL20.glGetProgrami(depthProgram, GL20.GL_LINK_STATUS)!=GL11.GL_TRUE) {
            throw new RuntimeException(GL20.glGetProgramInfoLog(depthProgram, 512));
        }
        
        GL20.glUseProgram(depthProgram);
        GL20.glUniform1i(GL20.glGetUniformLocation(depthProgram, "texture"), 0);
        GL20.glUseProgram(0);
        
        int overlayvshader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(overlayvshader,
                  "#version 120\r\n"
                  + "varying vec4 color;\r\n"
                  + "varying vec2 texcoord;\r\n"
                  + "\r\n"
                  + "void main()\r\n"
                  + "{\r\n"
                  + "    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).st;\r\n"
                  + "    color = gl_Color;\r\n"
                  + "    gl_Position = gl_ProjectionMatrix  * gl_ModelViewMatrix * gl_Vertex;"
                  + "}");
        GL20.glCompileShader(overlayvshader);
        if(GL20.glGetShaderi(overlayvshader, GL20.GL_COMPILE_STATUS)!=GL11.GL_TRUE) {
            throw new RuntimeException(GL20.glGetShaderInfoLog(overlayvshader, 512));
        }
        
        int overlayfshader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(overlayfshader,
                "#version 120\r\n"
                + "\r\n"
                + "uniform sampler2D texture;\r\n"
                + "uniform sampler2D texture1;\r\n"
                + "uniform sampler2D texture2;\r\n"
                + "uniform vec2 size;\r\n"
                + "varying vec4 color;\r\n"
                + "varying vec2 texcoord;\r\n"
                + "void main(){\r\n"
                + "    vec4 src=texture2D(texture, texcoord.st);//scope color\r\n"
                + "    vec4 src1=texture2D(texture1, texcoord.st);//world color\r\n"
                + "    vec4 src2=texture2D(texture2, texcoord.st);//lightmap color\r\n"
                + "    src.rgb=src.rgb*min(vec3(1,1,1),src1.rgb+src2.rgb)*src.a+src1.rgb*(1-src.a);\r\n"
                + "\r\n"
                + "    float srcWeight=1;\r\n"
                + "    float range=1;\r\n"
                + "    vec4 albedo = vec4(0,0,0,0);\r\n"
                + "    float sum=0;\r\n"
                + "    vec4 samp;\r\n"
                + "    vec4 samp1;\r\n"
                + "    vec4 samp2;\r\n"
                + "    vec2 uv;\r\n"
                + "    for(int x=-1;x<=1;x++){\r\n"
                + "        for(int y=-1;y<=1;y++){\r\n"
                + "            uv=texcoord.st+vec2(range*x/size.x,range*y/size.y);\r\n"
                + "            samp=texture2D(texture,uv);\r\n"
                + "            samp1=texture2D(texture1,uv);\r\n"
                + "            samp2=texture2D(texture2,uv);\r\n"
                + "            samp.rgb=samp.rgb*min(vec3(1,1,1),samp1.rgb+samp2.rgb)*samp.a;\r\n"
                + "            albedo+=samp;\r\n"
                + "            sum+=1-step(samp.a,0);\r\n"
                + "        }   \r\n"
                + "    }\r\n"
                + "    albedo=albedo/sum;\r\n"
                + "    float flag=1-step(5,sum); \r\n"
                + "    flag*=min(sum,1);\r\n"
                + "    albedo.a=flag*(9-sum)*0.11;\r\n"
                + "    gl_FragColor = (src*(1-albedo.a)+albedo*albedo.a)*color;\r\n"
                + "    gl_FragColor.a = step(0.1,gl_FragColor.a);\r\n"
                + "}");
        GL20.glCompileShader(overlayfshader);
        if(GL20.glGetShaderi(overlayfshader, GL20.GL_COMPILE_STATUS)!=GL11.GL_TRUE) {
            throw new RuntimeException(GL20.glGetShaderInfoLog(overlayfshader, 512));
        }
        
        overlayProgram = GL20.glCreateProgram();
        GL20.glAttachShader(overlayProgram, overlayvshader);
        GL20.glAttachShader(overlayProgram, overlayfshader);
        GL20.glDeleteShader(overlayvshader);
        GL20.glDeleteShader(overlayfshader);
        GL20.glLinkProgram(overlayProgram);
        if(GL20.glGetProgrami(overlayProgram, GL20.GL_LINK_STATUS)!=GL11.GL_TRUE) {
            throw new RuntimeException(GL20.glGetProgramInfoLog(overlayProgram, 512));
        }
        
        GL20.glUseProgram(overlayProgram);
        GL20.glUniform1i(GL20.glGetUniformLocation(overlayProgram, "texture"), 0);
        GL20.glUniform1i(GL20.glGetUniformLocation(overlayProgram, "texture1"), 3);
        GL20.glUniform1i(GL20.glGetUniformLocation(overlayProgram, "texture2"), 4);
        GL20.glUseProgram(0);
        
        int scopeBorderShader_V = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(scopeBorderShader_V,
                  "#version 120\r\n"
                  + "varying vec4 color;\r\n"
                  + "varying vec2 texcoord;\r\n"
                  + "\r\n"
                  + "void main()\r\n"
                  + "{\r\n"
                  + "    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).st;\r\n"
                  + "    color = gl_Color;\r\n"
                  + "    gl_Position = gl_ProjectionMatrix  * gl_ModelViewMatrix * gl_Vertex;"
                  + "}");
        GL20.glCompileShader(scopeBorderShader_V);
        if(GL20.glGetShaderi(scopeBorderShader_V, GL20.GL_COMPILE_STATUS)!=GL11.GL_TRUE) {
            throw new RuntimeException(GL20.glGetShaderInfoLog(scopeBorderShader_V, 512));
        }
        
        int scopeBorderShader_F=GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(scopeBorderShader_F,
                "#version 120\r\n"
                + "uniform sampler2D texture;\r\n"
                + "uniform vec2 size;\r\n"
                + "varying vec2 texcoord;\r\n"
                + "varying vec4 color;\r\n"
                + "uniform float maskRange;\r\n"
                + "//uniform float borderRange;\r\n"
                + "uniform float drawRange;\r\n"
                + "uniform float strength;\r\n"
                + "uniform float scaleRangeY;\r\n"
                + "uniform float scaleStrengthY;\r\n"
                + "uniform float verticality;\r\n"
                + "\r\n"
                + "void main(){\r\n"
                + "    float maskDir=length(vec2(maskRange,maskRange));\r\n"
                + "    vec2 drawDir=vec2(drawRange,drawRange);\r\n"
                + "    vec2 offset=texcoord.st-vec2(0.5,0.5);\r\n"
                + "    vec2 offsetDir=vec2(abs(offset.x*(size.x/size.y)),abs(offset.y));\r\n"
                + "    drawDir=normalize(offsetDir)*length(drawDir)*(1-verticality)+drawDir*verticality;\r\n"
                + "    offsetDir.x=max(0,offsetDir.x-drawDir.x);\r\n"
                + "    offsetDir.y=max(0,offsetDir.y-drawDir.y*scaleRangeY);\r\n"
                + "    float len=length(offsetDir);\r\n"
                + "    offset.x*=max(0,len)*strength;\r\n"
                + "    offset.y*=max(0,len)*strength*scaleStrengthY;\r\n"
                + "    vec4 albedo = texture2D(texture, texcoord.st+offset);\r\n"
                + "    /*\r\n"
                + "    float a=1;\r\n"
                + "    a *= texture2D(texture, texcoord.st+vec2(borderRange*(size.x/size.y),0.0)).a;\r\n"
                + "    a *= texture2D(texture, texcoord.st+vec2(-borderRange*(size.x/size.y),0.0)).a;\r\n"
                + "    a *= texture2D(texture, texcoord.st+vec2(0.0,borderRange)).a;\r\n"
                + "    a *= texture2D(texture, texcoord.st+vec2(0.0,-borderRange)).a;\r\n"
                + "    albedo.a = a;\r\n"
                + "    */\r\n"
                + "    albedo.rgb*=step(length(offsetDir),maskDir);\r\n"
                + "    gl_FragColor = albedo * color;\r\n"
                + "}");
      GL20.glCompileShader(scopeBorderShader_F);
      if(GL20.glGetShaderi(scopeBorderShader_F, GL20.GL_COMPILE_STATUS)!=GL11.GL_TRUE) {
          throw new RuntimeException(GL20.glGetShaderInfoLog(scopeBorderShader_F, 512));
      }
      
      scopeBorderProgram = GL20.glCreateProgram();
      GL20.glAttachShader(scopeBorderProgram, scopeBorderShader_V);
      GL20.glAttachShader(scopeBorderProgram, scopeBorderShader_F);
      GL20.glDeleteShader(scopeBorderShader_V);
      GL20.glDeleteShader(scopeBorderShader_F);
      GL20.glLinkProgram(scopeBorderProgram);
      if(GL20.glGetProgrami(scopeBorderProgram, GL20.GL_LINK_STATUS)!=GL11.GL_TRUE) {
          throw new RuntimeException(GL20.glGetProgramInfoLog(scopeBorderProgram, 512));
      }
      
      GL20.glUseProgram(scopeBorderProgram);
      GL20.glUniform1i(GL20.glGetUniformLocation(scopeBorderProgram, "texture"), 0);
      GL20.glUseProgram(0);
      
      int sunglassesvshader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
      GL20.glShaderSource(sunglassesvshader,
                "#version 120\r\n"
                + "varying vec4 color;\r\n"
                + "varying vec2 texcoord;\r\n"
                + "\r\n"
                + "void main()\r\n"
                + "{\r\n"
                + "    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).st;\r\n"
                + "    color = gl_Color;\r\n"
                + "    gl_Position = gl_ProjectionMatrix  * gl_ModelViewMatrix * gl_Vertex;"
                + "}");
      GL20.glCompileShader(sunglassesvshader);
      if(GL20.glGetShaderi(sunglassesvshader, GL20.GL_COMPILE_STATUS)!=GL11.GL_TRUE) {
          throw new RuntimeException(GL20.glGetShaderInfoLog(sunglassesvshader, 512));
      }
      
      int sunglassesfshader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
      GL20.glShaderSource(sunglassesfshader,
              "#version 120\r\n"
              + "\r\n"
              + "uniform sampler2D texture;\r\n"
              + "varying vec4 color;\r\n"
              + "varying vec2 texcoord;\r\n"
              + "void main(){\r\n"
              + "    vec4 src=texture2D(texture, texcoord.st);\r\n"
              + "    float flag=1.0-step(src.a,0);\r\n"
              + "    gl_FragColor=vec4(1.0,0.0,0.0,src.a);\r\n"
              + "    gl_FragDepth=0;\r\n"
              + "}");
      GL20.glCompileShader(sunglassesfshader);
      if(GL20.glGetShaderi(sunglassesfshader, GL20.GL_COMPILE_STATUS)!=GL11.GL_TRUE) {
          throw new RuntimeException(GL20.glGetShaderInfoLog(sunglassesfshader, 512));
      }
      
      sunglassesProgram = GL20.glCreateProgram();
      GL20.glAttachShader(sunglassesProgram, sunglassesvshader);
      GL20.glAttachShader(sunglassesProgram, sunglassesfshader);
      GL20.glDeleteShader(sunglassesvshader);
      GL20.glDeleteShader(sunglassesfshader);
      GL20.glLinkProgram(sunglassesProgram);
      if(GL20.glGetProgrami(sunglassesProgram, GL20.GL_LINK_STATUS)!=GL11.GL_TRUE) {
          throw new RuntimeException(GL20.glGetProgramInfoLog(sunglassesProgram, 512));
      }
      
      GL20.glUseProgram(sunglassesProgram);
      GL20.glUniform1i(GL20.glGetUniformLocation(sunglassesProgram, "texture"), 0);
      GL20.glUseProgram(0);
      
      int alphaDepthvshader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
      GL20.glShaderSource(alphaDepthvshader,
                "#version 120\r\n"
                + "varying vec4 color;\r\n"
                + "varying vec2 texcoord;\r\n"
                + "\r\n"
                + "void main()\r\n"
                + "{\r\n"
                + "    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).st;\r\n"
                + "    color = gl_Color;\r\n"
                + "    gl_Position = gl_ProjectionMatrix  * gl_ModelViewMatrix * gl_Vertex;"
                + "}");
      GL20.glCompileShader(alphaDepthvshader);
      if(GL20.glGetShaderi(alphaDepthvshader, GL20.GL_COMPILE_STATUS)!=GL11.GL_TRUE) {
          throw new RuntimeException(GL20.glGetShaderInfoLog(alphaDepthvshader, 512));
      }
      
      int alphaDepthfshader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
      GL20.glShaderSource(alphaDepthfshader,
              "#version 120\r\n"
              + "\r\n"
              + "uniform sampler2D texture;//depth\r\n"
              + "uniform sampler2D textureMask;//mask\r\n"
              + "varying vec4 color;\r\n"
              + "varying vec2 texcoord;\r\n"
              + "void main(){\r\n"
              + "    vec4 depth=texture2D(texture, texcoord.st);\r\n"
              + "    vec4 mask=texture2D(textureMask, texcoord.st);\r\n"
              + "    gl_FragDepth = depth.r;\r\n"
              + "    gl_FragColor = mask;\r\n"
              + "}");
      GL20.glCompileShader(alphaDepthfshader);
      if(GL20.glGetShaderi(alphaDepthfshader, GL20.GL_COMPILE_STATUS)!=GL11.GL_TRUE) {
          throw new RuntimeException(GL20.glGetShaderInfoLog(alphaDepthfshader, 512));
      }
      
      alphaDepthProgram = GL20.glCreateProgram();
      GL20.glAttachShader(alphaDepthProgram, alphaDepthvshader);
      GL20.glAttachShader(alphaDepthProgram, alphaDepthfshader);
      GL20.glDeleteShader(alphaDepthvshader);
      GL20.glDeleteShader(alphaDepthfshader);
      GL20.glLinkProgram(alphaDepthProgram);
      if(GL20.glGetProgrami(alphaDepthProgram, GL20.GL_LINK_STATUS)!=GL11.GL_TRUE) {
          throw new RuntimeException(GL20.glGetProgramInfoLog(alphaDepthProgram, 512));
      }
      
      GL20.glUseProgram(alphaDepthProgram);
      GL20.glUniform1i(GL20.glGetUniformLocation(alphaDepthProgram, "texture"), 0);
      GL20.glUniform1i(GL20.glGetUniformLocation(alphaDepthProgram, "textureMask"), 3);
      GL20.glUseProgram(0);
    }
}
