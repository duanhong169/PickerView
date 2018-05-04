package top.defaults.pickerviewapp;

import top.defaults.view.PickerView;

public class Item implements PickerView.PickerItem {

    private String text;

    public Item(String s) {
        text = s;
    }

    @Override
    public String getText() {
        return text;
    }
}
