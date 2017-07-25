package top.defaults.pickerviewapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import top.defaults.view.DateTimePickerView;

public class DatePickerActivity extends AppCompatActivity {

    private static final String TAG = "DatePickerActivity";

    @BindView(R.id.datePickerView)
    DateTimePickerView dateTimePickerView;
    @BindView(R.id.textView) TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_picker);
        ButterKnife.bind(this);
        dateTimePickerView.setOnSelectedDateChangedListener(new DateTimePickerView.OnSelectedDateChangedListener() {
            @Override
            public void onSelectedDateChanged(Calendar date) {
                int year = date.get(Calendar.YEAR);
                int month = date.get(Calendar.MONTH);
                int dayOfMonth = date.get(Calendar.DAY_OF_MONTH);
                int hour = date.get(Calendar.HOUR_OF_DAY);
                int minute = date.get(Calendar.MINUTE);
                String dateString = String.format(Locale.getDefault(), "%d年%02d月%02d日%02d时%02d分", year, month + 1, dayOfMonth, hour, minute);
                textView.setText(dateString);
                Log.d(TAG, "new date: " + dateString);
            }
        });
        dateTimePickerView.setStartDate(Calendar.getInstance());
        dateTimePickerView.setSelectedDate(new GregorianCalendar(2017, 6, 27, 21, 30));
    }
}