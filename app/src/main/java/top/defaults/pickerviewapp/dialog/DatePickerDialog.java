package top.defaults.pickerviewapp.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;

import top.defaults.pickerviewapp.R;
import top.defaults.view.DateTimePickerView;
import top.defaults.view.PickerViewDialog;

public class DatePickerDialog extends BaseDialogFragment {

    private DateTimePickerView datePicker;

    public static DatePickerDialog newInstance(int type, ActionListener actionListener) {
        return BaseDialogFragment.newInstance(DatePickerDialog.class, type, actionListener);
    }

    @Override
    protected Dialog createDialog(Bundle savedInstanceState) {
        PickerViewDialog dialog = new PickerViewDialog(getActivity());
        dialog.setContentView(R.layout.dialog_date_picker);
        datePicker = dialog.findViewById(R.id.datePicker);
        attachActions(dialog.findViewById(R.id.done), dialog.findViewById(R.id.cancel));
        return dialog;
    }

    @Override
    protected View createView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_date_picker, container, false);
        datePicker = view.findViewById(R.id.datePicker);
        attachActions(view.findViewById(R.id.done), view.findViewById(R.id.cancel));
        return view;
    }

    public Calendar getSelectedDate() {
        return datePicker.getSelectedDate();
    }
}
