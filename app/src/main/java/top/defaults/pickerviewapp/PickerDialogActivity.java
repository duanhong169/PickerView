package top.defaults.pickerviewapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import top.defaults.pickerviewapp.dialog.ActionListener;
import top.defaults.pickerviewapp.dialog.DivisionPickerDialog;
import top.defaults.pickerviewapp.dialog.SimplePickerDialog;
import top.defaults.pickerviewapp.dialog.TypeDialogFragment;
import top.defaults.view.Division;

public class PickerDialogActivity extends AppCompatActivity {

    @BindView(R.id.textView) TextView textView;
    @BindView(R.id.sampleChooser) RadioGroup sampleChooser;

    ActionListener actionListener = new ActionListener() {
        @Override
        public void onCancelClick(TypeDialogFragment dialog) {}

        @Override
        public void onDoneClick(TypeDialogFragment dialog) {
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
            }
        }
    };

    @OnClick(R.id.withView)
    void withView() {
        choosePicker(TypeDialogFragment.TYPE_VIEW).show(getFragmentManager(), "view");
    }

    @OnClick(R.id.withDialog)
    void withDialog() {
        choosePicker(TypeDialogFragment.TYPE_DIALOG).show(getFragmentManager(), "dialog");
    }

    TypeDialogFragment choosePicker(int type) {
        TypeDialogFragment picker;

        switch (sampleChooser.getCheckedRadioButtonId()) {
            case R.id.division:
                picker = DivisionPickerDialog.newInstance(type, actionListener);
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
}