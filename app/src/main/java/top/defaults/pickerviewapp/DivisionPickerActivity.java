package top.defaults.pickerviewapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import top.defaults.view.Division;
import top.defaults.view.DivisionPickerView;

public class DivisionPickerActivity extends AppCompatActivity {

    @BindView(R.id.divisionPicker) DivisionPickerView divisionPicker;
    @BindView(R.id.textView) TextView textView;
    @OnCheckedChanged(R.id.toggleType)
    void toggleType(boolean isChecked) {
        divisionPicker.setType(isChecked ? DivisionPickerView.TYPE_ALL : DivisionPickerView.TYPE_PROVINCE_AND_CITY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_division_picker);
        ButterKnife.bind(this);

        final List<DivisionModel> divisions = Divisions.get(this);
        divisionPicker.setDivisions(divisions);
        divisionPicker.setOnSelectedDateChangedListener(division -> textView.setText(Division.Helper.getCanonicalName(division)));
    }
}