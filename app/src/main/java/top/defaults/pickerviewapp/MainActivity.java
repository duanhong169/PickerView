package top.defaults.pickerviewapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import top.defaults.view.PickerView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PickerView pickerView = (PickerView) findViewById(R.id.pickerView);
        pickerView.setPreferredMaxOffsetItem(4);
        pickerView.setAdapter(new PickerView.Adapter() {

            @Override
            protected int getItemCount() {
                return Integer.MAX_VALUE / getItemHeight();
            }

            @Override
            protected int getItemHeight() {
                return 64;
            }

            @Override
            protected String getText(int index) {
                return "Item " + index;
            }
        });
    }
}
