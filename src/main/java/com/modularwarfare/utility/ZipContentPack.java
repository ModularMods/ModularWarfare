package com.modularwarfare.utility;

import com.modularwarfare.loader.ObjModel;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import java.util.HashMap;
import java.util.List;

public class ZipContentPack {

    public String contentPack;

    public List<FileHeader> fileHeaders;

    public ZipFile zipFile;

    public HashMap<String, ObjModel> models_cache = new HashMap<>();

    public ZipContentPack(String contentPack, List<FileHeader> fileHeaders, ZipFile zipFile) {
        this.contentPack = contentPack;
        this.fileHeaders = fileHeaders;
        this.zipFile = zipFile;
    }


    public ZipFile getZipFile() {
        return zipFile;
    }
}
