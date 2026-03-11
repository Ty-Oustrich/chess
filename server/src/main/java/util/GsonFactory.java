package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class GsonFactory {

    private static final Gson gson = new GsonBuilder().create();

    private GsonFactory() {
    }

    public static Gson create() {
        return gson;
    }
}

