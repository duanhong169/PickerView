package top.defaults.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.Calendar;
import java.util.Locale;

import static top.defaults.view.PickerView.DEFAULT_MAX_OFFSET_ITEM_COUNT;

public class DatePickerView extends LinearLayout {

    private int preferredMaxOffsetItemCount = DEFAULT_MAX_OFFSET_ITEM_COUNT;
    private int itemHeight;
    private int textSize;
    private int textColor = Color.BLACK;
    private boolean autoFitSize;

    private Calendar startDate = TimeUtils.get0Day();
    private Calendar selectedDate = TimeUtils.getCurrentDay();

    private final PickerView yearPickerView;
    private final PickerView monthPickerView;
    private final PickerView dayPickerView;

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

    public DatePickerView(Context context) {
        this(context, null);
    }

    public DatePickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DatePickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        yearPickerView = new PickerView(context);
        monthPickerView = new PickerView(context);
        dayPickerView = new PickerView(context);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(HORIZONTAL);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DatePickerView);
        preferredMaxOffsetItemCount = typedArray.getInt(R.styleable.DatePickerView_preferredMaxOffsetItemCount, DEFAULT_MAX_OFFSET_ITEM_COUNT);
        int defaultItemHeight = Utils.pixelOfDp(getContext(), 32);
        itemHeight = typedArray.getDimensionPixelSize(R.styleable.DatePickerView_itemHeight, defaultItemHeight);
        int defaultTextSize = Utils.pixelOfScaled(getContext(), 22);
        textSize = typedArray.getDimensionPixelSize(R.styleable.DatePickerView_textSize, defaultTextSize);
        textColor = typedArray.getColor(R.styleable.DatePickerView_textColor, Color.BLACK);
        autoFitSize = typedArray.getBoolean(R.styleable.DatePickerView_autoFitSize, true);
        typedArray.recycle();

        configurePickerViews();
    }

    private void configurePickerViews() {
        settlePickerView(yearPickerView);
        settlePickerView(monthPickerView);
        settlePickerView(dayPickerView);

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

    @Override
    public final void setOrientation(int orientation) {
        if (orientation != HORIZONTAL) {
            throw new RuntimeException("DatePickerView's orientation must be HORIZONTAL");
        }
        super.setOrientation(orientation);
    }

    public void setStartDate(Calendar date) {
        startDate = date;
        reloadData();
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

    private void settlePickerView(PickerView pickerView) {
        bindParams(pickerView);
        addPickerView(pickerView);
    }

    private void bindParams(PickerView pickerView) {
        pickerView.setPreferredMaxOffsetItemCount(preferredMaxOffsetItemCount);
        pickerView.setItemHeight(itemHeight);
        pickerView.setTextSize(textSize);
        pickerView.setTextColor(textColor);
        pickerView.setAutoFitSize(autoFitSize);
    }

    private void addPickerView(PickerView pickerView) {
        LinearLayout.LayoutParams layoutParams = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        addView(pickerView, layoutParams);
    }

    private void updateSelection() {
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

    private void reloadData() {
        updateSelection();
        yearPickerView.notifyDataSetChanged();
    }
}
