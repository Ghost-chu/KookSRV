package com.ghostchu.plugins.kooksrv.util;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.util.Objects;


/**
 * Utilities to prevent create Gson object in other place and reuse gson object in runtime
 *
 * @author Ghost_chu and sandtechnology, modified based on Lucko's Helper project
 */
public final class JsonUtil {
    private static final Gson STANDARD_GSON = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .setExclusionStrategies(new HiddenAnnotationExclusionStrategy())
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    private static final Gson PRETTY_PRINT_GSON = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .setExclusionStrategies(new HiddenAnnotationExclusionStrategy())
            .serializeNulls()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    @NotNull
    @Deprecated
    public static Gson get() {
        return standard();
    }

    @NotNull
    public static Gson standard() {
        return STANDARD_GSON;
    }

    public static Gson getGson() {
        return STANDARD_GSON;
    }

    @NotNull
    @Deprecated
    public static Gson getPrettyPrinting() {
        return prettyPrinting();
    }

    @NotNull
    public static Gson prettyPrinting() {
        return PRETTY_PRINT_GSON;
    }

    @NotNull
    public static JsonObject readObject(@NotNull Reader reader) {
        return new JsonParser().parse(reader).getAsJsonObject();
    }

    @NotNull
    public static JsonObject readObject(@NotNull String s) {
        return new JsonParser().parse(s).getAsJsonObject();
    }

    @NotNull
    public static JsonArray readArray(@NotNull String s) {
        return new JsonParser().parse(s).getAsJsonArray();
    }
    @NotNull
    public static JsonArray readArray(@NotNull Reader reader) {
        return new JsonParser().parse(reader).getAsJsonArray();
    }
    public static Gson regular() {
        return STANDARD_GSON;
    }
    public static boolean isJson(String str) {
        if (str == null || str.isBlank()) {
            return false;
        }
        try {
            JsonElement element = JsonParser.parseString(str);
            return element.isJsonObject() || element.isJsonArray();
        } catch (JsonParseException exception) {
            return false;
        }
    }
    @NotNull
    public static String toString(@NotNull JsonElement element) {
        return Objects.requireNonNull(standard().toJson(element));
    }

    @NotNull
    public static String toStringPretty(@NotNull JsonElement element) {
        return Objects.requireNonNull(prettyPrinting().toJson(element));
    }

    public static void writeElement(@NotNull Appendable writer, @NotNull JsonElement element) {
        standard().toJson(element, writer);
    }

    public static void writeElementPretty(@NotNull Appendable writer, @NotNull JsonElement element) {
        prettyPrinting().toJson(element, writer);
    }

    public static void writeObject(@NotNull Appendable writer, @NotNull JsonObject object) {
        standard().toJson(object, writer);
    }

    public static void writeObjectPretty(@NotNull Appendable writer, @NotNull JsonObject object) {
        prettyPrinting().toJson(object, writer);
    }

    public @interface Hidden {

    }

    public static class HiddenAnnotationExclusionStrategy implements ExclusionStrategy {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getAnnotation(Hidden.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return clazz.getDeclaredAnnotation(Hidden.class) != null;
        }
    }

}
