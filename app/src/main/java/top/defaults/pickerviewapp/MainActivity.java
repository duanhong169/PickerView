package top.defaults.pickerviewapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import top.defaults.view.PickerView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PickerView pickerView1 = (PickerView) findViewById(R.id.pickerView1);
        PickerView pickerView2 = (PickerView) findViewById(R.id.pickerView2);
        PickerView pickerView3 = (PickerView) findViewById(R.id.pickerView3);
        pickerView1.setPreferredMaxOffsetItemCount(4);
        pickerView2.setPreferredMaxOffsetItemCount(4);
        pickerView3.setPreferredMaxOffsetItemCount(4);
        PickerView.Adapter adapter = new PickerView.Adapter() {

            @Override
            protected int getItemCount() {
                return 42;
            }

            @Override
            protected int getItemHeight() {
                return 64;
            }

            @Override
            protected String getText(int index) {
                return "Item " + index;
            }
        };
        pickerView1.setAdapter(adapter);
        pickerView2.setAdapter(adapter);
        pickerView3.setAdapter(adapter);

        pickerView1.setOnSelectedItemChangedListener(new PickerView.OnSelectedItemChangedListener() {
            @Override
            public void onSelectedItemChanged(int selectedItemPosition) {
                Log.d(TAG, "selectedItemPosition: " + selectedItemPosition);
            }
        });
    }
}
