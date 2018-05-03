package top.defaults.view;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

public class BottomFullWidthDialog extends Dialog {
    private Context context;
    private float heightPercentage;

    public BottomFullWidthDialog(Context context, float heightPercentage) {
        this(context, heightPercentage, R.style.BottomFullWidthDialog);
    }

    private BottomFullWidthDialog(Context context, float heightPercentage, int themeResId) {
        super(context, themeResId);
        this.context = context;
        this.heightPercentage = heightPercentage;
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            layoutParams.width = displayMetrics.widthPixels;
            int height = (int) (displayMetrics.heightPixels * heightPercentage);
            layoutParams.height = height > 0 ? height : WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = Gravity.BOTTOM;
            window.setAttributes(layoutParams);
        }
    }
}
