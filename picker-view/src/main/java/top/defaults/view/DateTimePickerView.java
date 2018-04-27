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

        if (datePickerView != null) {
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

        runIfNotNull(new Runnable() {
            @Override
            public void run() {
                hourPickerView.setAdapter(new PickerView.Adapter() {

                    private int hourOffset() {
                        if (!isAtStartDate()) return 0;
                        return startDate.get(Calendar.HOUR_OF_DAY);
                    }

                    @Override
                    public int getItemCount() {
                        return 24 - hourOffset();
                    }

                    @Override
                    public PickerView.PickerItem getItem(int index) {
                        return new CalendarField(Calendar.HOUR_OF_DAY, index + hourOffset());
                    }
                });

                minutePickerView.setAdapter(new PickerView.Adapter() {

                    private int stepOffset() {
                        if (!isAtStartDateAndHour()) return 0;
                        int fix = startDate.get(Calendar.MINUTE) % minutesInterval != 0 ? 1: 0;
                        return startDate.get(Calendar.MINUTE) / minutesInterval + fix;
                    }

                    @Override
                    public int getItemCount() {
                        return 60 / minutesInterval - stepOffset();
                    }

                    @Override
                    public PickerView.PickerItem getItem(int index) {
                        return new CalendarField(Calendar.MINUTE, (index + stepOffset()) * minutesInterval);
                    }
                });
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
                case Calendar.HOUR_OF_DAY:
                    text = String.format(Locale.getDefault(), "%02d点", value);
                    break;
                case Calendar.MINUTE:
                    text = String.format(Locale.getDefault(), "%02d分", value);
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

                            CalendarField dayField = (CalendarField) dayPickerView.getAdapter().getItem(dayPickerView.getSelectedItemPosition());
                            Calendar tempCalendar = (Calendar) selectedDate.clone();
                            tempCalendar.set(Calendar.DAY_OF_MONTH, 1);
                            tempCalendar.set(Calendar.MONTH, field.value);
                            int days = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                            if (days < dayField.value) {
                                selectedDate.set(Calendar.DAY_OF_MONTH, days);
                            }

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
                                update(field);
                            }
                        } else {
                            CalendarField field = (CalendarField) pickerView.getAdapter().getItem(selectedItemPosition);
                            update(field);
                        }
                    }

                    private void update(CalendarField field) {
                        selectedDate.set(Calendar.DAY_OF_MONTH, field.value);
                        chainEvent();
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
                            notifySelectedDateChanged();
                        }
                    } else {
                        TimeItem currentItem = (TimeItem) pickerView.getAdapter().getItem(selectedItemPosition);
                        selectedDate.add(Calendar.MINUTE, (currentItem.stepCount - previousStep) * minutesInterval);
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
                        if (previousPosition == selectedItemPosition) {
                            CalendarField firstItem = (CalendarField) pickerView.getAdapter().getItem(0);
                            int desiredPosition;
                            if (selectedDate.get(Calendar.HOUR_OF_DAY) <= firstItem.value) {
                                desiredPosition = 0;
                            } else {
                                desiredPosition = selectedDate.get(Calendar.HOUR_OF_DAY) - firstItem.value;
                            }

                            if (selectedItemPosition != desiredPosition) {
                                hourPickerView.setSelectedItemPosition(desiredPosition);
                            } else {
                                CalendarField field = (CalendarField) pickerView.getAdapter().getItem(selectedItemPosition);
                                selectedDate.set(Calendar.HOUR_OF_DAY, field.value);
                                minutePickerView.notifyDataSetChanged();
                            }
                        } else {
                            CalendarField field = (CalendarField) pickerView.getAdapter().getItem(selectedItemPosition);
                            selectedDate.set(Calendar.HOUR_OF_DAY, field.value);
                            minutePickerView.notifyDataSetChanged();
                        }
                    }
                });

                minutePickerView.setOnSelectedItemChangedListener(new PickerView.OnSelectedItemChangedListener() {
                    @Override
                    public void onSelectedItemChanged(PickerView pickerView, int previousPosition, int selectedItemPosition) {
                        if (previousPosition == selectedItemPosition) {
                            CalendarField firstItem = (CalendarField) pickerView.getAdapter().getItem(0);
                            int desiredPosition;
                            if (selectedDate.get(Calendar.MINUTE) <= firstItem.value) {
                                desiredPosition = 0;
                            } else {
                                desiredPosition = (selectedDate.get(Calendar.MINUTE) - firstItem.value) / minutesInterval;
                            }

                            if (selectedItemPosition != desiredPosition) {
                                minutePickerView.setSelectedItemPosition(desiredPosition);
                            } else {
                                CalendarField field = (CalendarField) pickerView.getAdapter().getItem(selectedItemPosition);
                                selectedDate.set(Calendar.MINUTE, field.value);
                                notifySelectedDateChanged();
                            }
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
    }

    private int calculateStepOffset() {
        return TimeUtils.calculateStepOffset(startDate, selectedDate, minutesInterval);
    }

    private int calculateStep() {
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
