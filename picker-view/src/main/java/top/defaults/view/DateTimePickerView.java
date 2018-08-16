package top.defaults.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Locale;

import static top.defaults.view.Utils.checkNotNull;

@SuppressWarnings("unused")
public class DateTimePickerView extends PickerViewGroup {

    public static final int TYPE_DATE_TIME = 0;
    public static final int TYPE_DATE_HOUR_MINUTE = 1;
    public static final int TYPE_YEAR_MONTH_DAY_HOUR_MINUTE = 2;
    public static final int TYPE_YEAR_MONTH_DAY = 3;

    protected int type = TYPE_YEAR_MONTH_DAY;

    private Calendar startDate;
    private Calendar endDate;
    private Calendar selectedDate;

    private PickerView yearPickerView;
    private PickerView monthPickerView;
    private PickerView dayPickerView;
    private PickerView datePickerView;
    private PickerView hourPickerView;
    private PickerView minutePickerView;
    private PickerView timePickerView;

    public static final int ONE = 1;
    public static final int FIVE = 5;
    public static final int TEN = 10;
    public static final int FIFTEEN = 15;
    public static final int TWENTY = 20;
    public static final int THIRTY = 30;

    private int minutesInterval = FIVE;

    private static final int PRESERVE_FLAG_MONTH = 1;
    private static final int PRESERVE_FLAG_DAY = 1 << 1;
    private static final int PRESERVE_FLAG_HOUR = 1 << 2;
    private static final int PRESERVE_FLAG_MINUTE = 1 << 3;
    private static final int PRESERVE_FLAG_TIME = 1 << 4;
    private int tryPreserveValuesFlag = 0;

    private boolean shouldTryPreserve(int field) {
        switch (field) {
            case FIELD_MONTH:
                return (tryPreserveValuesFlag & PRESERVE_FLAG_MONTH) != 0;
            case FIELD_DAY:
                return (tryPreserveValuesFlag & PRESERVE_FLAG_DAY) != 0;
            case FIELD_HOUR:
                return (tryPreserveValuesFlag & PRESERVE_FLAG_HOUR) != 0;
            case FIELD_MINUTE:
                return (tryPreserveValuesFlag & PRESERVE_FLAG_MINUTE) != 0;
            case FIELD_TIME:
                return (tryPreserveValuesFlag & PRESERVE_FLAG_TIME) != 0;
        }
        return false;
    }

    private void addFlags(int field) {
        switch (field) {
            case FIELD_YEAR:
                tryPreserveValuesFlag |= PRESERVE_FLAG_MONTH;
                break;
            case FIELD_MONTH:
                tryPreserveValuesFlag |= PRESERVE_FLAG_DAY;
                break;
            case FIELD_DAY:
                if (type == TYPE_YEAR_MONTH_DAY_HOUR_MINUTE) {
                    tryPreserveValuesFlag |= PRESERVE_FLAG_HOUR;
                }
                break;
            case FIELD_DATE:
                if (type == TYPE_DATE_HOUR_MINUTE) {
                    tryPreserveValuesFlag |= PRESERVE_FLAG_HOUR;
                } else if (type == TYPE_DATE_TIME) {
                    tryPreserveValuesFlag |= PRESERVE_FLAG_TIME;
                }
                break;
            case FIELD_HOUR:
                tryPreserveValuesFlag |= PRESERVE_FLAG_MINUTE;
                break;
        }
    }

    private void clearFlags() {
        tryPreserveValuesFlag = 0;
    }

    public interface OnSelectedDateChangedListener {
        void onSelectedDateChanged(Calendar date);
    }

    private OnSelectedDateChangedListener onSelectedDateChangedListener;

    public void setOnSelectedDateChangedListener(OnSelectedDateChangedListener onSelectedDateChangedListener) {
        this.onSelectedDateChangedListener = onSelectedDateChangedListener;
    }

    private void notifySelectedDateChanged() {
        if (onSelectedDateChangedListener != null) {
            onSelectedDateChangedListener.onSelectedDateChanged(selectedDate);
        }
        clearFlags();
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

        buildViews(context);
    }

    private void buildViews(Context context) {
        removeAllViews();

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
        settlePickerView(hourPickerView, type == TYPE_DATE_HOUR_MINUTE);
        settlePickerView(minutePickerView, type == TYPE_DATE_HOUR_MINUTE);
        settlePickerView(timePickerView);

        runIfNotNull(new Runnable() {
            @Override
            public void run() {
                yearPickerView.setAdapter(new PickerView.Adapter<CalendarField>() {

                    @Override
                    public int getItemCount() {
                        if (endDate != null && TimeUtils.compare(startDate, endDate) <= 0) {
                            return endDate.get(Calendar.YEAR) - startDate.get(Calendar.YEAR) + 1;
                        }
                        return yearPickerView.getMaxCount();
                    }

                    @Override
                    public CalendarField getItem(int index) {
                        return new CalendarField(FIELD_YEAR, startDate.get(Calendar.YEAR) + index);
                    }
                });

                monthPickerView.setAdapter(new PickerView.Adapter<CalendarField>() {

                    private int monthOffset() {
                        if (!isAtStartYear()) return 0;
                        return startDate.get(Calendar.MONTH);
                    }

                    @Override
                    public int getItemCount() {
                        if (isAtEndYear()) {
                            return endDate.get(Calendar.MONTH) - monthOffset() + 1;
                        }
                        return 12 - monthOffset();
                    }

                    @Override
                    public CalendarField getItem(int index) {
                        return new CalendarField(FIELD_MONTH, index + monthOffset());
                    }
                });

                dayPickerView.setAdapter(new PickerView.Adapter<CalendarField>() {

                    private int dayOffset() {
                        if (!isAtStartYearAndMonth()) return 0;
                        return startDate.get(Calendar.DAY_OF_MONTH) - 1;
                    }

                    @Override
                    public int getItemCount() {
                        if (isAtEndYearAndMonth()) {
                            return endDate.get(Calendar.DAY_OF_MONTH) - dayOffset();
                        }
                        return selectedDate.getActualMaximum(Calendar.DAY_OF_MONTH) - dayOffset();
                    }

                    @Override
                    public CalendarField getItem(int index) {
                        return new CalendarField(FIELD_DAY, index + dayOffset() + 1);
                    }
                });
            }
        }, yearPickerView, monthPickerView, dayPickerView);

        if (datePickerView != null) {
            datePickerView.setAdapter(new PickerView.Adapter<PickerView.PickerItem>() {
                @Override
                public int getItemCount() {
                    if (endDate != null) {
                        return TimeUtils.daySwitchesBetween(startDate, endDate) + 1;
                    }
                    return datePickerView.getMaxCount();
                }

                @Override
                public PickerView.PickerItem getItem(int index) {
                    final Calendar tempCalendar = (Calendar) startDate.clone();
                    tempCalendar.add(Calendar.DAY_OF_YEAR, index);
                    return new PickerView.PickerItem() {
                        @Override
                        public String getText() {
                            if (TimeUtils.isToday(tempCalendar)) {
                                return "今天";
                            }
                            return TimeUtils.date(tempCalendar);
                        }
                    };
                }
            });
        }

        if (timePickerView != null) {
            timePickerView.setAdapter(new PickerView.Adapter<TimeItem>() {

                private int stepOffset = calculateStepOffsetForTimePicker();

                @Override
                public void notifyDataSetChanged() {
                    stepOffset = calculateStepOffsetForTimePicker();
                    super.notifyDataSetChanged();
                }

                @Override
                public int getItemCount() {
                    if (isAtEndDate()) {
                        return TimeUtils.calculateStep(startDate, endDate, minutesInterval) - stepOffset + 1;
                    }
                    int maxCount = 24 * (60 / minutesInterval);
                    return maxCount - stepOffset;
                }

                @Override
                public TimeItem getItem(int index) {
                    return new TimeItem(DateTimePickerView.this, index + stepOffset);
                }
            });
        }

        runIfNotNull(new Runnable() {
            @Override
            public void run() {
                hourPickerView.setAdapter(new PickerView.Adapter<CalendarField>() {

                    private int hourOffset() {
                        if (!isAtStartDate()) return 0;
                        return startDate.get(Calendar.HOUR_OF_DAY);
                    }

                    @Override
                    public int getItemCount() {
                        if (isAtEndDate()) {
                            return endDate.get(Calendar.HOUR_OF_DAY) - hourOffset() + 1;
                        }
                        return 24 - hourOffset();
                    }

                    @Override
                    public CalendarField getItem(int index) {
                        return new CalendarField(FIELD_HOUR, index + hourOffset());
                    }
                });

                minutePickerView.setAdapter(new PickerView.Adapter<CalendarField>() {

                    private int stepOffset() {
                        if (!isAtStartDateAndHour()) return 0;
                        int fix = startDate.get(Calendar.MINUTE) % minutesInterval != 0 ? 1: 0;
                        return startDate.get(Calendar.MINUTE) / minutesInterval + fix;
                    }

                    @Override
                    public int getItemCount() {
                        if (isAtEndDateAndHour()) {
                            return endDate.get(Calendar.MINUTE) / minutesInterval - stepOffset() + 1;
                        }
                        return 60 / minutesInterval - stepOffset();
                    }

                    @Override
                    public CalendarField getItem(int index) {
                        return new CalendarField(FIELD_MINUTE, (index + stepOffset()) * minutesInterval);
                    }
                });
            }
        }, hourPickerView, minutePickerView);

        reloadData();
    }

    private static final int FIELD_YEAR = 0;
    private static final int FIELD_MONTH = 1;
    private static final int FIELD_DAY = 2;
    private static final int FIELD_HOUR = 3;
    private static final int FIELD_MINUTE = 4;
    private static final int FIELD_DATE = 5;
    private static final int FIELD_TIME = 6;

    private static class CalendarField implements PickerView.PickerItem {

        private int field;
        protected int value;

        CalendarField(int field, int value) {
            this.field = field;
            this.value = value;
        }

        @Override
        public String getText() {
            String text = "";
            switch (field) {
                case FIELD_YEAR:
                    text = String.format(Locale.getDefault(), "%d年", value);
                    break;
                case FIELD_MONTH:
                    text = String.format(Locale.getDefault(), "%02d月", value + 1);
                    break;
                case FIELD_DAY:
                    text = String.format(Locale.getDefault(), "%02d日", value);
                    break;
                case FIELD_HOUR:
                    text = String.format(Locale.getDefault(), "%02d点", value);
                    break;
                case FIELD_MINUTE:
                    text = String.format(Locale.getDefault(), "%02d分", value);
                    break;
            }
            return text;
        }
    }

    private static class TimeItem extends CalendarField {

        private WeakReference<DateTimePickerView> hostRef;

        TimeItem(DateTimePickerView host, int stepCount) {
            super(FIELD_TIME, stepCount);
            hostRef = new WeakReference<>(host);
        }

        @Override
        public String getText() {
            if (hostRef.get() == null) return "";

            final Calendar tempCalendar = (Calendar) hostRef.get().selectedDate.clone();
            tempCalendar.set(Calendar.HOUR_OF_DAY, 0);
            tempCalendar.set(Calendar.MINUTE, 0);
            tempCalendar.add(Calendar.MINUTE, hostRef.get().minutesInterval * value);
            return TimeUtils.time(tempCalendar);
        }
    }

    public void setType(int type) {
        this.type = type;
        buildViews(getContext());
    }

    public void setMinutesInterval(int minutesInterval) {
        if (minutesInterval != ONE && minutesInterval != FIVE && minutesInterval != TEN
                && minutesInterval != FIFTEEN && minutesInterval != TWENTY && minutesInterval != THIRTY) {
            throw new RuntimeException("minutesInterval can only be (1, 5, 10, 15, 20, 30), invalid: " + minutesInterval);
        }

        if (this.minutesInterval != minutesInterval) {
            this.minutesInterval = minutesInterval;
            if (timePickerView != null) {
                timePickerView.notifyDataSetChanged();
            }

            if (minutePickerView != null) {
                minutePickerView.notifyDataSetChanged();
            }
        }
    }

    public void setStartDate(Calendar date) {
        checkNotNull(date, "startDate == null");
        startDate = date;
        adjustMinutesToInterval(startDate);
        if (selectedDate == null || TimeUtils.compare(startDate, selectedDate) > 0) {
            selectedDate = (Calendar) startDate.clone();
        }
        reloadData();
    }

    public void setEndDate(Calendar date) {
        checkNotNull(date, "endDate == null");
        endDate = date;
        if (TimeUtils.compare(startDate, endDate) > 0) {
            endDate = (Calendar) startDate.clone();
        }
        adjustMinutesToIntervalFloor(endDate);
        if (TimeUtils.compare(endDate, selectedDate) < 0) {
            selectedDate = (Calendar) endDate.clone();
        }
        reloadData();
    }

    public Calendar getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Calendar date) {
        checkNotNull(date, "selectedDate == null");
        selectedDate = date;
        adjustMinutesToInterval(selectedDate);
        if (TimeUtils.compare(startDate, selectedDate) > 0) {
            startDate = (Calendar) selectedDate.clone();
        }
        reloadData();
    }

    private void adjustMinutesToInterval(Calendar date) {
        adjustMinutesToInterval(date, false);
    }

    private void adjustMinutesToIntervalFloor(Calendar date) {
        adjustMinutesToInterval(date, true);
    }

    private void adjustMinutesToInterval(Calendar date, boolean floor) {
        int minute = date.get(Calendar.MINUTE);
        int remain = minute % minutesInterval;
        if (remain != 0) {
            date.set(Calendar.MINUTE, minute - remain + (floor ? 0 : minutesInterval));
        }
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

    private int getDesiredPosition(PickerView pickerView, int oldValue) {
        int desiredPosition;
        CalendarField firstItem = (CalendarField) pickerView.getAdapter().getItem(0);
        CalendarField lastItem = (CalendarField) pickerView.getAdapter().getLastItem();
        if (oldValue <= firstItem.value) {
            desiredPosition = 0;
        } else if (oldValue >= lastItem.value) {
            desiredPosition = pickerView.getAdapter().getItemCount() - 1;
        } else {
            desiredPosition = oldValue - firstItem.value;
            if (firstItem.field == FIELD_MINUTE) {
                desiredPosition /= minutesInterval;
            }
        }
        return desiredPosition;
    }

    private void updateSelection() {
        runIfNotNull(new Runnable() {
            @Override
            public void run() {
                yearPickerView.setOnSelectedItemChangedListener(null);
                monthPickerView.setOnSelectedItemChangedListener(null);
                dayPickerView.setOnSelectedItemChangedListener(null);

                int yearSelection = selectedDate.get(Calendar.YEAR) - startDate.get(Calendar.YEAR);
                int monthSelection;
                int daySelection;
                if (yearSelection == 0) {
                    monthSelection = selectedDate.get(Calendar.MONTH) - startDate.get(Calendar.MONTH);
                    if (monthSelection == 0) {
                        daySelection = selectedDate.get(Calendar.DAY_OF_MONTH) - startDate.get(Calendar.DAY_OF_MONTH);
                    } else {
                        daySelection = selectedDate.get(Calendar.DAY_OF_MONTH) - 1;
                    }
                } else {
                    monthSelection = selectedDate.get(Calendar.MONTH);
                    daySelection = selectedDate.get(Calendar.DAY_OF_MONTH) - 1;
                }


                yearPickerView.setSelectedItemPosition(yearSelection);
                monthPickerView.setSelectedItemPosition(monthSelection);
                dayPickerView.setSelectedItemPosition(daySelection);

                yearPickerView.setOnSelectedItemChangedListener(new PickerView.OnSelectedItemChangedListener() {
                    @Override
                    public void onSelectedItemChanged(PickerView pickerView, int previousPosition, int selectedItemPosition) {
                        CalendarField field = (CalendarField)  pickerView.getAdapter().getItem(selectedItemPosition);
                        selectedDate.set(Calendar.YEAR, field.value);
                        addFlags(FIELD_YEAR);
                        monthPickerView.notifyDataSetChanged();
                    }
                });
                monthPickerView.setOnSelectedItemChangedListener(new PickerView.OnSelectedItemChangedListener() {
                    @Override
                    public void onSelectedItemChanged(PickerView pickerView, int previousPosition, int selectedItemPosition) {
                        int desiredPosition = selectedItemPosition;
                        if (shouldTryPreserve(FIELD_MONTH)) {
                            desiredPosition = getDesiredPosition(pickerView, selectedDate.get(Calendar.MONTH));
                        }

                        if (selectedItemPosition != desiredPosition) {
                            monthPickerView.setSelectedItemPosition(desiredPosition);
                        } else {
                            CalendarField field = (CalendarField) pickerView.getAdapter().getItem(selectedItemPosition);

                            CalendarField dayField = (CalendarField) dayPickerView.getAdapter().getItem(dayPickerView.getSelectedItemPosition());
                            Calendar tempCalendar = (Calendar) selectedDate.clone();
                            tempCalendar.set(Calendar.DAY_OF_MONTH, 1);
                            tempCalendar.set(Calendar.MONTH, field.value);
                            int days = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                            if (days < dayField.value) {
                                selectedDate.set(Calendar.DAY_OF_MONTH, days);
                            }

                            selectedDate.set(Calendar.MONTH, field.value);
                            addFlags(FIELD_MONTH);
                            dayPickerView.notifyDataSetChanged();
                        }
                    }
                });
                dayPickerView.setOnSelectedItemChangedListener(new PickerView.OnSelectedItemChangedListener() {
                    @Override
                    public void onSelectedItemChanged(PickerView pickerView, int previousPosition, int selectedItemPosition) {
                        int desiredPosition = selectedItemPosition;
                        if (shouldTryPreserve(FIELD_DAY)) {
                            desiredPosition = getDesiredPosition(pickerView, selectedDate.get(Calendar.DAY_OF_MONTH));
                        }

                        if (selectedItemPosition != desiredPosition) {
                            dayPickerView.setSelectedItemPosition(desiredPosition);
                        } else {
                            CalendarField field = (CalendarField) pickerView.getAdapter().getItem(selectedItemPosition);
                            selectedDate.set(Calendar.DAY_OF_MONTH, field.value);
                            addFlags(FIELD_DAY);
                            chainEvent();
                        }
                    }
                });
            }
        }, yearPickerView, monthPickerView, dayPickerView);

        if (datePickerView != null) {
            datePickerView.setOnSelectedItemChangedListener(null);
            int dateSelection = TimeUtils.daySwitchesBetween(startDate, selectedDate);
            datePickerView.setSelectedItemPosition(dateSelection);

            datePickerView.setOnSelectedItemChangedListener(new PickerView.OnSelectedItemChangedListener() {
                @Override
                public void onSelectedItemChanged(PickerView pickerView, int previousPosition, final int selectedItemPosition) {
                    selectedDate.add(Calendar.DAY_OF_YEAR, selectedItemPosition - previousPosition);
                    addFlags(FIELD_DATE);
                    chainEvent();
                }
            });
        }

        if (timePickerView != null) {
            timePickerView.setOnSelectedItemChangedListener(null);
            int timeSelection = TimeUtils.calculateStep(startDate, selectedDate, minutesInterval);
            timePickerView.setSelectedItemPosition(timeSelection);

            timePickerView.setOnSelectedItemChangedListener(new PickerView.OnSelectedItemChangedListener() {
                @Override
                public void onSelectedItemChanged(PickerView pickerView, int previousPosition, int selectedItemPosition) {
                    int desiredPosition = selectedItemPosition;
                    int previousStep = calculateStepForTimePicker();
                    if (shouldTryPreserve(FIELD_TIME)) {
                        desiredPosition = getDesiredPosition(pickerView, previousStep);
                    }

                    if (selectedItemPosition != desiredPosition) {
                        timePickerView.setSelectedItemPosition(desiredPosition);
                    } else {
                        TimeItem currentItem = (TimeItem) pickerView.getAdapter().getItem(selectedItemPosition);
                        selectedDate.add(Calendar.MINUTE, (currentItem.value - previousStep) * minutesInterval);
                        notifySelectedDateChanged();
                    }
                }
            });
        }

        runIfNotNull(new Runnable() {
            @Override
            public void run() {
                hourPickerView.setOnSelectedItemChangedListener(null);
                minutePickerView.setOnSelectedItemChangedListener(null);

                int hourSelection;
                int minuteSelection;

                if (isAtStartDate()) {
                    hourSelection = selectedDate.get(Calendar.HOUR_OF_DAY) - startDate.get(Calendar.HOUR_OF_DAY);
                    if (hourSelection == 0) {
                        minuteSelection = (selectedDate.get(Calendar.MINUTE) - startDate.get(Calendar.MINUTE)) / minutesInterval;
                    } else {
                        minuteSelection = selectedDate.get(Calendar.MINUTE) / minutesInterval;
                    }
                } else {
                    hourSelection = selectedDate.get(Calendar.HOUR_OF_DAY);
                    minuteSelection = selectedDate.get(Calendar.MINUTE) / minutesInterval;
                }

                hourPickerView.setSelectedItemPosition(hourSelection);
                minutePickerView.setSelectedItemPosition(minuteSelection);

                hourPickerView.setOnSelectedItemChangedListener(new PickerView.OnSelectedItemChangedListener() {
                    @Override
                    public void onSelectedItemChanged(PickerView pickerView, int previousPosition, int selectedItemPosition) {
                        int desiredPosition = selectedItemPosition;
                        if (shouldTryPreserve(FIELD_HOUR)) {
                            desiredPosition = getDesiredPosition(pickerView, selectedDate.get(Calendar.HOUR_OF_DAY));
                        }

                        if (selectedItemPosition != desiredPosition) {
                            hourPickerView.setSelectedItemPosition(desiredPosition);
                        } else {
                            CalendarField field = (CalendarField) pickerView.getAdapter().getItem(selectedItemPosition);
                            selectedDate.set(Calendar.HOUR_OF_DAY, field.value);
                            addFlags(FIELD_HOUR);
                            minutePickerView.notifyDataSetChanged();
                        }
                    }
                });

                minutePickerView.setOnSelectedItemChangedListener(new PickerView.OnSelectedItemChangedListener() {
                    @Override
                    public void onSelectedItemChanged(PickerView pickerView, int previousPosition, int selectedItemPosition) {
                        int desiredPosition = selectedItemPosition;
                        if (shouldTryPreserve(FIELD_MINUTE)) {
                            desiredPosition = getDesiredPosition(pickerView, selectedDate.get(Calendar.MINUTE));
                        }

                        if (selectedItemPosition != desiredPosition) {
                            minutePickerView.setSelectedItemPosition(desiredPosition);
                        } else {
                            CalendarField field = (CalendarField) pickerView.getAdapter().getItem(selectedItemPosition);
                            selectedDate.set(Calendar.MINUTE, field.value);
                            notifySelectedDateChanged();
                        }
                    }
                });
            }
        }, hourPickerView, minutePickerView);
    }

    private void reloadData() {
        updateSelection();
        if (yearPickerView != null) {
            yearPickerView.notifyDataSetChanged();
        }
        if (datePickerView != null) {
            datePickerView.notifyDataSetChanged();
        }
        clearFlags();
    }

    private int calculateStepOffsetForTimePicker() {
        return TimeUtils.calculateStepOffset(startDate, selectedDate, minutesInterval);
    }

    private int calculateStepForTimePicker() {
        return TimeUtils.calculateStep(selectedDate, minutesInterval);
    }

    private void chainEvent() {
        if (timePickerView != null) {
            timePickerView.notifyDataSetChanged();
        } else {
            runIfNotNull(new Runnable() {
                @Override
                public void run() {
                    hourPickerView.notifyDataSetChanged();
                }
            }, new Runnable() {
                @Override
                public void run() {
                    notifySelectedDateChanged();
                }
            }, hourPickerView, minutePickerView);
        }
    }

    private boolean isAtStartYear() {
        return selectedDate.get(Calendar.YEAR) == startDate.get(Calendar.YEAR);
    }

    private boolean isAtStartYearAndMonth() {
        return selectedDate.get(Calendar.YEAR) == startDate.get(Calendar.YEAR)
                && selectedDate.get(Calendar.MONTH) == startDate.get(Calendar.MONTH);
    }

    private boolean isAtStartDate() {
        return selectedDate.get(Calendar.YEAR) == startDate.get(Calendar.YEAR)
                && selectedDate.get(Calendar.DAY_OF_YEAR) == startDate.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isAtStartDateAndHour() {
        return selectedDate.get(Calendar.YEAR) == startDate.get(Calendar.YEAR)
                && selectedDate.get(Calendar.DAY_OF_YEAR) == startDate.get(Calendar.DAY_OF_YEAR)
                && selectedDate.get(Calendar.HOUR_OF_DAY) == startDate.get(Calendar.HOUR_OF_DAY);
    }

    private boolean isAtEndYear() {
        return endDate != null
                && selectedDate.get(Calendar.YEAR) == endDate.get(Calendar.YEAR);
    }

    private boolean isAtEndYearAndMonth() {
        return endDate != null
                && selectedDate.get(Calendar.YEAR) == endDate.get(Calendar.YEAR)
                && selectedDate.get(Calendar.MONTH) == endDate.get(Calendar.MONTH);
    }

    private boolean isAtEndDate() {
        return endDate != null
                && selectedDate.get(Calendar.YEAR) == endDate.get(Calendar.YEAR)
                && selectedDate.get(Calendar.DAY_OF_YEAR) == endDate.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isAtEndDateAndHour() {
        return endDate != null
                && selectedDate.get(Calendar.YEAR) == endDate.get(Calendar.YEAR)
                && selectedDate.get(Calendar.DAY_OF_YEAR) == endDate.get(Calendar.DAY_OF_YEAR)
                && selectedDate.get(Calendar.HOUR_OF_DAY) == endDate.get(Calendar.HOUR_OF_DAY);
    }

    private void runIfNotNull(Runnable runnable, Object... objects) {
        runIfNotNull(runnable, null, objects);
    }

    private void runIfNotNull(Runnable runnable, Runnable elseRunnable, Object... objects) {
        boolean hasNull = false;
        for (Object object : objects) {
            if (object == null) {
                hasNull = true;
                break;
            }
        }
        if (!hasNull) {
            if (runnable != null) runnable.run();
        } else {
            if (elseRunnable != null) elseRunnable.run();
        }
    }
}
