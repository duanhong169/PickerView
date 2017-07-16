package top.defaults.pickerviewapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import top.defaults.view.PickerView;

public class DivisionPickerActivity extends AppCompatActivity {

    private static final String TAG = "DivisionPickerActivity";

    @BindView(R.id.provincePicker) PickerView provincePicker;
    @BindView(R.id.cityPicker) PickerView cityPicker;
    @BindView(R.id.divisionPicker) PickerView divisionPicker;

    private final DivisionAdapter provisionAdapter = new DivisionAdapter();
    private final DivisionAdapter cityAdapter = new DivisionAdapter();
    private final DivisionAdapter divisionAdapter = new DivisionAdapter();

    @BindView(R.id.textView) TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_division_picker);
        ButterKnife.bind(this);

        final List<Divisions.Division> divisions = Divisions.get(this);

        provisionAdapter.setDivisions(divisions);
        provincePicker.setAdapter(provisionAdapter);

        cityAdapter.setDivisions(provisionAdapter.getItem(provincePicker.getSelectedItemPosition()).getChildren());
        cityPicker.setAdapter(cityAdapter);

        divisionAdapter.setDivisions(cityAdapter.getItem(cityPicker.getSelectedItemPosition()).getChildren());
        divisionPicker.setAdapter(divisionAdapter);

        textView.setText(getSelectedDivisionName());

        PickerView.OnSelectedItemChangedListener listener = new PickerView.OnSelectedItemChangedListener() {
            @Override
            public void onSelectedItemChanged(PickerView pickerView, int selectedItemPosition) {
                switch (pickerView.getId()) {
                    case R.id.provincePicker:
                        cityAdapter.setDivisions(provisionAdapter.getItem(provincePicker.getSelectedItemPosition()).getChildren());
                        divisionAdapter.setDivisions(cityAdapter.getItem(cityPicker.getSelectedItemPosition()).getChildren());
                        break;
                    case R.id.cityPicker:
                        divisionAdapter.setDivisions(cityAdapter.getItem(cityPicker.getSelectedItemPosition()).getChildren());
                        break;
                }

                textView.setText(getSelectedDivisionName());
            }
        };

        provincePicker.setOnSelectedItemChangedListener(listener);
        cityPicker.setOnSelectedItemChangedListener(listener);
        divisionPicker.setOnSelectedItemChangedListener(listener);
    }

    private String getSelectedDivisionName() {
        String province = provisionAdapter.getText(provincePicker.getSelectedItemPosition());
        String city = cityAdapter.getText(cityPicker.getSelectedItemPosition());
        String division = divisionAdapter.getText(divisionPicker.getSelectedItemPosition());

        return String.format(Locale.US, "%s%s%s", province, city, division);
    }

    private static class DivisionAdapter extends PickerView.Adapter {

        private List<Divisions.Division> divisions;

        private void setDivisions(List<Divisions.Division> divisions) {
            this.divisions = divisions;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return divisions == null ? 0 : divisions.size();
        }

        @Override
        public String getText(int index) {
            if (index >= getItemCount()) return "";
            return getItem(index).getName();
        }

        private Divisions.Division getItem(int index) {
            return divisions.get(index);
        }
    }
}