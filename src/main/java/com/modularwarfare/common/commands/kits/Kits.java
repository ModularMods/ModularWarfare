package com.modularwarfare.common.commands.kits;

import java.util.ArrayList;

public class Kits {

    public ArrayList<Kit> kits;

    public Kits() {
        this.kits = new ArrayList<Kit>();
    }

    public static class Kit {
        public String name;
        public String data;
        public String backpack;
        public String vest;
        public boolean force = true;
    }

}
