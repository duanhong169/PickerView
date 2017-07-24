package top.defaults.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

class TimeUtils {

    static GregorianCalendar get0Day() {
        return new GregorianCalendar(1, 0, 1);
    }

    static Calendar getCurrentTime() {
        return Calendar.getInstance();
    }

    static boolean isToday(Calendar calendar) {
        Calendar current = getCurrentTime();
        return current.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && current.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR);
    }

    static String date(Calendar calendar) {
        StringBuilder sb = new StringBuilder(calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()));
        if (Locale.getDefault().equals(Locale.CHINA)) {
            sb.append(calendar.get(Calendar.DAY_OF_MONTH)).append("日");
        } else {
            sb.append(" ").append(calendar.get(Calendar.DAY_OF_MONTH));
        }
        sb.append(" ").append(calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()));
        return sb.toString();
    }

    private static SimpleDateFormat HHmm;

    static String time(Calendar calendar) {
        if (HHmm == null) {
            HHmm = new SimpleDateFormat("HH:mm", Locale.getDefault());
        }
        return HHmm.format(calendar.getTime());
    }

    static int compare(Calendar time1, Calendar time2) {
        long time1Millis = time1.getTimeInMillis();
        long time2Millis = time2.getTimeInMillis();
        if (time1Millis == time2Millis) return 0;
        else return time1Millis > time2Millis ? 1 : -1;
    }

    static int daySwitchesBetween(Calendar time1, Calendar time2) {
        return (int) ((time2.getTimeInMillis() - time1.getTimeInMillis()) / (24 * 60 * 60 * 1000));
    }

    private static boolean isAtStartDay(Calendar startDate, Calendar selectedDate) {
        return selectedDate.get(Calendar.YEAR) == startDate.get(Calendar.YEAR)
                && selectedDate.get(Calendar.DAY_OF_YEAR) == startDate.get(Calendar.DAY_OF_YEAR);
    }

    static int calculateStepOffset(Calendar startDate, Calendar selectedDate, int minutesInterval) {
        if (!isAtStartDay(startDate, selectedDate)) return 0;
        int stepOffset = 0;

        int hourValue = startDate.get(Calendar.HOUR_OF_DAY);
        int minuteValue = startDate.get(Calendar.MINUTE);

        stepOffset += (hourValue) * (60 / minutesInterval);
        boolean remain = minuteValue % minutesInterval > 0;
        minuteValue = (minuteValue / minutesInterval + (remain ? 1 : 0)) * minutesInterval;
        stepOffset += minuteValue / minutesInterval;

        return stepOffset;
    }

    static int calculateStep(Calendar date, int minutesInterval) {
        int hours = date.get(Calendar.HOUR_OF_DAY);
        int minutes = date.get(Calendar.MINUTE);
        return (hours * 60 + minutes) / minutesInterval;
    }

    static int calculateStep(Calendar startDate, Calendar selectedDate, int minutesInterval) {
        int hours = selectedDate.get(Calendar.HOUR_OF_DAY) - startDate.get(Calendar.HOUR_OF_DAY);
        int minutes = selectedDate.get(Calendar.MINUTE) - startDate.get(Calendar.MINUTE);
        return (hours * 60 + minutes) / minutesInterval;
    }
}
