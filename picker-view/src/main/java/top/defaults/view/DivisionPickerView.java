package top.defaults.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import java.util.List;

public class DivisionPickerView extends PickerViewGroup {

    private final DivisionAdapter provisionAdapter = new DivisionAdapter();
    private final DivisionAdapter cityAdapter = new DivisionAdapter();
    private final DivisionAdapter divisionAdapter = new DivisionAdapter();

    private PickerView provincePicker;
    private PickerView cityPicker;
    private PickerView divisionPicker;

    public static final int TYPE_ALL = 0;
    public static final int TYPE_PROVINCE_AND_CITY = 1;
    private int type = TYPE_ALL;

    public DivisionPickerView(Context context) {
        this(context, null);
    }

    public DivisionPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DivisionPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DivisionPickerView);
        type = typedArray.getInt(R.styleable.DivisionPickerView_divisionPickerType, TYPE_ALL);
        typedArray.recycle();

        provincePicker = new PickerView(context);
        settlePickerView(provincePicker);

        cityPicker = new PickerView(context);
        settlePickerView(cityPicker);

        divisionPicker = new PickerView(context);
        settlePickerView(divisionPicker);

        configure();
    }

    public void setType(int type) {
        this.type = type;
        configure();
    }

    private void configure() {
        if (type == TYPE_PROVINCE_AND_CITY) {
            divisionPicker.setVisibility(GONE);
        } else {
            divisionPicker.setVisibility(VISIBLE);
        }
    }

    public interface OnSelectedDivisionChangedListener {
        void onSelectedDivisionChanged(Division division);
    }

    private OnSelectedDivisionChangedListener onSelectedDivisionChangedListener;

    public void setOnSelectedDateChangedListener(OnSelectedDivisionChangedListener onSelectedDivisionChangedListener) {
        this.onSelectedDivisionChangedListener = onSelectedDivisionChangedListener;
    }

    /**
     * 设置省市区数据，省市区数据通过父子关系构成树形结构，此处需传入第一级（省）列表
     *
     * @param divisions 省数据列表，市和区数据为省数据的子节点，需要外部构造好之后传入
     */
    public void setDivisions(List<? extends Division> divisions) {
        provisionAdapter.setDivisions(divisions);
        provincePicker.setAdapter(provisionAdapter);

        cityAdapter.setDivisions(provisionAdapter.getItem(provincePicker.getSelectedItemPosition()).getChildren());
        cityPicker.setAdapter(cityAdapter);

        divisionAdapter.setDivisions(cityAdapter.getItem(cityPicker.getSelectedItemPosition()).getChildren());
        divisionPicker.setAdapter(divisionAdapter);

        PickerView.OnSelectedItemChangedListener listener = new PickerView.OnSelectedItemChangedListener() {
            @Override
            public void onSelectedItemChanged(PickerView pickerView, int previousPosition, int selectedItemPosition) {
                if (pickerView == provincePicker) {
                    cityAdapter.setDivisions(provisionAdapter.getItem(provincePicker.getSelectedItemPosition()).getChildren());
                    divisionAdapter.setDivisions(cityAdapter.getItem(cityPicker.getSelectedItemPosition()).getChildren());
                } else if (pickerView == cityPicker) {
                    divisionAdapter.setDivisions(cityAdapter.getItem(cityPicker.getSelectedItemPosition()).getChildren());
                }

                if (onSelectedDivisionChangedListener != null) {
                    onSelectedDivisionChangedListener.onSelectedDivisionChanged(getSelectedDivision());
                }
            }
        };

        provincePicker.setOnSelectedItemChangedListener(listener);
        cityPicker.setOnSelectedItemChangedListener(listener);
        divisionPicker.setOnSelectedItemChangedListener(listener);
    }

    public PickerView getProvincePicker() {
        return provincePicker;
    }

    public PickerView getCityPicker() {
        return cityPicker;
    }

    public PickerView getDivisionPicker() {
        return divisionPicker;
    }

    public Division getSelectedDivision() {
        Division division = null;
        if (type == TYPE_ALL) {
            division = divisionPicker.getSelectedItem(Division.class);
        }
        if (division == null) {
            division = cityPicker.getSelectedItem(Division.class);
        }
        if (division == null) {
            division = provincePicker.getSelectedItem(Division.class);
        }
        return division;
    }
}
