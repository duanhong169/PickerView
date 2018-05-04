package top.defaults.pickerviewapp.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import top.defaults.pickerviewapp.Divisions;
import top.defaults.pickerviewapp.R;
import top.defaults.view.PickerView;
import top.defaults.view.PickerViewDialog;

public class DivisionPickerDialog extends TypeDialogFragment {

    private final DivisionAdapter provisionAdapter = new DivisionAdapter();
    private final DivisionAdapter cityAdapter = new DivisionAdapter();
    private final DivisionAdapter divisionAdapter = new DivisionAdapter();

    private PickerView provincePicker;
    private PickerView cityPicker;
    private PickerView divisionPicker;

    public static DivisionPickerDialog newInstance(int type, ActionListener actionListener) {
        return TypeDialogFragment.newInstance(DivisionPickerDialog.class, type, actionListener);
    }

    @Override
    protected Dialog createDialog(Bundle savedInstanceState) {
        PickerViewDialog dialog = new PickerViewDialog(getActivity());
        dialog.setContentView(R.layout.dialog_division_picker);

        provincePicker = dialog.findViewById(R.id.provincePicker);
        cityPicker = dialog.findViewById(R.id.cityPicker);
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
        provincePicker = view.findViewById(R.id.provincePicker);
        cityPicker = view.findViewById(R.id.cityPicker);
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

    public Divisions.Division getSelectedDivision() {
        return divisionAdapter.getItem(divisionPicker.getSelectedItemPosition());
    }

    private void setupPickers() {
        final List<Divisions.Division> divisions = Divisions.get(getActivity());

        provisionAdapter.setDivisions(divisions);
        provincePicker.setAdapter(provisionAdapter);

        cityAdapter.setDivisions(provisionAdapter.getItem(provincePicker.getSelectedItemPosition()).getChildren());
        cityPicker.setAdapter(cityAdapter);

        divisionAdapter.setDivisions(cityAdapter.getItem(cityPicker.getSelectedItemPosition()).getChildren());
        divisionPicker.setAdapter(divisionAdapter);

        PickerView.OnSelectedItemChangedListener listener = (pickerView, previousPosition, selectedItemPosition) -> {
            switch (pickerView.getId()) {
                case R.id.provincePicker:
                    cityAdapter.setDivisions(provisionAdapter.getItem(provincePicker.getSelectedItemPosition()).getChildren());
                    divisionAdapter.setDivisions(cityAdapter.getItem(cityPicker.getSelectedItemPosition()).getChildren());
                    break;
                case R.id.cityPicker:
                    divisionAdapter.setDivisions(cityAdapter.getItem(cityPicker.getSelectedItemPosition()).getChildren());
                    break;
            }
        };

        provincePicker.setOnSelectedItemChangedListener(listener);
        cityPicker.setOnSelectedItemChangedListener(listener);
        divisionPicker.setOnSelectedItemChangedListener(listener);
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
        public Divisions.Division getItem(int index) {
            return divisions.get(index);
        }
    }
}
