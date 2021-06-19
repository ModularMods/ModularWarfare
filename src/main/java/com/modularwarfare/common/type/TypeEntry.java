package com.modularwarfare.common.type;

import java.util.function.BiConsumer;

public class TypeEntry {

    public String name;
    public Class<? extends BaseType> typeClass;
    public int id;
    public BiConsumer<BaseType, Boolean> typeAssignFunction;

    public TypeEntry(String name, Class<? extends BaseType> typeClass, int id, BiConsumer<BaseType, Boolean> typeAssignFunction) {
        this.name = name;
        this.typeClass = typeClass;
        this.id = id;
        this.typeAssignFunction = typeAssignFunction;
    }

    @Override
    public String toString() {
        return name;
    }

}
