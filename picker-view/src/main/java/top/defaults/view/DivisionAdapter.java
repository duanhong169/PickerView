package top.defaults.view;

import java.util.List;

public class DivisionAdapter extends PickerView.Adapter<Division> {

    private List<Division> divisions;

    public void setDivisions(List<Division> divisions) {
        this.divisions = divisions;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return divisions == null ? 0 : divisions.size();
    }

    @Override
    public Division getItem(int index) {
        return divisions.get(index);
    }
}
