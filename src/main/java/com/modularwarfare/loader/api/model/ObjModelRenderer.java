package com.modularwarfare.loader.api.model;


import com.modularwarfare.ModConfig;
import com.modularwarfare.client.fpp.basic.models.objects.CustomItemRenderer;
import com.modularwarfare.loader.ObjModel;
import com.modularwarfare.loader.part.ModelObject;
import com.modularwarfare.loader.part.Vertex;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class ObjModelRenderer {
    private static CustomItemRenderer customItemRenderer = new CustomItemRenderer();
    public static boolean glowTxtureMode = false;
    public static String glowType;
    public static String glowPath;
    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;
    public float rotateAngleX;
    public float rotateAngleY;
    public float rotateAngleZ;
    public boolean isHidden;
    public List<ObjModelRenderer> childModels = new ArrayList<>();
    private ModelObject model;
    private AbstractObjModel parent;
    private boolean glow = false;
    private int vertexCount = 0;
    private boolean compiled;
    /**
     * The GL display list rendered by the Tessellator for this model
     */
    private int displayList;

    public ObjModelRenderer(ObjModel parent, ModelObject modelForRender) {
        this.model = modelForRender;
        this.parent = parent;
        if (model.name.endsWith("_glow")) {
            glow = true;
        }
    }

    public String getName() {
        return model.name;
    }

    public void setRotationPoint(float rotationPointXIn, float rotationPointYIn, float rotationPointZIn) {
        this.rotationPointX = rotationPointXIn;
        this.rotationPointY = rotationPointYIn;
        this.rotationPointZ = rotationPointZIn;
    }

    public void setRotationPoint(Vertex vertex) {
        this.rotationPointX = vertex.x;
        this.rotationPointY = vertex.y;
        this.rotationPointZ = vertex.z;
    }

    /**
     * Adds child to Renderer.
     * After using this method you must call {@link ObjModel#clearDuplications()} method to delete all generated duplicates in {@link #parent}.
     * You MUST do this after adding all children to the renderer.
     */
    public void addChild(ObjModelRenderer child) {
        childModels.add(child);
        parent.addDuplication(child);
    }

    /**
     * Renders part with given {@code scale}.
     *
     * @param scale scaleFactor, that determines your part size.
     */
    @SideOnly(Side.CLIENT)
    public void render(float scale) {
        float x = OpenGlHelper.lastBrightnessX;
        float y = OpenGlHelper.lastBrightnessY;
        if (this.glow) {
            GlStateManager.disableLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
        }
        if (!this.isHidden) {
            if (!this.compiled) {
                if(ModConfig.INSTANCE.model_optimization){
                    this.compileVAO(scale);
                } else {
                    this.compileDisplayList(scale);
                }
            }

            if (this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F) {
                if(ModConfig.INSTANCE.model_optimization) {
                    callVAO();
                } else {
                    GlStateManager.callList(this.displayList);
                }
                if (this.childModels != null) {
                    for (ObjModelRenderer childModel : this.childModels) {
                        childModel.render(scale);
                    }
                }
            } else {
                GlStateManager.pushMatrix();

                GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale,
                        this.rotationPointZ * scale);

                if (this.rotateAngleZ != 0.0F) {

                    GlStateManager.rotate(this.rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
                }

                if (this.rotateAngleY != 0.0F) {
                    GlStateManager.rotate(-this.rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
                }

                if (this.rotateAngleX != 0.0F) {
                    GlStateManager.rotate(this.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
                }

                GlStateManager.translate(-this.rotationPointX * scale, -this.rotationPointY * scale,
                        -this.rotationPointZ * scale);

                if(ModConfig.INSTANCE.model_optimization) {
                    callVAO();
                } else {
                    GlStateManager.callList(this.displayList);
                }

                if (this.childModels != null) {
                    for (ObjModelRenderer childModel : this.childModels) {
                        childModel.render(scale);
                    }
                }

                GlStateManager.popMatrix();
            }
        }
        if (this.glow) {
            GlStateManager.disableLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, x, y);
        }
        if(glowTxtureMode) {
            if(!customItemRenderer.bindTextureGlow(glowType, glowPath)) {
                return;
            }
            glowTxtureMode=false;
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.depthFunc(GL11.GL_EQUAL);
            GlStateManager.disableLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
            render(scale);
            GlStateManager.enableLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, x, y);
            GlStateManager.depthFunc(GL11.GL_LEQUAL);
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
            glowTxtureMode=true;
            customItemRenderer.bindTexture(glowType, glowPath);
        }
    }

    /**
     * Renders part with given {@code scale} and rotation.
     *
     * @param scale scaleFactor, that determines your part size.
     */
    @SideOnly(Side.CLIENT)
    public void renderWithRotation(float scale) {
        if (!this.isHidden) {
            if (!this.compiled) {
                if(ModConfig.INSTANCE.model_optimization){
                    this.compileVAO(scale);
                } else {
                    this.compileDisplayList(scale);
                }
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale,
                    this.rotationPointZ * scale);

            if (this.rotateAngleY != 0.0F) {
                GlStateManager.rotate(this.rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
            }

            if (this.rotateAngleX != 0.0F) {
                GlStateManager.rotate(this.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
            }

            if (this.rotateAngleZ != 0.0F) {
                GlStateManager.rotate(this.rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
            }

            GlStateManager.translate(-this.rotationPointX * scale, -this.rotationPointY * scale,
                    -this.rotationPointZ * scale);

            if(ModConfig.INSTANCE.model_optimization) {
                callVAO();
            } else {
                GlStateManager.callList(this.displayList);
            }

            if (this.childModels != null) {
                for (ObjModelRenderer childModel : this.childModels) {
                    childModel.render(scale);
                }
            }
            GlStateManager.popMatrix();
        }
    }

    /**
     * Allows the changing of Angles after a box has been rendered
     */
    @SideOnly(Side.CLIENT)
    public void postRender(float scale) {
        if (!this.isHidden) {
            if (!this.compiled) {
                if(ModConfig.INSTANCE.model_optimization){
                    this.compileVAO(scale);
                } else {
                    this.compileDisplayList(scale);
                }
            }

            if (this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F) {
                if (this.rotationPointX != 0.0F || this.rotationPointY != 0.0F || this.rotationPointZ != 0.0F) {
                    GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale,
                            this.rotationPointZ * scale);
                }
            } else {
                GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale,
                        this.rotationPointZ * scale);

                if (this.rotateAngleZ != 0.0F) {
                    GlStateManager.rotate(this.rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
                }

                if (this.rotateAngleY != 0.0F) {
                    GlStateManager.rotate(this.rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
                }

                if (this.rotateAngleX != 0.0F) {
                    GlStateManager.rotate(this.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
                }
            }
        }
    }

    /**
     * Compiles a GL display list for this model
     */
    @SideOnly(Side.CLIENT)
    private void compileDisplayList(float scale) {
        this.displayList = GLAllocation.generateDisplayLists(1);
        GlStateManager.glNewList(this.displayList, GL11.GL_COMPILE);
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();

        model.render(bufferbuilder, scale);

        GlStateManager.glEndList();
        this.compiled = true;
    }

    @SideOnly(Side.CLIENT)
    private void compileVAO(float scale) {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        List<Float> list = new ArrayList<Float>();
        int flag = model.renderByVAO(list, scale);
        this.displayList = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(displayList);
        vertexCount = list.size() / flag;
        FloatBuffer pos_floatBuffer = BufferUtils.createFloatBuffer(vertexCount * 3);
        FloatBuffer tex_floatBuffer = BufferUtils.createFloatBuffer(vertexCount * 2);
        FloatBuffer normal_floatBuffer = BufferUtils.createFloatBuffer(vertexCount * 3);
        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            if (count == 8) {
                count = 0;
            }
            if (count < 3) {
                pos_floatBuffer.put(list.get(i));
            } else if (count < 5) {
                tex_floatBuffer.put(list.get(i));
            } else if (count < 8) {
                normal_floatBuffer.put(list.get(i));
            }
            count++;
        }
        pos_floatBuffer.flip();
        tex_floatBuffer.flip();
        normal_floatBuffer.flip();
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        int pos_vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, pos_vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, pos_floatBuffer, GL15.GL_STATIC_DRAW);
        GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);

        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        int tex_vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, tex_vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, tex_floatBuffer, GL15.GL_STATIC_DRAW);
        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);

        GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
        int normal_vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normal_vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normal_floatBuffer, GL15.GL_STATIC_DRAW);
        GL11.glNormalPointer(GL11.GL_FLOAT, 0, 0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
        GL15.glDeleteBuffers(pos_vbo);
        GL15.glDeleteBuffers(tex_vbo);
        GL15.glDeleteBuffers(normal_vbo);
        this.compiled = true;
    }

    private void callVAO() {
        GL30.glBindVertexArray(displayList);
        GL11.glDrawArrays(model.glDrawingMode, 0, vertexCount);
        GL30.glBindVertexArray(0);
    }
}