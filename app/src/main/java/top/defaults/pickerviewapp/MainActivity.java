package top.defaults.pickerviewapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import top.defaults.view.PickerView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @BindView(R.id.pickerView1) PickerView pickerView1;
    @BindView(R.id.pickerView2) PickerView pickerView2;
    @BindView(R.id.pickerView3) PickerView pickerView3;

    @BindView(R.id.textView1) TextView textView1;
    @BindView(R.id.textView2) TextView textView2;
    @BindView(R.id.textView3) TextView textView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        pickerView1.setPreferredMaxOffsetItemCount(4);
        pickerView2.setPreferredMaxOffsetItemCount(4);
        pickerView3.setPreferredMaxOffsetItemCount(4);
        PickerView.Adapter adapter = new PickerView.Adapter() {

            @Override
            public int getItemCount() {
                return 42;
            }

            @Override
            public int getItemHeight() {
                return 64;
            }

            @Override
            public String getText(int index) {
                return "Item " + index;
            }
        };
        pickerView1.setAdapter(adapter);
        pickerView2.setAdapter(adapter);
        pickerView3.setAdapter(adapter);

        PickerView.OnSelectedItemChangedListener listener = new PickerView.OnSelectedItemChangedListener() {
            @Override
            public void onSelectedItemChanged(PickerView pickerView, int selectedItemPosition) {
                Log.d(TAG, "selectedItemPosition: " + selectedItemPosition);

                switch (pickerView.getId()) {
                    case R.id.pickerView1:
                        textView1.setText(pickerView.getAdapter().getText(selectedItemPosition));
                        break;
                    case R.id.pickerView2:
                        textView2.setText(pickerView.getAdapter().getText(selectedItemPosition));
                        break;
                    case R.id.pickerView3:
                        textView3.setText(pickerView.getAdapter().getText(selectedItemPosition));
                        break;
                }
            }
        };

        pickerView1.setOnSelectedItemChangedListener(listener);
        pickerView2.setOnSelectedItemChangedListener(listener);
        pickerView3.setOnSelectedItemChangedListener(listener);

        pickerView1.setSelectedItemPosition(4);
        pickerView2.setSelectedItemPosition(3);
        pickerView3.setSelectedItemPosition(2);
    }
}
