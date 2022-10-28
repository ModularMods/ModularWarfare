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

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IDisplayableError;
import net.minecraftforge.fml.common.EnhancedRuntimeException;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class OpenGLNotSupportedException extends EnhancedRuntimeException implements IDisplayableError
{
    public OpenGLNotSupportedException() {
    }


    @Override
    public String getMessage()
    {
        return String.format("OpenGL 3+ is needed to run MCglTF, your graphic card does not support this version.");
    }

    @Override
    protected void printStackTrace(WrappedPrintStream stream) {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen createGui()
    {
        return new GuiWrongOpenGL();
    }

}
