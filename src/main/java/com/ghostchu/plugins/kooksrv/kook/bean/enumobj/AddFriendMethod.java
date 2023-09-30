package com.ghostchu.plugins.kooksrv.kook.bean.enumobj;

import org.jetbrains.annotations.Nullable;

public enum AddFriendMethod {
    SEARCH(0),
    FROM_GUILD(2);
    private final int id;

    AddFriendMethod(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
    @Nullable
    public AddFriendMethod fromId(int id){
        for (AddFriendMethod value : values()) {
            if(value.getId() == id){
                return value;
            }
        }
        return null;
    }
}
