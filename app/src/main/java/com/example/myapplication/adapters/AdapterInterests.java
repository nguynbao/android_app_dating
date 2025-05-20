package com.example.myapplication.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdapterInterests extends RecyclerView.Adapter<AdapterInterests.ViewHolder> {
    private List<String> items;
    private Context context;

    public AdapterInterests(Context context,List<String> items) {
        this.items = items;
        this.context = context;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public View overlay;
        public ImageView imageView;
        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.itemText);
            imageView = view.findViewById(R.id.img);
            overlay = view.findViewById(R.id.overlay);
        }
}
    @NonNull
    @Override
    public AdapterInterests.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_interests, parent, false);
        return new ViewHolder(view);
    }
    private Set<Integer> selectedPositions = new HashSet<>();

    @Override
    public void onBindViewHolder(AdapterInterests.ViewHolder holder, int position) {
        String interest = items.get(position);
        holder.textView.setText(interest);
        holder.imageView.setImageResource(getIconForInterest(interest));

        boolean isSelected = selectedPositions.contains(position);
        holder.overlay.setVisibility(isSelected ? View.GONE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            if (selectedPositions.contains(pos)) {
                selectedPositions.remove(pos);
            } else {
                selectedPositions.add(pos);
            }

            // Gọi lại onBindViewHolder để cập nhật alpha
            notifyItemChanged(pos);
        });
    }


    @Override
    public int getItemCount() {
        return items.size();
    }
    private int getIconForInterest(String interest) {
        switch (interest.toLowerCase()) {
            case "music":
                return R.drawable.music;
            case "cooking":
                return R.drawable.cooking;
            case "travelling":
                return R.drawable.travelling;
            case "photography":
                return R.drawable.photograpy;
            case "video games":
                return R.drawable.videogame;
            case "fitness":
                return R.drawable.fitness;
            case "shopping":
                return R.drawable.shoping;
            case "art & crafts":
                return R.drawable.art;
            case "ex sports":
                return R.drawable.exsport;
            case "drinking":
                return R.drawable.drinking;
            case "speeches":
                return R.drawable.speeches;
            case "swimming":
                return R.drawable.swimming;
            default:
                return R.drawable.music;

        }


}
    public List<String> getSelectedItems() {
        List<String> selected = new java.util.ArrayList<>();
        for (Integer pos : selectedPositions) {
            if (pos >= 0 && pos < items.size()) {
                selected.add(items.get(pos));
            }
        }
        return selected;
    }

}
