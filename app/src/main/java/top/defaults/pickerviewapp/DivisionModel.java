package top.defaults.pickerviewapp;

import java.util.List;

import top.defaults.view.Division;

public class DivisionModel implements Division {
    public int id;
    public String name;
    public int lvl;
    public int parentId;

    public DivisionModel parent;
    public List<Division> children;

    public String getName() {
        return name;
    }

    public List<Division> getChildren() {
        return children;
    }

    public DivisionModel getParent() {
        return parent;
    }

    @Override
    public String getText() {
        return name;
    }
}
