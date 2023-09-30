package com.ghostchu.plugins.kooksrv.kook.bean.enumobj;

import org.jetbrains.annotations.Nullable;

public enum ChannelMessageType {
    TEXT(1),
    KMARKDOWN(9),
    CARD_MESSAGE(10);
    private final int id;


    ChannelMessageType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Nullable
    public ChannelMessageType fromId(int id){
        for (ChannelMessageType value : values()) {
            if(value.getId() == id){
                return value;
            }
        }
        return null;
    }
}
