package com.modularwarfare.api;


import com.modularwarfare.client.fpp.basic.renderers.RenderGunStatic;

import net.minecraft.util.EnumHandSide;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class RenderHandFisrtPersonEvent extends Event{
    public final RenderGunStatic renderGunStatic;
    public final EnumHandSide side;

    public RenderHandFisrtPersonEvent(RenderGunStatic renderGunStatic, EnumHandSide side) {
        super();
        this.renderGunStatic = renderGunStatic;
        this.side = side;
    }

    @Cancelable
    public static class Pre extends RenderHandFisrtPersonEvent{

        public Pre(RenderGunStatic renderGunStatic, EnumHandSide side) {
            super(renderGunStatic, side);
            // TODO Auto-generated constructor stub
        }

    }

    public static class Post extends RenderHandFisrtPersonEvent{

        public Post(RenderGunStatic renderGunStatic, EnumHandSide side) {
            super(renderGunStatic, side);
            // TODO Auto-generated constructor stub
        }

    }

}