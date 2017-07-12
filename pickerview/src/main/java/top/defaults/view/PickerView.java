package top.defaults.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.text.Layout;
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
    private int preferredMaxOffsetItem = DEFAULT_MAX_OFFSET_ITEM;
    private int maxOffsetItem = preferredMaxOffsetItem;
    private int selectedItem = 0;

    private Adapter adapter;

    private Paint textPaint;
    private int textSize;
    private Rect textBounds = new Rect();

    private GestureDetector gestureDetector;
    private OverScroller scroller;
    private boolean pendingJustify;
    private float previousTouchedY;
    private double yOffset;
    private int minY;
    private int maxY;
    private int maxOverScrollY;

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

    public void setPreferredMaxOffsetItem(int preferredMaxOffsetItem) {
        this.preferredMaxOffsetItem = preferredMaxOffsetItem;
    }

    public void setAdapter(final Adapter adapter) {
        checkNotNull(adapter, "adapter == null");
        if (adapter.getItemCount() > Integer.MAX_VALUE / adapter.getItemHeight()) {
            Log.w(TAG, "getItemCount() is too large, unsupported yet");
        }

        this.adapter = adapter;
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
        int height = resolveSizeAndState((1 + 2 * preferredMaxOffsetItem) * itemHeight, heightMeasureSpec, 0);
        int realHeight = height & ~View.MEASURED_STATE_MASK;

        maxOffsetItem = (int) Math.ceil((realHeight / adapter.getItemHeight() - 1) / 2.f);
        computeYOffset();
        minY = -(adapter.getItemCount() - 1 - maxOffsetItem) * adapter.getItemHeight();
        maxY = maxOffsetItem * adapter.getItemHeight();
        maxOverScrollY = (maxOffsetItem > 3 ? 3 : maxOffsetItem) * adapter.getItemHeight();

        setMeasuredDimension(widthMeasureSpec, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        checkNotNull(adapter, "adapter == null");

        drawItems(canvas);

        topMask.setBounds(0, 0, getMeasuredWidth(), maxOffsetItem * adapter.getItemHeight());
        topMask.draw(canvas);

        bottomMask.setBounds(0, (maxOffsetItem + 1) * adapter.getItemHeight(), getMeasuredWidth(), getMeasuredHeight());
        bottomMask.draw(canvas);
    }

    private void drawItems(Canvas canvas) {
        updateSelectedItem();

        int itemHeight = adapter.getItemHeight();
        double drawYOffset = this.yOffset;
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
            float textBottom = (float) drawYOffset + (itemHeight + (textBounds.height())) / 2;

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

            Log.d(TAG, "computeScroll yOffset: " + yOffset);

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
        selectedItem = (int) Math.floor(centerPosition);
        Log.d(TAG, "selectedItem: " + selectedItem);
    }

    // 计算selectedItem的offset
    private void computeYOffset() {
        yOffset = (maxOffsetItem - selectedItem) * adapter.getItemHeight();
    }

    private void clampYOffset(int overY) {
        boolean clamped = false;
        int itemHeight = adapter.getItemHeight();
        int itemCount = adapter.getItemCount();

        double maxYOffset = maxOffsetItem * itemHeight + overY;
        double minYOffset = ((double) (maxOffsetItem - itemCount + 1)) * itemHeight - overY;

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

        if (centerItem < 0) centerItem = 0;
        else if (centerItem >= adapter.getItemCount()) centerItem = adapter.getItemCount() - 1;

        return (maxOffsetItem - centerItem) * adapter.getItemHeight();
    }

    // 中心线切割的单元位置
    private float centerPosition() {
        return (float) (maxOffsetItem + 0.5 - yOffset / adapter.getItemHeight());
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
