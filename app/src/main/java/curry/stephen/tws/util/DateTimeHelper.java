package curry.stephen.tws.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by lingchong on 16/6/23.
 */
public class DateTimeHelper {

    public static String getDateTimeNow() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).format(
                Calendar.getInstance().getTime());
    }
}
