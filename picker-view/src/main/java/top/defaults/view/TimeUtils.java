package top.defaults.view;

import java.util.Calendar;
import java.util.GregorianCalendar;

class TimeUtils {

    static GregorianCalendar get0Day() {
        return new GregorianCalendar(1, 0, 1);
    }

    static GregorianCalendar getCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        return new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }
}
