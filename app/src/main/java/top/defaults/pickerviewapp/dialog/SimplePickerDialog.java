package top.defaults.pickerviewapp.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import top.defaults.pickerviewapp.Item;
import top.defaults.pickerviewapp.R;
import top.defaults.view.PickerView;
import top.defaults.view.PickerViewDialog;

public class SimplePickerDialog extends TypeDialogFragment {

    PickerView.Adapter<Item> adapter = new PickerView.Adapter<Item>() {

        @Override
        public int getItemCount() {
            return 42;
        }

        @Override
        public Item getItem(int index) {
            return new Item("Item " + index);
        }
    };

    private PickerView pickerView;

    public static SimplePickerDialog newInstance(int type, ActionListener actionListener) {
        return TypeDialogFragment.newInstance(SimplePickerDialog.class, type, actionListener);
    }

    @Override
    public Dialog createDialog(Bundle savedInstanceState) {
        PickerViewDialog dialog = new PickerViewDialog(getActivity());
        dialog.setContentView(R.layout.dialog_simple_picker);

        pickerView = dialog.findViewById(R.id.pickerView);
        pickerView.setAdapter(adapter);

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

    @Nullable
    @Override
    public View createView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_simple_picker, container, false);

        pickerView = view.findViewById(R.id.pickerView);
        pickerView.setAdapter(adapter);

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

    public Item getSelectedItem() {
        return adapter.getItem(pickerView.getSelectedItemPosition());
    }
}
