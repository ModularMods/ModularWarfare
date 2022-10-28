/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.modularwarfare.mcgltf;

import net.minecraftforge.fml.client.GuiErrorBase;

public class GuiWrongOpenGL extends GuiErrorBase {

    public GuiWrongOpenGL() {
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        int offset = 75;
        this.drawCenteredString(this.fontRenderer, "Error, OpenGL incompatibility detected", this.width / 2, offset, 0xFFFFFF);
        this.drawCenteredString(this.fontRenderer, "OpenGL 3+ is needed to run MCglTF", this.width / 2, offset + 30, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
