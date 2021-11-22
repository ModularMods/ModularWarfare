package com.modularwarfare.client.killchat;

public class KillFeedEntry {
    private String text;
    private boolean causeIsGun = false;
    private String weaponInternalName;
    private int timeLived;
    private int timeLiving;

    public KillFeedEntry(final String text, int timeLiving, String weaponInternalName) {
        this.text = text;
        this.timeLiving = timeLiving;

        if (weaponInternalName != null) {
            this.weaponInternalName = weaponInternalName;
            this.causeIsGun = true;
        }
    }

    public int incrementTimeLived() {
        return this.timeLived++;
    }

    public boolean isCausedByGun() {
        return this.causeIsGun;
    }

    public String getWeaponInternalName() {
        return this.weaponInternalName;
    }

    public int getTimeLiving() {
        return this.timeLiving;
    }

    public int getTimeLived() {
        return this.timeLived;
    }

    public String getText() {
        return this.text;
    }
}
