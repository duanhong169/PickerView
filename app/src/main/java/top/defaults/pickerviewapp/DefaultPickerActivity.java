package top.defaults.pickerviewapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import top.defaults.logger.Logger;
import top.defaults.view.PickerView;

public class DefaultPickerActivity extends AppCompatActivity {

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
            public Item getItem(int index) {
                return new Item("Item " + index);
            }
        };

        pickerView.setAdapter(adapter);
        pickerView.setOnSelectedItemChangedListener((pickerView, previousPosition, selectedItemPosition) -> {
            Logger.d("selectedItemPosition: %d", selectedItemPosition);
            textView.setText(pickerView.getAdapter().getText(selectedItemPosition));
        });
        pickerView.setSelectedItemPosition(4);
    }
}