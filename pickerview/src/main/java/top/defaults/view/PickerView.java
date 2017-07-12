package top.defaults.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;

import static top.defaults.view.Utils.checkNotNull;

public class PickerView extends View {

    private static final String TAG = "PickerView";

    private static final int DEFAULT_MAX_OFFSET_ITEM = 2;
    private int maxOffsetItem = DEFAULT_MAX_OFFSET_ITEM;
    private int selectedItem = 0;

    private Adapter adapter;

    private Paint textPaint;
    private Paint selectedTextPaint;
    private Paint backgroundPaint;
    private int textSize;
    private Rect textBounds = new Rect();

    private GestureDetector gestureDetector;
    private OverScroller scroller;
    private float previousTouchedY;
    private float yOffset;
    private int minY;
    private int maxY;
    private int maxOverScrollY;

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
                return true;
            }
        });
        scroller = new OverScroller(getContext());

        textSize = Utils.pixelOfScaled(getContext(), 14);
        initPaints();
    }

    private void initPaints() {
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);

        selectedTextPaint = new Paint();
        selectedTextPaint.setColor(Color.WHITE);
        selectedTextPaint.setTextSize(textSize);
        selectedTextPaint.setAntiAlias(true);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.GRAY);
    }

    public void setMaxOffsetItem(int maxOffsetItem) {
        this.maxOffsetItem = maxOffsetItem;
    }

    public void setAdapter(final Adapter adapter) {
        checkNotNull(adapter, "adapter == null");

        this.adapter = adapter;
        yOffset = computeYOffset();
        minY = -(adapter.getItemCount() - 1 - maxOffsetItem) * adapter.getItemHeight();
        maxY = maxOffsetItem * adapter.getItemHeight();
        maxOverScrollY = maxOffsetItem * adapter.getItemHeight();
    }

    public abstract static class Adapter {
        protected abstract int getItemCount();
        protected abstract int getItemHeight();
        protected abstract String getText(int index);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        checkNotNull(adapter, "adapter == null");

        int itemHeight = adapter.getItemHeight();
        int height = resolveSizeAndState((1 + 2 * maxOffsetItem) * itemHeight, heightMeasureSpec, 0);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        checkNotNull(adapter, "adapter == null");

        updateSelectedItem();

        int itemHeight = adapter.getItemHeight();
        float drawYOffset = this.yOffset;
        if (selectedItem > maxOffsetItem) {
            drawYOffset += (selectedItem - maxOffsetItem) * itemHeight;
        }

        int start = selectedItem - maxOffsetItem;
        int end = selectedItem + maxOffsetItem + 1;

        // 绘制最上方的一个仅显示一部分的item
        if (start > 0) {
            start--;
            drawYOffset -= itemHeight;
        }

        for (int i = start; i <= end; i++) {
            if (i < 0) {
                continue;
            } else if (i >= adapter.getItemCount()) {
                break;
            }

            String text = adapter.getText(i);
            textPaint.getTextBounds(text, 0, text.length(), textBounds);
            canvas.drawLine(0, drawYOffset + itemHeight, getMeasuredWidth(), drawYOffset + itemHeight, backgroundPaint);
            if (i == selectedItem) {
                canvas.drawText(text, 0, drawYOffset + (itemHeight + (textBounds.height())) / 2, selectedTextPaint);
            } else {
                canvas.drawText(text, 0, drawYOffset + (itemHeight + (textBounds.height())) / 2, textPaint);
            }
            drawYOffset += itemHeight;
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
                int finalY = computeFinalYOffset();
                scroller.startScroll(
                        0, (int) yOffset,
                        0, (int) (finalY - yOffset),
                        500);

                Log.d(TAG, "startScroll: " + yOffset + ", to: " + finalY);
                break;
        }

        invalidate();
        return true;
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            yOffset = scroller.getCurrY();

            Log.d(TAG, "yOffset: " + yOffset);

            clampYOffset(maxOverScrollY);
            invalidate();
        }
    }

    private void updateSelectedItem() {
        float centerPosition = centerPosition();
        selectedItem = (int) Math.floor(centerPosition);
        Log.d(TAG, "selectedItem: " + selectedItem);
    }

    // 计算selectedItem的offset
    private int computeYOffset() {
        return (maxOffsetItem - selectedItem) * adapter.getItemHeight();
    }

    private void clampYOffset(int overY) {
        boolean clamped = false;
        int itemHeight = adapter.getItemHeight();
        int itemCount = adapter.getItemCount();
        if (yOffset > maxOffsetItem * itemHeight + overY) {
            yOffset = maxOffsetItem * itemHeight + overY;
            clamped = true;
        } else if (yOffset < (maxOffsetItem - itemCount + 1) * itemHeight - overY) {
            yOffset = (maxOffsetItem - itemCount + 1) * itemHeight - overY;
            clamped = true;
        }
        if (clamped) scroller.forceFinished(true);
    }

    private int computeFinalYOffset() {
        float centerPosition = centerPosition();
        int centerItem = (int) Math.floor(centerPosition);

        if (centerItem < 0) centerItem = 0;
        else if (centerItem >= adapter.getItemCount()) centerItem = adapter.getItemCount() - 1;

        return (maxOffsetItem - centerItem) * adapter.getItemHeight();
    }

    // 中心线切割的单元位置
    private float centerPosition() {
        return (float) (maxOffsetItem + 0.5 - yOffset / adapter.getItemHeight());
    }
}
