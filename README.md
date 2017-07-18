# PickerView
Android滚动选择器

<img src='art/default.gif' height='500px'/>
<img src='art/division.gif' height='500px'/>

## 使用方法

### 1. 添加依赖

gradle：
	
```
compile 'com.github.duanhong169:picker-view:0.1.2'
```

maven：
	
```
<dependency>
	<groupId>com.github.duanhong169</groupId>
	<artifactId>picker-view</artifactId>
	<version>0.1.2</version>
	<type>pom</type>
</dependency>
```

### 2. 集成到项目中
	
添加到layout文件中：

```	
<top.defaults.view.PickerView
	android:id="@+id/pickerView"
	android:layout_width="match_parent"
	android:layout_height="wrap_content"/>
```
	
配置数据源：

```java
PickerView.Adapter adapter = new PickerView.Adapter() {

    @Override
    public int getItemCount() {
        return 42;
    }

    @Override
    public String getText(int index) {
        return "Item " + index;
    }
};
pickerView.setAdapter(adapter);
```

监听选择事件：

```java
pickerView.setOnSelectedItemChangedListener(new PickerView.OnSelectedItemChangedListener() {
    @Override
    public void onSelectedItemChanged(PickerView pickerView, int selectedItemPosition) {
        Log.d(TAG, "selectedItemPosition: " + selectedItemPosition);
        textView.setText(pickerView.getAdapter().getText(selectedItemPosition));
    }
});
```

更详细的使用方法请参见示例。

### License
请查看[LICENSE](./LICENSE)文件。