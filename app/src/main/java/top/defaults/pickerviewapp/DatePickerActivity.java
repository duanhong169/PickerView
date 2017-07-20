package top.defaults.pickerviewapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import top.defaults.view.DatePickerView;

public class DatePickerActivity extends AppCompatActivity {

    private static final String TAG = "DatePickerActivity";

    @BindView(R.id.datePickerView) DatePickerView datePickerView;
    @BindView(R.id.textView) TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_picker);
        ButterKnife.bind(this);
        datePickerView.setOnSelectedDateChangedListener(new DatePickerView.OnSelectedDateChangedListener() {
            @Override
            public void onSelectedDateChanged(Calendar date) {
                int year = date.get(Calendar.YEAR);
                int month = date.get(Calendar.MONTH);
                int dayOfMonth = date.get(Calendar.DAY_OF_MONTH);
                String dateString = String.format(Locale.getDefault(), "%d年%02d月%02d日", year, month + 1, dayOfMonth);
                textView.setText(dateString);
                Log.d(TAG, "new date: " + dateString);
            }
        });
        datePickerView.setStartDate(Calendar.getInstance());

    }
}