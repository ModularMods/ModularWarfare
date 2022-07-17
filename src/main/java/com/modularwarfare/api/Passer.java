package com.modularwarfare.api;

public class Passer<T> {
    public T object;
    public Passer(T object) {
       this.object=object;
    }
    
    public Passer() {
        this(null);
    }
    
    public T get() {
        return object;
    }
    
    public void set(T object) {
        this.object=object;
    }
}
