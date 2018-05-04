package top.defaults.pickerviewapp.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import top.defaults.pickerviewapp.DivisionModel;
import top.defaults.view.Division;
import top.defaults.pickerviewapp.Divisions;
import top.defaults.pickerviewapp.R;
import top.defaults.view.DivisionPickerView;
import top.defaults.view.PickerViewDialog;

public class DivisionPickerDialog extends TypeDialogFragment {

    private DivisionPickerView divisionPicker;

    public static DivisionPickerDialog newInstance(int type, ActionListener actionListener) {
        return TypeDialogFragment.newInstance(DivisionPickerDialog.class, type, actionListener);
    }

    @Override
    protected Dialog createDialog(Bundle savedInstanceState) {
        PickerViewDialog dialog = new PickerViewDialog(getActivity());
        dialog.setContentView(R.layout.dialog_division_picker);
        divisionPicker = dialog.findViewById(R.id.divisionPicker);

        setupPickers();

        View cancel = dialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onCancelClick(this);
            }
            dismiss();
        });

        View done = dialog.findViewById(R.id.done);
        done.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDoneClick(this);
            }
            dismiss();
        });
        return dialog;
    }

    @Override
    protected View createView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_division_picker, container, false);
        divisionPicker = view.findViewById(R.id.divisionPicker);

        setupPickers();

        View cancel = view.findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onCancelClick(this);
            }
            dismiss();
        });

        View done = view.findViewById(R.id.done);
        done.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDoneClick(this);
            }
            dismiss();
        });
        return view;
    }

    public Division getSelectedDivision() {
        return divisionPicker.getSelectedDivision();
    }

    private void setupPickers() {
        final List<DivisionModel> divisions = Divisions.get(getActivity());
        divisionPicker.setDivisions(divisions);
    }
}
