package top.defaults.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import static top.defaults.view.PickerView.DEFAULT_MAX_OFFSET_ITEM_COUNT;

@SuppressWarnings("unused")
public class PickerViewGroup extends LinearLayout {

    protected int preferredMaxOffsetItemCount = DEFAULT_MAX_OFFSET_ITEM_COUNT;
    protected int itemHeight;
    protected int textSize;
    protected int textColor = Color.BLACK;
    protected boolean autoFitSize;
    protected boolean curved;

    public PickerViewGroup(Context context) {
        this(context, null);
    }

    public PickerViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PickerViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PickerViewGroup);
        preferredMaxOffsetItemCount = typedArray.getInt(R.styleable.PickerViewGroup_preferredMaxOffsetItemCount, DEFAULT_MAX_OFFSET_ITEM_COUNT);
        int defaultItemHeight = Utils.pixelOfDp(getContext(), PickerView.DEFAULT_ITEM_HEIGHT_IN_DP);
        itemHeight = typedArray.getDimensionPixelSize(R.styleable.PickerViewGroup_itemHeight, defaultItemHeight);
        int defaultTextSize = Utils.pixelOfScaled(getContext(), PickerView.DEFAULT_TEXT_SIZE_IN_SP);
        textSize = typedArray.getDimensionPixelSize(R.styleable.PickerViewGroup_textSize, defaultTextSize);
        textColor = typedArray.getColor(R.styleable.PickerViewGroup_textColor, Color.BLACK);
        autoFitSize = typedArray.getBoolean(R.styleable.PickerViewGroup_autoFitSize, true);
        curved = typedArray.getBoolean(R.styleable.PickerViewGroup_curved, false);
        typedArray.recycle();
    }

    @Override
    public final void setOrientation(int orientation) {
        if (orientation != HORIZONTAL) {
            throw new RuntimeException("DatePickerView's orientation must be HORIZONTAL");
        }
        super.setOrientation(orientation);
    }

    public void setCurved(boolean curved) {
        this.curved = curved;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            PickerView pickerView = (PickerView) getChildAt(i);
            pickerView.setCurved(curved);
        }
    }

    protected void settlePickerView(PickerView pickerView) {
        settlePickerView(pickerView, false);
    }

    protected void settlePickerView(PickerView pickerView, boolean narrow) {
        if (pickerView == null) return;
        bindParams(pickerView);
        addPickerView(pickerView, narrow);
    }

    protected void bindParams(PickerView pickerView) {
        pickerView.setPreferredMaxOffsetItemCount(preferredMaxOffsetItemCount);
        pickerView.setItemHeight(itemHeight);
        pickerView.setTextSize(textSize);
        pickerView.setTextColor(textColor);
        pickerView.setAutoFitSize(autoFitSize);
        pickerView.setCurved(curved);
    }

    protected void addPickerView(PickerView pickerView, boolean narrow) {
        LinearLayout.LayoutParams layoutParams = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, narrow ? 1 : 2);
        addView(pickerView, layoutParams);
    }
}
