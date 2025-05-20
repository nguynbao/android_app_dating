package com.example.myapplication.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.myapplication.R;

import java.util.List;

public class CityDropdownAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> cityList;

    public CityDropdownAdapter(Context context, List<String> cityList) {
        super(context, 0, cityList);
        this.context = context;
        this.cityList = cityList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return createCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        // Xóa background trắng mặc định của dropdown item
        view.setBackgroundResource(R.drawable.et_border_gradient);
        return view;
    }

    private View createCustomView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_dropdown_city, parent, false);
        }

        TextView cityText = convertView.findViewById(R.id.text_view_city);
        cityText.setText(cityList.get(position));
        return convertView;
    }
}
