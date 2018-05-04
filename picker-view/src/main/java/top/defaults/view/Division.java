package top.defaults.view;

import java.util.List;

public interface Division extends PickerView.PickerItem {

    String getName();

    List<Division> getChildren();

    Division getParent();

    class Helper {

        public static String getCanonicalName(Division division) {
            StringBuilder text = new StringBuilder(division.getText());
            while (division.getParent() != null) {
                division = division.getParent();
                text.insert(0, division.getText());
            }
            return text.toString();
        }
    }
}
