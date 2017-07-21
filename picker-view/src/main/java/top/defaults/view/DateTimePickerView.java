package top.defaults.view;

import android.content.Context;
import android.util.AttributeSet;

import java.util.Calendar;
import java.util.Locale;

public class DateTimePickerView extends PickerViewGroup {

    private Calendar startDate = TimeUtils.get0Day();
    private Calendar selectedDate = TimeUtils.getCurrentDay();

    private final PickerView yearPickerView;
    private final PickerView monthPickerView;
    private final PickerView dayPickerView;
    private final PickerView datePickerView;
    private final PickerView hourPickerView;
    private final PickerView minutePickerView;
    private final PickerView timePickerView;

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
                        return (startDate != null && selectedDate.get(Calendar.YEAR) == startDate.get(Calendar.YEAR));
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
                        return (startDate != null
                                && selectedDate.get(Calendar.YEAR) == startDate.get(Calendar.YEAR)
                                && selectedDate.get(Calendar.MONTH) == startDate.get(Calendar.MONTH));
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

        updateSelection();
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

    public void setStartDate(Calendar date) {
        startDate = date;
        reloadData();
    }

    public Calendar getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Calendar date) {
        selectedDate = date;
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
    }

    private void reloadData() {
        updateSelection();
        runIfNotNull(new Runnable() {
            @Override
            public void run() {
                yearPickerView.notifyDataSetChanged();
            }
        }, yearPickerView);
    }

    private void runIfNotNull(Runnable runnable, Object... objects) {
        for (Object object : objects) {
            if (object == null) return;
        }
        runnable.run();
    }
}
