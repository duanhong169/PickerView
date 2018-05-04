package top.defaults.view;

import java.util.List;

public class DivisionAdapter extends PickerView.Adapter<Division> {

    private List<? extends Division> divisions;

    public void setDivisions(List<? extends Division> divisions) {
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
