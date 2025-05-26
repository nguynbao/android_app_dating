package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.PersonModel;

import java.util.List;

public class PersonAdapter extends RecyclerView.Adapter<PersonAdapter.PersonViewHolder> {

    private final List<PersonModel> personList;

    public PersonAdapter(List<PersonModel> personList) {
        this.personList = personList;
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_person, parent, false);
        return new PersonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder holder, int position) {
        PersonModel person = personList.get(position);
        holder.nameTextView.setText(person.getName());
        holder.cityTextView.setText("Thành phố: " + person.getCity());
        holder.locationTextView.setText("Vị trí: " + person.getLat() + ", " + person.getLon());
    }

    @Override
    public int getItemCount() {

        return personList == null ? 0 : personList.size();
    }

    public static class PersonViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, cityTextView, locationTextView;

        public PersonViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tvName);
            cityTextView = itemView.findViewById(R.id.tvCity);
            locationTextView = itemView.findViewById(R.id.tvLocation);
        }
    }
}
