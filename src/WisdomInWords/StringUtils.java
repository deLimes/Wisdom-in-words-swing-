package WisdomInWords;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by User on 25.02.2018.
 */

public abstract class StringUtils {

    public static boolean isStringSet(String str) {
        return ((str != null) && (str.length() > 0));
    }

    public static String d00(int i) {
        if (i > 9) {
            return String.valueOf(i);
        } else {
            return "0" + String.valueOf(i);
        }
    }

    public static String d000(int i) {
        if (i > 99) {
            return String.valueOf(i);
        } else if (i > 9) {
            return "0" + String.valueOf(i);
        } else {
            return "00" + String.valueOf(i);
        }
    }

    public static String longFormat(long number) {
        StringBuffer sb = new StringBuffer();
        long r = number;
        while (true) {
            int m = (int) r % 1000;
            r = r / 1000;
            if (r == 0) {
                sb.insert(0, m);
                break;
            } else {
                sb.insert(0, d000(m));
                sb.insert(0, ',');
            }
        }
        return sb.toString();
    }

    public static String timeToString(long milliseconds) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        //Calendar calendar = Calendar.getInstance(new Locale("uk", "UA"));
        //calendar.setTime(new Date(milliseconds));
        calendar.setTimeInMillis(milliseconds);
        return StringUtils.timeToString(calendar);
    }

    public static String timeToStringSSS(long milliseconds) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(milliseconds);
        return StringUtils.timeToStringSSS(calendar);
    }

    public static String timeToString(Calendar calendar) {
        StringBuffer sb;
        sb = new StringBuffer();
        sb.append(StringUtils.d00(calendar.get(Calendar.HOUR_OF_DAY))).append(":");
        sb.append(StringUtils.d00(calendar.get(Calendar.MINUTE))).append(":");
        sb.append(StringUtils.d00(calendar.get(Calendar.SECOND)));
        return sb.toString();
    }

    public static String timeToStringSSS(Calendar calendar) {
        StringBuffer sb;
        sb = new StringBuffer();
        sb.append(StringUtils.d00(calendar.get(Calendar.HOUR_OF_DAY))).append(":");
        sb.append(StringUtils.d00(calendar.get(Calendar.MINUTE))).append(":");
        sb.append(StringUtils.d00(calendar.get(Calendar.SECOND))).append(".");
        sb.append(StringUtils.d000(calendar.get(Calendar.MILLISECOND)));
        return sb.toString();
    }


    public static String dateToString(long milliseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(milliseconds));
        return StringUtils.dateToString(calendar);
    }

    public static String dateToString(Calendar calendar) {
        StringBuffer sb;
        sb = new StringBuffer();
        sb.append(StringUtils.d00(calendar.get(Calendar.DAY_OF_MONTH))).append("-");
        sb.append(StringUtils.d00(calendar.get(Calendar.MONTH) + 1)).append("-");
        sb.append(StringUtils.d00(calendar.get(Calendar.YEAR)));
        return sb.toString();
    }


}