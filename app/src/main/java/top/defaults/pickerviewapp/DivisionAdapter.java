package top.defaults.pickerviewapp;

import java.util.List;

import top.defaults.view.PickerView;

public class DivisionAdapter extends PickerView.Adapter {

    private List<Divisions.Division> divisions;

    public void setDivisions(List<Divisions.Division> divisions) {
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
