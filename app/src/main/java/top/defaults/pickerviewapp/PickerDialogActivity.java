package top.defaults.pickerviewapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import top.defaults.view.PickerView;
import top.defaults.view.PickerViewDialog;

public class PickerDialogActivity extends AppCompatActivity {

    @BindView(R.id.textView) TextView textView;

    PickerViewDialog dialog;

    @OnClick(R.id.button)
    void pick() {
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker_dialog);
        ButterKnife.bind(this);

        dialog = new PickerViewDialog(this);
        dialog.setContentView(R.layout.dialog_default_picker);

        PickerView pickerView = dialog.findViewById(R.id.pickerView);
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
        pickerView.setSelectedItemPosition(4);

        View cancel = dialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> dialog.dismiss());

        View done = dialog.findViewById(R.id.done);
        done.setOnClickListener(v -> {
            textView.setText(adapter.getText(pickerView.getSelectedItemPosition()));
            dialog.dismiss();
        });
    }

    private static class Item implements PickerView.PickerItem {

        private String text;

        Item(String s) {
            text = s;
        }

        @Override
        public String getText() {
            return text;
        }
    }
}