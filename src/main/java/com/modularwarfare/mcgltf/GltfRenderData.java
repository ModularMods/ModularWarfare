/*
 * www.javagl.de - JglTF
 *
 * Copyright 2015-2016 Marco Hutter - http://www.javagl.de
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package com.modularwarfare.mcgltf;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL40;

/**
 * A class maintaining the data for rendering a glTF with OpenGL.<br>
 * <br>
 * The actual creation of these objects will be done lazily when
 * the objects are requested for the first time. The methods of this class
 * may only be called from the OpenGL thread (when the GL context
 * is current). 
 */
public class GltfRenderData
{
	/**
	 * The GL vertex array objects
	 */
	private final List<Integer> glVertexArrays;
	
	private final List<Integer> glBufferViews;
	
	private final List<Integer> glTextures;
	
	private final List<Integer> glTransformFeedbacks;
	
	/**
	 * Create the render data
	 */
	GltfRenderData() {
		this.glVertexArrays = new ArrayList<Integer>();
		this.glBufferViews = new ArrayList<Integer>();
		this.glTextures = new ArrayList<Integer>();
		this.glTransformFeedbacks = new ArrayList<Integer>();
	}

	/**
	 * Add the given GL vertex array 
	 * 
	 * @param glVertexArray The GL vertex array
	 */
	void addGlVertexArray(int glVertexArray) {
		glVertexArrays.add(glVertexArray);
	}
	
	void addGlBufferView(int glBufferView) {
		glBufferViews.add(glBufferView);
	}
	
	void addGlTexture(int glTexture) {
		glTextures.add(glTexture);
	}
	
	void addGlTransformFeedback(int glTransformFeedback) {
		glTransformFeedbacks.add(glTransformFeedback);
	}
	
	/**
	 * Delete all GL resources that have been created internally.
	 */
	public void delete()
	{
		glVertexArrays.forEach((glVertexArray) -> GL30.glDeleteVertexArrays(glVertexArray));
		glBufferViews.forEach((glBufferView) -> GL15.glDeleteBuffers(glBufferView));
		glTextures.forEach((glTexture) -> GL11.glDeleteTextures(glTexture));
		glTransformFeedbacks.forEach((glTransformFeedback) -> GL40.glDeleteTransformFeedbacks(glTransformFeedback));
	}
	
}
