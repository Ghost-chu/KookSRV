package com.ghostchu.plugins.kooksrv.kook.bean.message;

import com.ghostchu.plugins.kooksrv.kook.bean.JsonObjectSerializable;
import com.ghostchu.plugins.kooksrv.kook.bean.enumobj.ChannelMessageType;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SendChannelMessageObject extends JsonObjectSerializable {
    @SerializedName("type")
    private int messageType;
    @SerializedName("target_id")
    private String targetChannelId;
    @SerializedName("content")
    private String content;

    @SerializedName("quote")
    private String quote;
    @SerializedName("nonce")
    private String nonce;
    @SerializedName("temp_target_id")
    private String tempTargetId;

    public SendChannelMessageObject(@NotNull ChannelMessageType messageType, @NotNull String targetChannelId, @NotNull String content, @Nullable String quote, @Nullable String nonce, @Nullable String tempTargetId) {
        this.messageType = messageType.getId();
        this.targetChannelId = targetChannelId;
        this.content = content;
        this.quote = quote;
        this.nonce = nonce;
        this.tempTargetId = tempTargetId;
    }

    @SerializedName("type")
    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    @SerializedName("target_id")
    public String getTargetChannelId() {
        return targetChannelId;
    }

    @SerializedName("quote")
    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public void setTargetChannelId(String targetChannelId) {
        this.targetChannelId = targetChannelId;
    }

    @SerializedName("content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @SerializedName("nonce")
    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    @SerializedName("temp_target_id")
    public String getTempTargetId() {
        return tempTargetId;
    }

    public void setTempTargetId(String tempTargetId) {
        this.tempTargetId = tempTargetId;
    }
}
