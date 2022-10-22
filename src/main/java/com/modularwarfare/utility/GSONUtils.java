package com.modularwarfare.utility;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.modularwarfare.ModularWarfare;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Type;

public class GSONUtils {

    @Nullable
    public static <T> T fromJson(Gson p_193838_0_, JsonReader jsonreader, Type p_193838_2_, String name) {
        try {
            return (T) p_193838_0_.getAdapter(TypeToken.get(p_193838_2_)).read(jsonreader);
        } catch (IOException ioexception) {
            ModularWarfare.LOGGER.warn("[ModularWarfare] JSON Error reading file: "+name);
            throw new JsonParseException(ioexception);
        }
    }


}
