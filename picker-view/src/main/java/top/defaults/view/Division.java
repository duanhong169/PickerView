package top.defaults.view;

import java.util.List;

public class Division implements PickerView.PickerItem {
    public int id;
    public String name;
    public int lvl;
    public int parentId;

    public Division parent;
    public List<Division> children;

    public String getName() {
        return name;
    }

    public List<Division> getChildren() {
        return children;
    }

    public Division getParent() {
        return parent;
    }

    @Override
    public String getText() {
        return name;
    }

    public String getCanonicalName() {
        Division division = this;
        StringBuilder text = new StringBuilder(division.getText());
        while (division.getParent() != null) {
            division = division.getParent();
            text.insert(0, division.getText());
        }
        return text.toString();
    }
}
