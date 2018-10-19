package top.defaults.pickerviewapp;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import top.defaults.view.PickerView;

public class DefaultPickerActivity extends AppCompatActivity {

    @BindView(R.id.pickerView) PickerView pickerView;
    @BindView(R.id.textView) TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_picker);
        ButterKnife.bind(this);

        pickerView.setItems(Item.sampleItems(), item -> textView.setText(item.getText()));
        pickerView.setSelectedItemPosition(4);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "SpaceMono-Regular.ttf");
        pickerView.setTypeface(typeface);
    }
}