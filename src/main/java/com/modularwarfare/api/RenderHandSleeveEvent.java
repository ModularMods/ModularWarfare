package com.modularwarfare.api;


import com.modularwarfare.client.fpp.basic.renderers.RenderGunStatic;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public abstract class RenderHandSleeveEvent extends Event{
    public final RenderGunStatic renderGunStatic;
    public final EnumHandSide side;
    public final ModelBiped modelplayer;


    public RenderHandSleeveEvent(RenderGunStatic render,EnumHandSide side, ModelBiped modelplayer) {
        this.renderGunStatic = render;
        this.side = side;
        this.modelplayer = modelplayer;
    }


    @Cancelable
    public static class Pre extends RenderHandSleeveEvent{

        public Pre(RenderGunStatic render, EnumHandSide side, ModelBiped modelplayer) {
            super(render, side, modelplayer);
            // TODO Auto-generated constructor stub
        }


    }

    public static  class Post extends RenderHandSleeveEvent{

        public Post(RenderGunStatic render, EnumHandSide side, ModelBiped modelplayer) {
            super(render, side, modelplayer);
            // TODO Auto-generated constructor stub
        }


    }
}