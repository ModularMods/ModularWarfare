package com.modularwarfare.client.shader;

import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.*;

public class FrameBuffer {

	private int buffer;
	private int renderBuffer;
	
	private int width, height;
	
	private int[] textures;
	private int textureDepth;
	
	private final boolean hasRenderBuffer;
	private final boolean hasDepthTexture;
	boolean multipleTextures;
	
	public FrameBuffer(int width, int height, int textureCount, boolean hasRenderBuffer, boolean hasDepthTexture) {
		this.width = width;
		this.height = height;
		this.hasRenderBuffer = hasRenderBuffer;
		this.hasDepthTexture = hasDepthTexture;
		this.textures = new int[textureCount];
		delete();
		create();
	}
	
	public FrameBuffer(int width, int height) {
		this.width = width;
		this.height = height;
		this.hasRenderBuffer = false;
		this.hasDepthTexture = false;
		delete();
		create();
		init();
	}
	
	public void init() {
		bind(true);
		createTexture();
		glDrawBuffer(GL_COLOR_ATTACHMENT0);
		checkComplete();
		
		if(hasRenderBuffer) {
			generateRenderBuffer();
		}
		
		if(hasDepthTexture) {
			createDepthTexture();
		}
	}
	
	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
		deleteTextures();
	}
	
	public void create() {
		buffer = glGenFramebuffers();
	}
	
	public void bind(boolean viewport) {
		glBindFramebuffer(GL_FRAMEBUFFER, buffer);
		if(viewport) glViewport(0, 0, width, height);
	}
	
	public void unbind() {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	private void createTexture() {
		int texture = glGenTextures();
		textures = new int[1];
		textures[0] = texture;
		glBindTexture(GL_TEXTURE_2D, texture);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, width, height, 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);
	}
	
	public void createTexture(int index, int textureFormat, int format, int type, int colorAttachment, int wrap, int filter) {
		int texture = glGenTextures();
		textures[index] = texture;
		glBindTexture(GL_TEXTURE_2D, texture);
		glTexImage2D(GL_TEXTURE_2D, 0, textureFormat, width, height, 0, format, type, (ByteBuffer) null);
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrap);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrap);

		glFramebufferTexture2D(GL_FRAMEBUFFER, colorAttachment, GL_TEXTURE_2D, texture, 0);
	}
	
	public void setTexture(int texture, int colorAttachment) {
		glFramebufferTexture2D(GL_FRAMEBUFFER, colorAttachment, GL_TEXTURE_2D, texture, 0);
	}
	
	private void createDepthTexture() {
		textureDepth = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, textureDepth);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, textureDepth, 0);
	}
	
	private void generateRenderBuffer() {
		renderBuffer = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, renderBuffer);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderBuffer);
		
		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
			Throwable t = new Throwable("Render buffer couldn't create!");
			CrashReport crashreport = CrashReport.makeCrashReport(t, "Shader compile");
			throw new ReportedException(crashreport);
		}
	}
	
	public void bindTexture(int index) {
		glBindTexture(GL_TEXTURE_2D, textures[index]);
	}
	
	public void bindTexture() {
		glBindTexture(GL_TEXTURE_2D, textures[0]);
	}
	
	public int getTexture(int index) {
		return textures[index];
	}
	
	public int getTexture() {
		return textures[0];
	}
	
	private void checkComplete() {
		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
			Throwable t = new Throwable("MRT couldn't create!");
			CrashReport crashreport = CrashReport.makeCrashReport(t, "Shader compile");
			throw new ReportedException(crashreport);
		}
	}
	
	public void clear() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	public void viewPort(int width, int height) {
		glViewport(0, 0, width, height);
	}

	public void delete() {
		deleteTextures();
		if(hasDepthTexture) glDeleteTextures(textureDepth);
		if(hasRenderBuffer) glDeleteRenderbuffers(renderBuffer);
		glDeleteFramebuffers(buffer);
	}
	
	private void deleteTextures() {
		if(textures != null)
			for (int texture : textures) {
				glDeleteTextures(texture);
			}
	}
	
	public void drawBuffer(int i) {
		glDrawBuffer(GL_COLOR_ATTACHMENT0 + i);
	}
	
	public int getBuffer() {
		return buffer;
	}
	
	public int getRenderBuffer() {
		return renderBuffer;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getTextureDepth() {
		return textureDepth;
	}
}
