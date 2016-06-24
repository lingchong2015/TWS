package curry.stephen.tws.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

import curry.stephen.tws.constant.GlobalVariables;

/**
 * Created by lingchong on 16/6/6.
 */
public class SharedPreferencesHelper {

    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        return context.getSharedPreferences(GlobalVariables.MY_SHARED_PREFERENCE,
                Context.MODE_PRIVATE);
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        return getDefaultSharedPreferences(context).getBoolean(key, defaultValue);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(key, value)
                .apply();
    }

    public static String getString(Context context, String key, String defaultValue) {
        return getDefaultSharedPreferences(context).getString(key, defaultValue);
    }

    public static void putString(Context context, String key, String value) {
        getDefaultSharedPreferences(context)
                .edit()
                .putString(key, value)
                .apply();
    }

    public static void putStringSet(Context context, String key, Set<String> values) {
        getDefaultSharedPreferences(context)
                .edit()
                .putStringSet(key, values)
                .apply();
    }

    public static Set<String> getStringSet(Context context, String key, Set<String> defaultValues) {
        return getDefaultSharedPreferences(context).getStringSet(key, defaultValues);
    }
}
