package com.modularwarfare.common.guns;

import com.google.gson.annotations.SerializedName;

public enum WeaponScopeModeType {
    
    //历史遗留问题选项
    @Deprecated
    @SerializedName("dot")
    SIMPLE_DOT("dot", false, false, false, true),
    @SerializedName("simple")
    SIMPLE("simple", false, false, false),
    @SerializedName("normal")
    NORMAL("normal", false, true, true),
    @SerializedName("pip")
    PIP("pip", true, true, true);
    
    public String name;
    public boolean isPIP;
    public boolean isMirror;
    public boolean insideGunRendering;
    public boolean isDot=false;
    
    private WeaponScopeModeType(String name,boolean isPIP,boolean isMirror, boolean insideGunRendering,boolean isDot) {
        this.name=name;
        this.isPIP = isPIP;
        this.isMirror=isMirror;
        this.insideGunRendering = insideGunRendering;
        this.isDot=isDot;
    }
    
    private WeaponScopeModeType(String name,boolean isPIP,boolean isMirror, boolean insideGunRendering) {
        this(name, isPIP, isMirror, insideGunRendering, false);
    }
    
    
}
