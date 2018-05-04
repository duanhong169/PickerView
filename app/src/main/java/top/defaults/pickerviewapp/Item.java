package top.defaults.pickerviewapp;

import java.util.ArrayList;
import java.util.List;

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

    public static List<Item> sampleItems() {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 42; i++) {
            items.add(new Item("Item " + i));
        }
        return items;
    }
}
