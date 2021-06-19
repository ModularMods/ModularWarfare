package com.modularwarfare.client.export;

public class ItemModelExport {

    public String parent = "item/generated";
    public TextureLayers textures = new TextureLayers();
    public Display display = new Display();

    public void setBaseLayer(String path) {
        textures.layer0 += path;
    }

    public static class TextureLayers {
        public String layer0 = "modularwarfare:items/";
    }

    public static class Display {

        public DisplayType thirdperson_lefthand = new DisplayType();
        public DisplayType thirdperson_righthand = new DisplayType();

        public static class DisplayType {
            public float[] scale = {0, 0, 0};
        }

    }

}