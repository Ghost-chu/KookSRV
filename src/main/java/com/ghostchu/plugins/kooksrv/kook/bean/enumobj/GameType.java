package com.ghostchu.plugins.kooksrv.kook.bean.enumobj;

import org.jetbrains.annotations.Nullable;

public enum GameType {
    ALL(0),
    CREATED_BY_USER(1),
    CREATED_BY_KOOK(1);
    private final int id;


    GameType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Nullable
    public GameType fromId(int id){
        for (GameType value : values()) {
            if(value.getId() == id){
                return value;
            }
        }
        return null;
    }
}
