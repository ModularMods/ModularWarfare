package com.modularwarfare.addon;

public class AddonLoadingException extends Exception {
    public String message;

    public AddonLoadingException(String msg) {
        this.message = msg;
    }
}
