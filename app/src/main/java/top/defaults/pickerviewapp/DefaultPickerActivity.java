package top.defaults.pickerviewapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import top.defaults.view.PickerView;

public class DefaultPickerActivity extends AppCompatActivity {

    private static final String TAG = "DefaultPickerActivity";

    @BindView(R.id.pickerView) PickerView pickerView;
    @BindView(R.id.textView) TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_picker);
        ButterKnife.bind(this);

        PickerView.Adapter adapter = new PickerView.Adapter() {

            @Override
            public int getItemCount() {
                return 42;
            }

            @Override
            public String getText(int index) {
                return "Item " + index;
            }
        };
        pickerView.setAdapter(adapter);
        pickerView.setOnSelectedItemChangedListener(new PickerView.OnSelectedItemChangedListener() {
            @Override
            public void onSelectedItemChanged(PickerView pickerView, int selectedItemPosition) {
                Log.d(TAG, "selectedItemPosition: " + selectedItemPosition);
                textView.setText(pickerView.getAdapter().getText(selectedItemPosition));
            }
        });
        pickerView.setSelectedItemPosition(4);
    }
}