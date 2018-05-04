package top.defaults.pickerviewapp;

import android.content.Context;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Divisions {

    private static List<DivisionModel> divisions;

    private static DivisionModel parse(JSONObject o) {
        DivisionModel division = new DivisionModel();
        try {
            division.id = o.getInt("id");
            division.name = o.getString("name");
            division.lvl = o.getInt("lvl");
            division.parentId = o.getInt("parent");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return division;
    }

    public static List<DivisionModel> get(Context context) {
        if (divisions != null) {
            return divisions;
        }

        divisions = new ArrayList<>(4000);
        SparseArray<DivisionModel> divisionMap = new SparseArray<>(4000);

        try {
            JSONArray array = new JSONArray(readJson(context));
            for (int i = 0; i < array.length(); i++) {
                DivisionModel division = Divisions.parse(array.getJSONObject(i));
                if (division.lvl == 1) divisions.add(division);
                divisionMap.put(division.id, division);
            }

            for (int i = 0; i < divisionMap.size(); i++) {
                DivisionModel division = divisionMap.valueAt(i);
                if (division.parentId != 0) {
                    division.parent = divisionMap.get(division.parentId);
                    if (division.parent.children == null) {
                        division.parent.children = new ArrayList<>(40);
                    }
                    division.parent.children.add(division);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return divisions;
    }

    private static String readJson(Context context) {
        InputStream inputStream;
        try {
            inputStream = context.getAssets().open("area.json");
            int size = inputStream.available();
            byte[] bytes = new byte[size];
            size = inputStream.read(bytes);
            inputStream.close();
            return new String(bytes, 0, size);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "[]";
    }
}
