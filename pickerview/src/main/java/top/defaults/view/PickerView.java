package top.defaults.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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

    private static final int DEFAULT_MAX_OFFSET_ITEM_COUNT = 2;
    private int preferredMaxOffsetItemCount = DEFAULT_MAX_OFFSET_ITEM_COUNT;
    private int maxOffsetItemCount = preferredMaxOffsetItemCount;
    private int selectedItemPosition = 0;

    private Adapter adapter;

    private Paint textPaint;
    private int textSize;
    private Rect textBounds = new Rect();

    private GestureDetector gestureDetector;
    private OverScroller scroller;
    private boolean pendingJustify;
    private float previousTouchedY;
    private float yOffset;
    private int minY;
    private int maxY;
    private int maxOverScrollY;

    private boolean isCyclic = false;
    private Drawable selectedItemDrawable;
    private int[] DEFAULT_GRADIENT_COLORS = new int[]{0xcffafafa, 0x9ffafafa, 0x5ffafafa};
    private int[] gradientColors = DEFAULT_GRADIENT_COLORS;
    private GradientDrawable topMask;
    private GradientDrawable bottomMask;

    private Layout.Alignment textAlign = Layout.Alignment.ALIGN_CENTER;

    public PickerView(Context context) {
        this(context, null);
    }

    public PickerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
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

        selectedItemDrawable = ContextCompat.getDrawable(getContext(), R.drawable.top_defaults_view_pickerview_selected_item);
        topMask = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, gradientColors);
        bottomMask = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, gradientColors);

        textSize = Utils.pixelOfScaled(getContext(), 14);
        initPaints();
    }

    private void initPaints() {
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);
    }

    public void setPreferredMaxOffsetItemCount(int preferredMaxOffsetItemCount) {
        this.preferredMaxOffsetItemCount = preferredMaxOffsetItemCount;
    }

    public Adapter getAdapter() {
        return adapter;
    }

    public void setAdapter(final Adapter adapter) {
        checkNotNull(adapter, "adapter == null");
        if (adapter.getItemCount() > Integer.MAX_VALUE / adapter.getItemHeight()) {
            Log.w(TAG, "getItemCount() is too large, unsupported yet");
        }

        adapter.setPickerView(this);
        this.adapter = adapter;
    }

    public abstract static class Adapter {
        private WeakReference<PickerView> pickerViewRef;

        private void setPickerView(PickerView pickerView) {
            this.pickerViewRef = new WeakReference<>(pickerView);
        }

        public void notifyDataSetChanged() {
            PickerView pickerView = pickerViewRef.get();
            if (pickerViewRef != null && pickerView != null) {
                pickerView.invalidate();
            }
        }

        public abstract int getItemCount();
        public abstract int getItemHeight();
        public abstract String getText(int index);
    }

    public int getSelectedItemPosition() {
        return selectedItemPosition;
    }

    public void setSelectedItemPosition(int selectedItemPosition) {
        checkNotNull(adapter, "adapter must be set first");

        notifySelectedItemChangedIfNeeded(clampItemPosition(selectedItemPosition));
        this.selectedItemPosition = clampItemPosition(selectedItemPosition);
        computeYOffset();
        invalidate();
    }

    public interface OnSelectedItemChangedListener {
        void onSelectedItemChanged(PickerView pickerView, int selectedItemPosition);
    }

    private OnSelectedItemChangedListener onSelectedItemChangedListener;

    public void setOnSelectedItemChangedListener(OnSelectedItemChangedListener onSelectedItemChangedListener) {
        this.onSelectedItemChangedListener = onSelectedItemChangedListener;
    }

    private void notifySelectedItemChangedIfNeeded(int newSelectedItemPosition) {
        if (clampItemPosition(selectedItemPosition) != clampItemPosition(newSelectedItemPosition)) {
            if (onSelectedItemChangedListener != null) {
                onSelectedItemChangedListener.onSelectedItemChanged(this, newSelectedItemPosition);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        checkNotNull(adapter, "adapter == null");

        int itemHeight = adapter.getItemHeight();
        int height = resolveSizeAndState((1 + 2 * preferredMaxOffsetItemCount) * itemHeight, heightMeasureSpec, 0);
        int realHeight = height & ~View.MEASURED_STATE_MASK;

        maxOffsetItemCount = (int) Math.ceil((realHeight / adapter.getItemHeight() - 1) / 2.f);
        computeYOffset();
        if (isCyclic) {
            minY = Integer.MIN_VALUE;
            maxY = Integer.MAX_VALUE;
        } else {
            minY = -(adapter.getItemCount() - 1 - maxOffsetItemCount) * adapter.getItemHeight();
            maxY = maxOffsetItemCount * adapter.getItemHeight();
        }
        maxOverScrollY = (maxOffsetItemCount > 3 ? 3 : maxOffsetItemCount) * adapter.getItemHeight();

        setMeasuredDimension(widthMeasureSpec, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        checkNotNull(adapter, "adapter == null");

        selectedItemDrawable.setBounds(0, maxOffsetItemCount * adapter.getItemHeight(), getMeasuredWidth(), (maxOffsetItemCount + 1) * adapter.getItemHeight());
        selectedItemDrawable.draw(canvas);

        drawItems(canvas);
        drawMasks(canvas);
    }

    private void drawItems(Canvas canvas) {
        updateSelectedItem();

        int itemHeight = adapter.getItemHeight();
        float drawYOffset = this.yOffset;
        drawYOffset += (selectedItemPosition - maxOffsetItemCount) * itemHeight;

        int start = selectedItemPosition - maxOffsetItemCount;
        int end = selectedItemPosition + maxOffsetItemCount + 1;

        // 绘制最上方的一个仅显示一部分的item
        if (start > 0) {
            start--;
            drawYOffset -= itemHeight;
        }

        for (int i = start; i <= end; i++) {
            if (i < 0) {
                if (isCyclic) {
                    String text = adapter.getText(i % adapter.getItemCount() + adapter.getItemCount());
                    drawText(canvas, text, drawYOffset);
                }
                drawYOffset += itemHeight;
                continue;
            } else if (i >= adapter.getItemCount()) {
                if (isCyclic) {
                    String text = adapter.getText(i % adapter.getItemCount());
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
        topMask.setBounds(0, 0, getMeasuredWidth(), maxOffsetItemCount * adapter.getItemHeight());
        topMask.draw(canvas);

        bottomMask.setBounds(0, (maxOffsetItemCount + 1) * adapter.getItemHeight(), getMeasuredWidth(), getMeasuredHeight());
        bottomMask.draw(canvas);
    }

    private void drawText(Canvas canvas, String text, float offset) {
        textPaint.getTextBounds(text, 0, text.length(), textBounds);
        float textBottom = offset + (adapter.getItemHeight() + (textBounds.height())) / 2;

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
                clampYOffset(maxOverScrollY);
                previousTouchedY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
            default:
                // align items
                dy = event.getY() - previousTouchedY;
                yOffset += dy;
                justify(500);
                break;
        }

        invalidate();
        return true;
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            yOffset = scroller.getCurrY();
            clampYOffset(maxOverScrollY);
            invalidate();
        } else {
            if (pendingJustify) {
                if (justify(250)) {
                    invalidate();
                }
                pendingJustify = false;
            }
        }
    }

    private void updateSelectedItem() {
        float centerPosition = centerPosition();
        int newSelectedItemPosition = clampItemPosition((int) Math.floor(centerPosition));
        notifySelectedItemChangedIfNeeded(newSelectedItemPosition);
        selectedItemPosition = newSelectedItemPosition;
    }

    // 计算selectedItem的offset
    private void computeYOffset() {
        yOffset = (maxOffsetItemCount - selectedItemPosition) * adapter.getItemHeight();
    }

    private void clampYOffset(int overY) {
        if (isCyclic) return;

        boolean clamped = false;
        int itemHeight = adapter.getItemHeight();
        int itemCount = adapter.getItemCount();

        float maxYOffset = maxOffsetItemCount * itemHeight + overY;
        float minYOffset = ((float) (maxOffsetItemCount - itemCount + 1)) * itemHeight - overY;

        if (yOffset > maxYOffset) {
            yOffset = maxYOffset;
            clamped = true;
        } else if (yOffset < minYOffset) {
            yOffset = minYOffset;
            clamped = true;
        }
        if (clamped) scroller.forceFinished(true);
    }

    private int computeFinalYOffset() {
        float centerPosition = centerPosition();
        int centerItem = (int) Math.floor(centerPosition);

        if (!isCyclic) {
            centerItem = clampItemPosition(centerItem);
        }
        return (maxOffsetItemCount - centerItem) * adapter.getItemHeight();
    }

    private int clampItemPosition(int itemPosition) {
        if (isCyclic) {
            if (itemPosition < 0) itemPosition = itemPosition % adapter.getItemCount() + adapter.getItemCount();
            else if (itemPosition >= adapter.getItemCount()) itemPosition %= adapter.getItemCount();
        }

        if (itemPosition < 0) itemPosition = 0;
        else if (itemPosition >= adapter.getItemCount()) itemPosition = adapter.getItemCount() - 1;
        return itemPosition;
    }

    // 中心线切割的单元位置
    private float centerPosition() {
        return (float) (maxOffsetItemCount + 0.5 - yOffset / adapter.getItemHeight());
    }

    private boolean justify(int duration) {
        int finalY = computeFinalYOffset();
        if (finalY != yOffset) {
            scroller.startScroll(
                    0, (int) yOffset,
                    0, (int) (finalY - yOffset),
                    duration);

            Log.d(TAG, "startScroll: " + yOffset + ", to: " + finalY);
            return true;
        }

        return false;
    }
}
