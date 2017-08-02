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
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import top.defaults.view.DateTimePickerView;

public class DatePickerActivity extends AppCompatActivity {

    private static final String TAG = "DatePickerActivity";
    private int type = DateTimePickerView.TYPE_DATE_TIME;

    @BindView(R.id.datePickerView) DateTimePickerView dateTimePickerView;
    @BindView(R.id.textView) TextView textView;
    @OnClick(R.id.button) void buttonClicked() {
        type++;
        type %= 4;
        dateTimePickerView.setType(type);
    }
    @OnCheckedChanged(R.id.curved) void toggle(boolean checked) {
        dateTimePickerView.setCurved(checked);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_picker);
        ButterKnife.bind(this);
        dateTimePickerView.setStartDate(Calendar.getInstance());
        dateTimePickerView.setSelectedDate(new GregorianCalendar(2017, 6, 27, 21, 30));
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
    }
}