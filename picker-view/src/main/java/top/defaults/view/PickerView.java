package top.defaults.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;

import java.lang.ref.WeakReference;

import top.defaults.logger.Logger;

import static top.defaults.view.Utils.checkNotNull;

@SuppressWarnings("unused")
public class PickerView extends View {

    private static final String TAG = "PickerView";
    private static final boolean DEBUG = false;

    static final int DEFAULT_MAX_OFFSET_ITEM_COUNT = 3;
    private int preferredMaxOffsetItemCount = DEFAULT_MAX_OFFSET_ITEM_COUNT;
    private int selectedItemPosition;

    private Adapter adapter;

    private Paint textPaint;
    private Rect textBounds = new Rect();

    private GestureDetector gestureDetector;
    private OverScroller scroller;
    private boolean pendingJustify;
    private float previousTouchedY;
    private int previousScrollerY;
    private int yOffset;
    private int minY;
    private int maxY;
    private int maxOverScrollY;

    private int itemHeight;
    private int textSize;
    private int textColor = Color.BLACK;
    private boolean isCyclic;
    private boolean autoFitSize;
    private boolean curved;
    private Drawable selectedItemDrawable;
    private int[] DEFAULT_GRADIENT_COLORS = new int[]{0xcffafafa, 0x9ffafafa, 0x5ffafafa};
    private int[] gradientColors = DEFAULT_GRADIENT_COLORS;
    private GradientDrawable topMask;
    private GradientDrawable bottomMask;
    private Layout.Alignment textAlign = Layout.Alignment.ALIGN_CENTER;

    private float radius;
    private Camera camera;
    private Matrix matrix;

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
                previousScrollerY = yOffset - itemHeight * selectedItemPosition;
                scroller.fling(
                        0, previousScrollerY,
                        0, (int) velocityY,
                        0, 0,
                        minY,
                        maxY,
                        0, maxOverScrollY);

                if (DEBUG) {
                    Logger.d(TAG, "fling: " + previousScrollerY + ", velocityY: " + velocityY);
                }

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
        curved = typedArray.getBoolean(R.styleable.PickerView_curved, false);
        typedArray.recycle();

        initPaints();

        camera = new Camera();
        matrix = new Matrix();
    }

    private void initPaints() {
        textPaint = new Paint();
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

    @SuppressWarnings("WeakerAccess")
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

    public void setCurved(boolean curved) {
        if (this.curved != curved) {
            this.curved = curved;
            invalidate();
            requestLayout();
        }
    }

    public int getSelectedItemPosition() {
        return clampItemPosition(selectedItemPosition);
    }

    public void setSelectedItemPosition(int selectedItemPosition) {
        checkNotNull(adapter, "adapter must be set first");

        notifySelectedItemChangedIfNeeded(selectedItemPosition);
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

        int height = resolveSizeAndState(calculateIntrinsicHeight(), heightMeasureSpec, 0);
        computeScrollParams();
        setMeasuredDimension(widthMeasureSpec, height);
    }

    private int calculateIntrinsicHeight() {
        if (curved) {
            radius = itemHeight / (float) Math.sin(Math.PI / (3 + 2 * preferredMaxOffsetItemCount));
            return (int) Math.ceil(2 * radius);
        } else {
            return (1 + 2 * preferredMaxOffsetItemCount) * itemHeight;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        checkNotNull(adapter, "adapter == null");
        if (adapter.getItemCount() == 0 || itemHeight == 0) return;

        if (!isInEditMode()) {
            selectedItemDrawable.setBounds(0, (getMeasuredHeight() - itemHeight) / 2, getMeasuredWidth(), (getMeasuredHeight() + itemHeight) / 2);
            selectedItemDrawable.draw(canvas);
        }

        drawItems(canvas);
        drawMasks(canvas);
    }

    private void drawItems(Canvas canvas) {
         // 绘制选中项
        float drawYOffset = yOffset + (getMeasuredHeight() - itemHeight) / 2;
        int itemPosition = selectedItemPosition;
        String text = adapter.getText(clampItemPosition(itemPosition));
        drawText(canvas, text, drawYOffset);
        drawYOffset -= itemHeight;

        // 绘制选中项上方的item
        itemPosition = selectedItemPosition - 1;
        while (drawYOffset + (itemHeight * (curved ? 2 : 1)) > 0) {
            if (isPositionInvalid(itemPosition) && !isCyclic) {
                break;
            }

            text = adapter.getText(clampItemPosition(itemPosition));
            drawText(canvas, text, drawYOffset);
            drawYOffset -= itemHeight;
            itemPosition--;
        }

        // 绘制选中项下方的item
        drawYOffset = yOffset + (getMeasuredHeight() + itemHeight) / 2;
        itemPosition = selectedItemPosition + 1;
        while (drawYOffset - (itemHeight * (curved ? 1: 0)) < getMeasuredHeight()) {
            if (isPositionInvalid(itemPosition) && !isCyclic) {
                break;
            }

            text = adapter.getText(clampItemPosition(itemPosition));
            drawText(canvas, text, drawYOffset);
            drawYOffset += itemHeight;
            itemPosition++;
        }
    }

    private void drawMasks(Canvas canvas) {
        topMask.setBounds(0, 0, getMeasuredWidth(), (getMeasuredHeight() - itemHeight) / 2);
        topMask.draw(canvas);

        bottomMask.setBounds(0, (getMeasuredHeight() + itemHeight) / 2, getMeasuredWidth(), getMeasuredHeight());
        bottomMask.draw(canvas);
    }

    private void drawText(Canvas canvas, String text, float offset) {
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        textPaint.getTextBounds(text, 0, text.length(), textBounds);

        if (autoFitSize) {
            while (getMeasuredWidth() < textBounds.width() && textPaint.getTextSize() > 16) {
                textPaint.setTextSize(textPaint.getTextSize() - 1);
                textPaint.getTextBounds(text, 0, text.length(), textBounds);
            }
        }

        float textBottom = offset + (itemHeight + (textBounds.height())) / 2;

        if (curved) {
            // 根据当前item的offset换算得到对应的倾斜角度，rotateRatio用于减小倾斜角度，否则倾斜角度过大会导致视觉效果不佳
            float rotateRatio = 2f / preferredMaxOffsetItemCount;
            double radian = Math.atan((radius - (offset + itemHeight / 2)) / radius) * rotateRatio;
            float degree = (float) (radian * 180 / Math.PI);
            camera.save();
            camera.rotateX(degree);
            camera.translate(0, 0, - Math.abs((radius / (2 + preferredMaxOffsetItemCount)) * (float) Math.sin(radian)));
            camera.getMatrix(matrix);
            matrix.preTranslate(-getMeasuredWidth() / 2, -getMeasuredHeight() / 2);
            matrix.postTranslate(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
            canvas.save();
            canvas.concat(matrix);
        }
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
        if (curved) {
            canvas.restore();
            camera.restore();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            invalidate();
            return true;
        }

        int dy;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pendingJustify = false;
                if (!scroller.isFinished()) {
                    scroller.forceFinished(true);
                }
                previousTouchedY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                pendingJustify = false;
                dy = (int) (event.getY() - previousTouchedY);
                handleOffset(dy);
                previousTouchedY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                // align items
                dy = (int) (event.getY() - previousTouchedY);
                handleOffset(dy);
                justify(250);
                break;
        }

        invalidate();
        return true;
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            int dy = scroller.getCurrY() - previousScrollerY;
            handleOffset(dy);
            previousScrollerY = scroller.getCurrY();
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
            minY = -(adapter.getItemCount() - 1) * itemHeight;
            maxY = 0;
        }
        maxOverScrollY = 2 * itemHeight;
    }

    private void updateSelectedItem() {
        float centerPosition = centerPosition();
        int newSelectedItemPosition = (int) Math.floor(centerPosition);
        notifySelectedItemChangedIfNeeded(newSelectedItemPosition);
    }

    private boolean isPositionInvalid(int itemPosition) {
        return itemPosition < 0 || itemPosition >= adapter.getItemCount();
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
        return selectedItemPosition + 0.5f - yOffset / itemHeight;
    }

    // 对齐item
    private void justify(int duration) {
        if (yOffset != 0) {
            int scrollOffset = -yOffset;
            if (yOffset > 0) {
                if (yOffset > itemHeight / 2) {
                    scrollOffset = itemHeight - yOffset;
                }
            } else {
                if (Math.abs(yOffset) > itemHeight / 2) {
                    scrollOffset = -(itemHeight + yOffset);
                }
            }

            previousScrollerY = yOffset - itemHeight * selectedItemPosition;
            scroller.startScroll(
                    0, previousScrollerY,
                    0, scrollOffset,
                    duration);
            if (DEBUG) {
                Logger.d(TAG, "justify: duration = " + duration + ", yOffset = " + yOffset + ", scrollOffset = " + scrollOffset);
            }

            invalidate();
        }
    }

    private void handleOffset(int dy) {
        yOffset += dy;
        if (DEBUG) {
            Logger.d(TAG, "yOffset = " + yOffset + ", dy = " + dy);
        }

        if (Math.abs(yOffset) >= itemHeight) {
            // 滚动到边界时
            if (selectedItemPosition == 0 && dy >= 0 || selectedItemPosition == adapter.getItemCount() - 1 && dy <= 0) {
                if (Math.abs(yOffset) > maxOverScrollY) {
                    yOffset = yOffset > 0 ? maxOverScrollY : - maxOverScrollY;
                }
                return;
            }

            int preSelection = selectedItemPosition;
            notifySelectedItemChangedIfNeeded(selectedItemPosition - (yOffset / itemHeight));
            yOffset -= (preSelection - selectedItemPosition) * itemHeight;
        }
    }
}
