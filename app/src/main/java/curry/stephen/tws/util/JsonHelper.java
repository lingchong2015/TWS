package curry.stephen.tws.util;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Json-related based on Gson package helper class.<br/>
 * Created by lingchong on 16/5/23.
 */
public class JsonHelper {

    public static String toJson(Object object) {
        return new Gson().toJson(object);
    }

    public static <T> T fromJson(String s, Type t) {
        return (T)  new Gson().fromJson(s, t);
    }
}
