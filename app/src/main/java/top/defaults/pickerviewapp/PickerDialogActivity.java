package top.defaults.pickerviewapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import top.defaults.pickerviewapp.dialog.ActionListener;
import top.defaults.pickerviewapp.dialog.DatePickerDialog;
import top.defaults.pickerviewapp.dialog.DivisionPickerDialog;
import top.defaults.pickerviewapp.dialog.SimplePickerDialog;
import top.defaults.pickerviewapp.dialog.BaseDialogFragment;
import top.defaults.view.Division;

public class PickerDialogActivity extends AppCompatActivity {

    @BindView(R.id.textView) TextView textView;
    @BindView(R.id.sampleChooser) RadioGroup sampleChooser;

    ActionListener actionListener = new ActionListener() {
        @Override
        public void onCancelClick(BaseDialogFragment dialog) {}

        @Override
        public void onDoneClick(BaseDialogFragment dialog) {
            if (dialog instanceof SimplePickerDialog) {
                textView.setText(((SimplePickerDialog) dialog).getSelectedItem().getText());
            } else if (dialog instanceof DivisionPickerDialog) {
                Division division = ((DivisionPickerDialog) dialog).getSelectedDivision();
                StringBuilder text = new StringBuilder(division.getText());
                while (division.getParent() != null) {
                    division = division.getParent();
                    text.insert(0, division.getText());
                }
                textView.setText(text.toString());
            } else if (dialog instanceof DatePickerDialog) {
                textView.setText(getDateString(((DatePickerDialog) dialog).getSelectedDate()));
            }
        }
    };

    @OnClick(R.id.withView)
    void withView() {
        choosePicker(BaseDialogFragment.TYPE_VIEW).show(getFragmentManager(), "view");
    }

    @OnClick(R.id.withDialog)
    void withDialog() {
        choosePicker(BaseDialogFragment.TYPE_DIALOG).show(getFragmentManager(), "dialog");
    }

    BaseDialogFragment choosePicker(int type) {
        BaseDialogFragment picker;

        switch (sampleChooser.getCheckedRadioButtonId()) {
            case R.id.division:
                picker = DivisionPickerDialog.newInstance(type, actionListener);
                break;
            case R.id.date:
                picker = DatePickerDialog.newInstance(type, actionListener);
                break;
            case R.id.simple:
            default:
                picker = SimplePickerDialog.newInstance(type, actionListener);
                break;
        }

        return picker;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker_dialog);
        ButterKnife.bind(this);
    }

    private String getDateString(Calendar date) {
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int dayOfMonth = date.get(Calendar.DAY_OF_MONTH);
        int hour = date.get(Calendar.HOUR_OF_DAY);
        int minute = date.get(Calendar.MINUTE);
        return String.format(Locale.getDefault(), "%d年%02d月%02d日%02d时%02d分", year, month + 1, dayOfMonth, hour, minute);
    }
}