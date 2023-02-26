package com.modularwarfare.client.input;

public enum KeyType {

    GunReload("Reload Gun", 0x13), // R
    ClientReload("Reload Client", 0x43), // F9
    DebugMode("Debug Mode", 0x44), // F10
    FireMode("Fire Mode", 0x2F), // V
    Inspect("Inspect", 0x31), // N
    GunUnload("Unload Key", 0x16), // U
    AddAttachment("Attachment Mode", 0x32), // M
    Flashlight("Flashlight", 0x23), // H
    Inventory("Open Inventory", 0x30), // B


    Left("Left (Attach mode)", 203), // H
    Right("Right (Attach mode)", 205), // H
    Up("Up (Attach mode)", 200), // H
    Down("Down (Attach mode)", 208); // H


    //Keyboard
    public String displayName;
    public int keyCode;

    KeyType(String displayName, int keyCode) {
        this.displayName = displayName;
        this.keyCode = keyCode;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

}
