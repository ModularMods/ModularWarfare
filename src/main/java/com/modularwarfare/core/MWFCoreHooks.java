package com.modularwarfare.core;

import com.modularwarfare.client.ClientProxy;

public class MWFCoreHooks {
    public static void onRender0() {
        ClientProxy.scopeUtils.onPreRenderHand0();
    }
    
    public static void onRender1() {
        ClientProxy.scopeUtils.onPreRenderHand1();
    }
}
