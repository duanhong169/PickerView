package top.defaults.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Locale;

import static top.defaults.view.Utils.checkNotNull;

public class DateTimePickerView extends PickerViewGroup {

    protected static final int TYPE_DATE_TIME = 0;
    protected static final int TYPE_DATE_HOUR_MINUTE = 1;
    protected static final int TYPE_YEAR_MONTH_DAY_HOUR_MINUTE = 2;
    protected static final int TYPE_YEAR_MONTH_DAY = 3;

    protected int type = TYPE_YEAR_MONTH_DAY;

    private Calendar startDate;
    private Calendar selectedDate;

    private final PickerView yearPickerView;
    private final PickerView monthPickerView;
    private final PickerView dayPickerView;
    private final PickerView datePickerView;
    private final PickerView hourPickerView;
    private final PickerView minutePickerView;
    private final PickerView timePickerView;

    public static final int ONE = 1;
    public static final int FIVE = 5;
    public static final int TEN = 10;
    public static final int FIFTEEN = 15;
    public static final int TWENTY = 20;
    public static final int THIRTY = 30;

    private int minutesInterval = FIVE;

    public interface OnSelectedDateChangedListener {
        void onSelectedDateChanged(Calendar date);
    }

    private OnSelectedDateChangedListener onSelectedDateChangedListener;

    public void setOnSelectedDateChangedListener(OnSelectedDateChangedListener onSelectedDateChangedListener) {
        this.onSelectedDateChangedListener = onSelectedDateChangedListener;
    }

    private void notifySelectedDateChanged(Calendar date) {
        if (onSelectedDateChangedListener != null) {
            onSelectedDateChangedListener.onSelectedDateChanged(date);
        }
    }

    public DateTimePickerView(Context context) {
        this(context, null);
    }

    public DateTimePickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DateTimePickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setStartDate(TimeUtils.get0Day());
        setSelectedDate(TimeUtils.getCurrentTime());

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DateTimePickerView);
        type = typedArray.getInt(R.styleable.DateTimePickerView_type, TYPE_YEAR_MONTH_DAY);
        minutesInterval = typedArray.getInt(R.styleable.DateTimePickerView_minutesInterval, FIVE);
        typedArray.recycle();

        switch (type) {
            case TYPE_DATE_TIME:
                yearPickerView = null;
                monthPickerView = null;
                dayPickerView = null;
                datePickerView = new PickerView(context);
                hourPickerView = null;
                minutePickerView = null;
                timePickerView = new PickerView(context);
                break;
            case TYPE_DATE_HOUR_MINUTE:
                yearPickerView = null;
                monthPickerView = null;
                dayPickerView = null;
                datePickerView = new PickerView(context);
                hourPickerView = new PickerView(context);
                minutePickerView = new PickerView(context);
                timePickerView = null;
                break;
            case TYPE_YEAR_MONTH_DAY_HOUR_MINUTE:
                yearPickerView = new PickerView(context);
                monthPickerView = new PickerView(context);
                dayPickerView = new PickerView(context);
                datePickerView = null;
                hourPickerView = new PickerView(context);
                minutePickerView = new PickerView(context);
                timePickerView = null;
                break;
            case TYPE_YEAR_MONTH_DAY:
                yearPickerView = new PickerView(context);
                monthPickerView = new PickerView(context);
                dayPickerView = new PickerView(context);
                datePickerView = null;
                hourPickerView = null;
                minutePickerView = null;
                timePickerView = null;
                break;
            default:
                yearPickerView = null;
                monthPickerView = null;
                dayPickerView = null;
                datePickerView = null;
                hourPickerView = null;
                minutePickerView = null;
                timePickerView = null;
                break;
        }

        configurePickerViews();
    }

    private void configurePickerViews() {
        // 顺序敏感
        settlePickerView(yearPickerView);
        settlePickerView(monthPickerView);
        settlePickerView(dayPickerView);
        settlePickerView(datePickerView);
        settlePickerView(hourPickerView);
        settlePickerView(minutePickerView);
        settlePickerView(timePickerView);

        runIfNotNull(new Runnable() {
            @Override
            public void run() {
                yearPickerView.setAdapter(new PickerView.Adapter() {

                    @Override
                    public int getItemCount() {
                        return yearPickerView.getMaxCount();
                    }

                    @Override
                    public CalendarField getItem(int index) {
                        return new CalendarField(Calendar.YEAR, startDate.get(Calendar.YEAR) + index);
                    }
                });

                monthPickerView.setAdapter(new PickerView.Adapter() {
                    // 不显示开始日期之前的月份
                    private boolean isAtStartYear() {
                        return selectedDate.get(Calendar.YEAR) == startDate.get(Calendar.YEAR);
                    }

                    private int monthOffset() {
                        if (!isAtStartYear()) return 0;
                        return startDate.get(Calendar.MONTH);
                    }

                    @Override
                    public int getItemCount() {
                        return 12 - monthOffset();
                    }

                    @Override
                    public CalendarField getItem(int index) {
                        return new CalendarField(Calendar.MONTH, index + monthOffset());
                    }
                });

                dayPickerView.setAdapter(new PickerView.Adapter() {
                    // 不显示开始日期之前的日期
                    private boolean isAtStartYearAndMonth() {
                        return selectedDate.get(Calendar.YEAR) == startDate.get(Calendar.YEAR)
                                && selectedDate.get(Calendar.MONTH) == startDate.get(Calendar.MONTH);
                    }

                    private int dayOffset() {
                        if (!isAtStartYearAndMonth()) return 0;
                        return startDate.get(Calendar.DAY_OF_MONTH) - 1;
                    }

                    @Override
                    public int getItemCount() {
                        return selectedDate.getActualMaximum(Calendar.DAY_OF_MONTH) - dayOffset();
                    }

                    @Override
                    public CalendarField getItem(int index) {
                        return new CalendarField(Calendar.DAY_OF_MONTH, index + dayOffset() + 1);
                    }
                });
            }
        }, yearPickerView, monthPickerView, dayPickerView);

        runIfNotNull(new Runnable() {
            @Override
            public void run() {
                datePickerView.setAdapter(new PickerView.Adapter() {
                    @Override
                    public int getItemCount() {
                        return datePickerView.getMaxCount();
                    }

                    @Override
                    public PickerView.PickerItem getItem(int index) {
                        final Calendar tempCalendar = (Calendar) startDate.clone();
                        tempCalendar.add(Calendar.DAY_OF_YEAR, index);
                        return new PickerView.PickerItem() {
                            @Override
                            public String getText() {
                                return TimeUtils.date(tempCalendar);
                            }
                        };
                    }
                });
            }
        }, datePickerView);

        runIfNotNull(new Runnable() {
            @Override
            public void run() {
                timePickerView.setAdapter(new PickerView.Adapter() {

                    private int stepOffset = calculateStepOffset();

                    @Override
                    public void notifyDataSetChanged() {
                        stepOffset = calculateStepOffset();
                        super.notifyDataSetChanged();
                    }

                    @Override
                    public int getItemCount() {
                        int maxCount = 24 * (60 / minutesInterval);
                        return maxCount - stepOffset;
                    }

                    @Override
                    public PickerView.PickerItem getItem(int index) {
                        return new TimeItem(DateTimePickerView.this, index + stepOffset);
                    }
                });
            }
        }, timePickerView);

        runIfNotNull(new Runnable() {
            @Override
            public void run() {

            }
        }, hourPickerView, minutePickerView);

        reloadData();
    }

    private static class CalendarField implements PickerView.PickerItem {

        private int field;
        private int value;

        CalendarField(int field, int value) {
            this.field = field;
            this.value = value;
        }

        @Override
        public String getText() {
            String text = "";
            switch (field) {
                case Calendar.YEAR:
                    text = String.format(Locale.getDefault(), "%d年", value);
                    break;
                case Calendar.MONTH:
                    text = String.format(Locale.getDefault(), "%02d月", value + 1);
                    break;
                case Calendar.DAY_OF_MONTH:
                    text = String.format(Locale.getDefault(), "%02d日", value);
                    break;
            }
            return text;
        }
    }

    private static class TimeItem implements PickerView.PickerItem {

        private WeakReference<DateTimePickerView> hostRef;
        private int stepCount;

        TimeItem(DateTimePickerView host, int stepCount) {
            hostRef = new WeakReference<>(host);
            this.stepCount = stepCount;
        }

        @Override
        public String getText() {
            if (hostRef.get() == null) return "";

            final Calendar tempCalendar = (Calendar) hostRef.get().selectedDate.clone();
            tempCalendar.set(Calendar.HOUR_OF_DAY, 0);
            tempCalendar.set(Calendar.MINUTE, 0);
            tempCalendar.add(Calendar.MINUTE, hostRef.get().minutesInterval * stepCount);
            return TimeUtils.time(tempCalendar);
        }
    }

    public void setStartDate(Calendar date) {
        checkNotNull(date, "startDate == null");
        startDate = date;
        int minute = startDate.get(Calendar.MINUTE);
        int remain = minute % minutesInterval;
        if (remain != 0) {
            startDate.set(Calendar.MINUTE, minute + minutesInterval - remain);
        }
        if (selectedDate == null || TimeUtils.compare(startDate, selectedDate) > 0) {
            selectedDate = (Calendar) startDate.clone();
        }
        reloadData();
    }

    public Calendar getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Calendar date) {
        checkNotNull(date, "selectedDate == null");
        selectedDate = date;
        int minute = selectedDate.get(Calendar.MINUTE);
        int remain = minute % minutesInterval;
        if (remain != 0) {
            selectedDate.set(Calendar.MINUTE, minute + minutesInterval - remain);
        }
        if (startDate == null || TimeUtils.compare(startDate, selectedDate) > 0) {
            startDate = (Calendar) selectedDate.clone();
        }
        reloadData();
    }

    public PickerView getYearPickerView() {
        return yearPickerView;
    }

    public PickerView getMonthPickerView() {
        return monthPickerView;
    }

    public PickerView getDayPickerView() {
        return dayPickerView;
    }

    public PickerView getDatePickerView() {
        return datePickerView;
    }

    public PickerView getHourPickerView() {
        return hourPickerView;
    }

    public PickerView getMinutePickerView() {
        return minutePickerView;
    }

    public PickerView getTimePickerView() {
        return timePickerView;
    }

    private void updateSelection() {
        runIfNotNull(new Runnable() {
            @Override
            public void run() {
                yearPickerView.setOnSelectedItemChangedListener(null);
                monthPickerView.setOnSelectedItemChangedListener(null);
                dayPickerView.setOnSelectedItemChangedListener(null);

                int yearSelection = selectedDate.get(Calendar.YEAR) - startDate.get(Calendar.YEAR);
                int monthSelection = selectedDate.get(Calendar.MONTH) - startDate.get(Calendar.MONTH);
                int daySelection = selectedDate.get(Calendar.DAY_OF_MONTH) - startDate.get(Calendar.DAY_OF_MONTH);

                yearPickerView.setSelectedItemPosition(yearSelection);
                monthPickerView.setSelectedItemPosition(monthSelection);
                dayPickerView.setSelectedItemPosition(daySelection);

                yearPickerView.setOnSelectedItemChangedListener(new PickerView.OnSelectedItemChangedListener() {
                    @Override
                    public void onSelectedItemChanged(PickerView pickerView, int previousPosition, int selectedItemPosition) {
                        CalendarField field = (CalendarField)  pickerView.getAdapter().getItem(selectedItemPosition);
                        selectedDate.set(Calendar.YEAR, field.value);
                        monthPickerView.notifyDataSetChanged();
                    }
                });
                monthPickerView.setOnSelectedItemChangedListener(new PickerView.OnSelectedItemChangedListener() {
                    @Override
                    public void onSelectedItemChanged(PickerView pickerView, int previousPosition, int selectedItemPosition) {
                        if (previousPosition == selectedItemPosition) {
                            CalendarField firstItem = (CalendarField) pickerView.getAdapter().getItem(0);
                            int desiredPosition;
                            if (selectedDate.get(Calendar.MONTH) <= firstItem.value) {
                                desiredPosition = 0;
                            } else {
                                desiredPosition = selectedDate.get(Calendar.MONTH) - firstItem.value;
                            }

                            if (selectedItemPosition != desiredPosition) {
                                monthPickerView.setSelectedItemPosition(desiredPosition);
                            } else {
                                CalendarField field = (CalendarField) pickerView.getAdapter().getItem(selectedItemPosition);
                                selectedDate.set(Calendar.MONTH, field.value);
                                dayPickerView.notifyDataSetChanged();
                            }
                        } else {
                            CalendarField field = (CalendarField) pickerView.getAdapter().getItem(selectedItemPosition);
                            selectedDate.set(Calendar.MONTH, field.value);
                            dayPickerView.notifyDataSetChanged();
                        }
                    }
                });
                dayPickerView.setOnSelectedItemChangedListener(new PickerView.OnSelectedItemChangedListener() {
                    @Override
                    public void onSelectedItemChanged(PickerView pickerView, int previousPosition, int selectedItemPosition) {
                        if (previousPosition == selectedItemPosition) {
                            CalendarField firstItem = (CalendarField) pickerView.getAdapter().getItem(0);
                            int desiredPosition;
                            if (selectedDate.get(Calendar.DAY_OF_MONTH) <= firstItem.value) {
                                desiredPosition = 0;
                            } else {
                                desiredPosition = selectedDate.get(Calendar.DAY_OF_MONTH) - firstItem.value;
                            }

                            if (selectedItemPosition != desiredPosition) {
                                dayPickerView.setSelectedItemPosition(desiredPosition);
                            } else {
                                CalendarField field = (CalendarField) pickerView.getAdapter().getItem(selectedItemPosition);
                                selectedDate.set(Calendar.DAY_OF_MONTH, field.value);
                                notifySelectedDateChanged(selectedDate);
                            }
                        } else {
                            CalendarField field = (CalendarField) pickerView.getAdapter().getItem(selectedItemPosition);
                            selectedDate.set(Calendar.DAY_OF_MONTH, field.value);
                            notifySelectedDateChanged(selectedDate);
                        }
                    }
                });
            }
        }, yearPickerView, monthPickerView, dayPickerView);

        runIfNotNull(new Runnable() {
            @Override
            public void run() {
                datePickerView.setOnSelectedItemChangedListener(null);
                timePickerView.setOnSelectedItemChangedListener(null);

                int dateSelection = TimeUtils.daySwitchesBetween(startDate, selectedDate);
                int timeSelection = TimeUtils.calculateStep(startDate, selectedDate, minutesInterval);

                datePickerView.setSelectedItemPosition(dateSelection);
                timePickerView.setSelectedItemPosition(timeSelection);

                datePickerView.setOnSelectedItemChangedListener(new PickerView.OnSelectedItemChangedListener() {
                    @Override
                    public void onSelectedItemChanged(PickerView pickerView, int previousPosition, int selectedItemPosition) {
                        selectedDate.add(Calendar.DAY_OF_YEAR, selectedItemPosition - previousPosition);
                        timePickerView.notifyDataSetChanged();
                    }
                });

                timePickerView.setOnSelectedItemChangedListener(new PickerView.OnSelectedItemChangedListener() {
                    @Override
                    public void onSelectedItemChanged(PickerView pickerView, int previousPosition, int selectedItemPosition) {
                        int previousStep = calculateStep();
                        if (previousPosition == selectedItemPosition) {
                            TimeItem firstItem = (TimeItem) pickerView.getAdapter().getItem(0);
                            int desiredPosition;
                            if (previousStep <= firstItem.stepCount) {
                                desiredPosition = 0;
                            } else {
                                desiredPosition = previousStep - firstItem.stepCount;
                            }

                            if (selectedItemPosition != desiredPosition) {
                                timePickerView.setSelectedItemPosition(desiredPosition);
                            } else {
                                TimeItem currentItem = (TimeItem) pickerView.getAdapter().getItem(selectedItemPosition);
                                selectedDate.add(Calendar.MINUTE, (currentItem.stepCount - previousStep) * minutesInterval);
                                notifySelectedDateChanged(selectedDate);
                            }
                        } else {
                            TimeItem currentItem = (TimeItem) pickerView.getAdapter().getItem(selectedItemPosition);
                            selectedDate.add(Calendar.MINUTE, (currentItem.stepCount - previousStep) * minutesInterval);
                            notifySelectedDateChanged(selectedDate);
                        }
                    }
                });
            }
        }, datePickerView, timePickerView);

        runIfNotNull(new Runnable() {
            @Override
            public void run() {

            }
        }, datePickerView, hourPickerView, minutePickerView);

        runIfNotNull(new Runnable() {
            @Override
            public void run() {

            }
        }, hourPickerView, minutePickerView);
    }

    private void reloadData() {
        updateSelection();
        runIfNotNull(new Runnable() {
            @Override
            public void run() {
                yearPickerView.notifyDataSetChanged();
            }
        }, yearPickerView);

        runIfNotNull(new Runnable() {
            @Override
            public void run() {
                datePickerView.notifyDataSetChanged();
            }
        }, datePickerView);
    }

    private int calculateStepOffset() {
        return TimeUtils.calculateStepOffset(startDate, selectedDate, minutesInterval);
    }

    private int calculateStep() {
        return TimeUtils.calculateStep(selectedDate, minutesInterval);
    }

    private void runIfNotNull(Runnable runnable, Object... objects) {
        for (Object object : objects) {
            if (object == null) return;
        }
        runnable.run();
    }
}
