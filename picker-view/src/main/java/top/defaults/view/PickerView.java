package top.defaults.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;

import java.lang.ref.WeakReference;

import static top.defaults.view.Utils.checkNotNull;

public class PickerView extends View {

    private static final String TAG = "PickerView";

    static final int DEFAULT_MAX_OFFSET_ITEM_COUNT = 3;
    private int preferredMaxOffsetItemCount = DEFAULT_MAX_OFFSET_ITEM_COUNT;
    private int maxOffsetItemCount;
    private int selectedItemPosition = 0;

    private Adapter adapter;

    private Paint textPaint;
    private Rect textBounds = new Rect();

    private GestureDetector gestureDetector;
    private OverScroller scroller;
    private boolean pendingJustify;
    private float previousTouchedY;
    private float yOffset;
    private int minY;
    private int maxY;
    private int maxOverScrollY;

    private int itemHeight;
    private int textSize;
    private int textColor = Color.BLACK;
    private boolean isCyclic;
    private boolean autoFitSize;
    private Drawable selectedItemDrawable;
    private int[] DEFAULT_GRADIENT_COLORS = new int[]{0xcffafafa, 0x9ffafafa, 0x5ffafafa};
    private int[] gradientColors = DEFAULT_GRADIENT_COLORS;
    private GradientDrawable topMask;
    private GradientDrawable bottomMask;
    private Layout.Alignment textAlign = Layout.Alignment.ALIGN_CENTER;

    public interface PickerItem {
        String getText();
    }

    public PickerView(Context context) {
        this(context, null);
    }

    public PickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (yOffset < minY || yOffset > maxY) return false;

                scroller.fling(
                        0, (int) yOffset,
                        0, (int) velocityY,
                        0, 0,
                        minY,
                        maxY,
                        0, maxOverScrollY);
                Log.d(TAG, "fling: " + yOffset + ", velocityY: " + velocityY);

                pendingJustify = true;
                return true;
            }
        });
        scroller = new OverScroller(getContext());

        if (isInEditMode()) {
            adapter = new Adapter() {
                @Override
                public int getItemCount() {
                    return getMaxCount();
                }

                @Override
                public PickerItem getItem(final int index) {
                    return new PickerItem() {
                        @Override
                        public String getText() {
                            return "Item " + index;
                        }
                    };
                }
            };
        } else {
            selectedItemDrawable = Utils.getDrawable(getContext(), R.drawable.top_defaults_view_pickerview_selected_item);
        }

        topMask = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, gradientColors);
        bottomMask = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, gradientColors);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PickerView);
        preferredMaxOffsetItemCount = typedArray.getInt(R.styleable.PickerView_preferredMaxOffsetItemCount, DEFAULT_MAX_OFFSET_ITEM_COUNT);
        if (preferredMaxOffsetItemCount <= 0) preferredMaxOffsetItemCount = DEFAULT_MAX_OFFSET_ITEM_COUNT;

        int defaultItemHeight = Utils.pixelOfDp(getContext(), 32);
        itemHeight = typedArray.getDimensionPixelSize(R.styleable.PickerView_itemHeight, defaultItemHeight);
        if (itemHeight <= 0) itemHeight = defaultItemHeight;

        int defaultTextSize = Utils.pixelOfScaled(getContext(), 22);
        textSize = typedArray.getDimensionPixelSize(R.styleable.PickerView_textSize, defaultTextSize);
        textColor = typedArray.getColor(R.styleable.PickerView_textColor, Color.BLACK);

        isCyclic = typedArray.getBoolean(R.styleable.PickerView_isCyclic, false);
        autoFitSize = typedArray.getBoolean(R.styleable.PickerView_autoFitSize, true);
        typedArray.recycle();

        initPaints();
    }

    private void initPaints() {
        textPaint = new Paint();
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        textPaint.setAntiAlias(true);
    }

    public Adapter getAdapter() {
        return adapter;
    }

    public void setAdapter(final Adapter adapter) {
        checkNotNull(adapter, "adapter == null");
        if (adapter.getItemCount() > Integer.MAX_VALUE / itemHeight) {
            throw new RuntimeException("getItemCount() is too large, max count can be PickerView.getMaxCount()");
        }

        adapter.setPickerView(this);
        this.adapter = adapter;
    }

    protected int getMaxCount() {
        return Integer.MAX_VALUE / itemHeight;
    }

    public void notifyDataSetChanged() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public abstract static class Adapter {
        private WeakReference<PickerView> pickerViewRef;

        private void setPickerView(PickerView pickerView) {
            this.pickerViewRef = new WeakReference<>(pickerView);
        }

        public void notifyDataSetChanged() {
            if (pickerViewRef != null) {
                PickerView pickerView = pickerViewRef.get();
                if (pickerView != null) {
                    pickerView.notifySelectedItemChangedIfNeeded(pickerView.selectedItemPosition, true);
                    pickerView.updateSelectedItem();
                    pickerView.computeScrollParams();
                    if (!pickerView.scroller.isFinished()) {
                        pickerView.scroller.forceFinished(true);
                    }
                    pickerView.justify(0);
                    pickerView.invalidate();
                }
            }
        }

        public abstract int getItemCount();
        public abstract PickerItem getItem(int index);

        public String getText(int index) {
            if (getItem(index) == null) return "null";
            return getItem(index).getText();
        }
    }

    public void setPreferredMaxOffsetItemCount(int preferredMaxOffsetItemCount) {
        this.preferredMaxOffsetItemCount = preferredMaxOffsetItemCount;
    }

    public void setItemHeight(int itemHeight) {
        if (this.itemHeight != itemHeight) {
            this.itemHeight = itemHeight;
            invalidate();
            requestLayout();
        }
    }

    public void setTextSize(int textSize) {
        if (this.textSize != textSize) {
            this.textSize = textSize;
            invalidate();
        }
    }

    public void setTextColor(int textColor) {
        if (this.textColor != textColor) {
            this.textColor = textColor;
            invalidate();
        }
    }

    public void setCyclic(boolean cyclic) {
        if (this.isCyclic != cyclic) {
            isCyclic = cyclic;
            invalidate();
        }
    }

    public void setAutoFitSize(boolean autoFitSize) {
        if (this.autoFitSize != autoFitSize) {
            this.autoFitSize = autoFitSize;
            invalidate();
        }
    }

    public int getSelectedItemPosition() {
        return clampItemPosition(selectedItemPosition);
    }

    public void setSelectedItemPosition(int selectedItemPosition) {
        checkNotNull(adapter, "adapter must be set first");

        notifySelectedItemChangedIfNeeded(selectedItemPosition);
        computeYOffset();
        invalidate();
    }

    public interface OnSelectedItemChangedListener {
        void onSelectedItemChanged(PickerView pickerView, int previousPosition, int selectedItemPosition);
    }

    private OnSelectedItemChangedListener onSelectedItemChangedListener;

    public void setOnSelectedItemChangedListener(OnSelectedItemChangedListener onSelectedItemChangedListener) {
        this.onSelectedItemChangedListener = onSelectedItemChangedListener;
    }

    private void notifySelectedItemChangedIfNeeded(int newSelectedItemPosition) {
        notifySelectedItemChangedIfNeeded(newSelectedItemPosition, false);
    }

    private void notifySelectedItemChangedIfNeeded(int newSelectedItemPosition, boolean forceNotify) {
        int clampedOldSelectedItemPosition = clampItemPosition(selectedItemPosition);
        int clampedNewSelectedItemPosition = clampItemPosition(newSelectedItemPosition);

        boolean changed = forceNotify;
        if (isCyclic) {
            if (selectedItemPosition != newSelectedItemPosition) {
                selectedItemPosition = newSelectedItemPosition;
                changed = true;
            }
        } else {
            if (selectedItemPosition != clampedNewSelectedItemPosition) {
                selectedItemPosition = clampedNewSelectedItemPosition;
                changed = true;
            }
        }

        if (changed && onSelectedItemChangedListener != null) {
            onSelectedItemChangedListener.onSelectedItemChanged(this, clampedOldSelectedItemPosition, clampedNewSelectedItemPosition);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        checkNotNull(adapter, "adapter == null");

        int height = resolveSizeAndState((1 + 2 * preferredMaxOffsetItemCount) * itemHeight, heightMeasureSpec, 0);
        int realHeight = height & ~View.MEASURED_STATE_MASK;

        maxOffsetItemCount = (int) Math.ceil((realHeight / itemHeight - 1) / 2.f);
        computeYOffset();
        computeScrollParams();
        setMeasuredDimension(widthMeasureSpec, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        checkNotNull(adapter, "adapter == null");
        if (adapter.getItemCount() == 0 || itemHeight == 0) return;

        if (!isInEditMode()) {
            selectedItemDrawable.setBounds(0, maxOffsetItemCount * itemHeight, getMeasuredWidth(), (maxOffsetItemCount + 1) * itemHeight);
            selectedItemDrawable.draw(canvas);
        }

        drawItems(canvas);
        drawMasks(canvas);
    }

    private void drawItems(Canvas canvas) {
        updateSelectedItem();

        float drawYOffset = this.yOffset;
        drawYOffset += (selectedItemPosition - maxOffsetItemCount) * itemHeight;

        // 上下多绘制一个item
        int start = selectedItemPosition - maxOffsetItemCount - 1;
        int end = selectedItemPosition + maxOffsetItemCount + 1;

        // 向上便宜一个item作为起始点
        drawYOffset -= itemHeight;

        for (int i = start; i <= end; i++) {
            if (i < 0) {
                if (isCyclic) {
                    String text = adapter.getText(clampItemPosition(i));
                    drawText(canvas, text, drawYOffset);
                }
                drawYOffset += itemHeight;
                continue;
            } else if (i >= adapter.getItemCount()) {
                if (isCyclic) {
                    String text = adapter.getText(clampItemPosition(i));
                    drawText(canvas, text, drawYOffset);
                }
                drawYOffset += itemHeight;
                continue;
            }

            String text = adapter.getText(i);
            drawText(canvas, text, drawYOffset);
            drawYOffset += itemHeight;
        }
    }

    private void drawMasks(Canvas canvas) {
        topMask.setBounds(0, 0, getMeasuredWidth(), maxOffsetItemCount * itemHeight);
        topMask.draw(canvas);

        bottomMask.setBounds(0, (maxOffsetItemCount + 1) * itemHeight, getMeasuredWidth(), getMeasuredHeight());
        bottomMask.draw(canvas);
    }

    private void drawText(Canvas canvas, String text, float offset) {
        textPaint.setTextSize(textSize);
        textPaint.getTextBounds(text, 0, text.length(), textBounds);

        if (autoFitSize) {
            while (getMeasuredWidth() < textBounds.width() && textPaint.getTextSize() > 16) {
                textPaint.setTextSize(textPaint.getTextSize() - 1);
                textPaint.getTextBounds(text, 0, text.length(), textBounds);
            }
        }

        float textBottom = offset + (itemHeight + (textBounds.height())) / 2;

        if (textAlign == Layout.Alignment.ALIGN_CENTER) {
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(text, getMeasuredWidth() / 2, textBottom, textPaint);
        } else if (textAlign == Layout.Alignment.ALIGN_OPPOSITE) {
            textPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(text, getMeasuredWidth(), textBottom, textPaint);
        } else {
            textPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(text, 0, textBottom, textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            invalidate();
            return true;
        }

        float dy;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!scroller.isFinished()) {
                    scroller.forceFinished(true);
                }
                previousTouchedY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                dy = event.getY() - previousTouchedY;
                yOffset += dy;
                clampYOffset();
                previousTouchedY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
            default:
                // align items
                dy = event.getY() - previousTouchedY;
                yOffset += dy;
                justify(250);
                break;
        }

        invalidate();
        return true;
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            yOffset = scroller.getCurrY();
            clampYOffset();
            invalidate();
        } else {
            if (pendingJustify) {
                justify(250);
                pendingJustify = false;
            }
        }
    }

    private void computeScrollParams() {
        if (isCyclic) {
            minY = Integer.MIN_VALUE;
            maxY = Integer.MAX_VALUE;
        } else {
            minY = -(adapter.getItemCount() - 1 - maxOffsetItemCount) * itemHeight;
            maxY = maxOffsetItemCount * itemHeight;
        }
        maxOverScrollY = (maxOffsetItemCount > 3 ? 3 : maxOffsetItemCount) * itemHeight;
    }

    private void updateSelectedItem() {
        float centerPosition = centerPosition();
        int newSelectedItemPosition = (int) Math.floor(centerPosition);
        notifySelectedItemChangedIfNeeded(newSelectedItemPosition);
    }

    // 计算selectedItem的offset
    private void computeYOffset() {
        yOffset = (maxOffsetItemCount - selectedItemPosition) * itemHeight;
    }

    private void clampYOffset() {
        if (isCyclic) return;

        int itemCount = adapter.getItemCount();

        float maxYOffset = maxOffsetItemCount * itemHeight + maxOverScrollY;
        float minYOffset = ((float) (maxOffsetItemCount - itemCount + 1)) * itemHeight - maxOverScrollY;

        if (yOffset > maxYOffset) {
            yOffset = maxYOffset;
        } else if (yOffset < minYOffset) {
            yOffset = minYOffset;
        }
    }

    private int computeFinalYOffset() {
        float centerPosition = centerPosition();
        int centerItem = (int) Math.floor(centerPosition);

        if (!isCyclic) {
            centerItem = clampItemPosition(centerItem);
        }
        return (maxOffsetItemCount - centerItem) * itemHeight;
    }

    private int clampItemPosition(int itemPosition) {
        if (adapter.getItemCount() == 0) return 0;

        if (isCyclic) {
            if (itemPosition < 0) {
                itemPosition = itemPosition % adapter.getItemCount();
                if (itemPosition != 0) itemPosition += adapter.getItemCount();
            } else if (itemPosition >= adapter.getItemCount()) itemPosition %= adapter.getItemCount();
        }

        if (itemPosition < 0) itemPosition = 0;
        else if (itemPosition >= adapter.getItemCount()) itemPosition = adapter.getItemCount() - 1;
        return itemPosition;
    }

    // 中心线切割的单元位置
    private float centerPosition() {
        return (float) (maxOffsetItemCount + 0.5 - yOffset / itemHeight);
    }

    private void justify(int duration) {
        int finalY = computeFinalYOffset();

        Log.d(TAG, "justify: duration = " + duration + ", yOffset = " + yOffset + ", finalY = " + finalY);

        if (finalY != yOffset) {
            scroller.startScroll(
                    0, (int) yOffset,
                    0, (int) (finalY - yOffset),
                    duration);

            Log.d(TAG, "startScroll: " + yOffset + ", to: " + finalY);
            invalidate();
        }
    }
}
