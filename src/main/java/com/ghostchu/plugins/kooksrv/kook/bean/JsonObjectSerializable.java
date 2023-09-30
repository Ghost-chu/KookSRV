package com.ghostchu.plugins.kooksrv.kook.bean;

import com.ghostchu.plugins.kooksrv.util.JsonUtil;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public abstract class JsonObjectSerializable {
    @NotNull
    public JsonObject toJsonObject(){
        return JsonUtil.readObject(JsonUtil.standard().toJson(this));
    }
}
