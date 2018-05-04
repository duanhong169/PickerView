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

public class SimplePickerDialog extends BaseDialogFragment {

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
        return BaseDialogFragment.newInstance(SimplePickerDialog.class, type, actionListener);
    }

    @Override
    public Dialog createDialog(Bundle savedInstanceState) {
        PickerViewDialog dialog = new PickerViewDialog(getActivity());
        dialog.setContentView(R.layout.dialog_simple_picker);

        pickerView = dialog.findViewById(R.id.pickerView);
        pickerView.setAdapter(adapter);

        attachActions(dialog.findViewById(R.id.done), dialog.findViewById(R.id.cancel));
        return dialog;
    }

    @Nullable
    @Override
    public View createView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_simple_picker, container, false);

        pickerView = view.findViewById(R.id.pickerView);
        pickerView.setAdapter(adapter);

        attachActions(view.findViewById(R.id.done), view.findViewById(R.id.cancel));
        return view;
    }

    public Item getSelectedItem() {
        return adapter.getItem(pickerView.getSelectedItemPosition());
    }
}
