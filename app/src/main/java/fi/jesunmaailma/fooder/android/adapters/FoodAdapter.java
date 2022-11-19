package fi.jesunmaailma.fooder.android.adapters;

import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fi.jesunmaailma.fooder.android.R;
import fi.jesunmaailma.fooder.android.models.Food;
import fi.jesunmaailma.fooder.android.models.Restaurant;
import fi.jesunmaailma.fooder.android.ui.activities.FoodDetails;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {
    List<Food> foodList;
    View view;

    Food food;

    public FoodAdapter(List<Food> foodList) {
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.food_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        food = foodList.get(position);

        if (food.getListPos() == 0) {
            holder.categoryName.setVisibility(View.VISIBLE);
            holder.categoryName.setText(food.getCategoryName());
            holder.categoryDescription.setVisibility(View.VISIBLE);

            if (food.getCategoryDescription().contains("null")) {
                holder.categoryDescription.setText("Tällä kategorialla ei ole kuvausta.");
                holder.categoryDescription.setTypeface(holder.categoryDescription.getTypeface(), Typeface.ITALIC);
            } else {
                holder.categoryDescription.setText(food.getCategoryDescription());
                holder.categoryDescription.setTypeface(holder.categoryDescription.getTypeface(), Typeface.NORMAL);
            }

        } else {
            holder.categoryName.setText(null);
            holder.categoryName.setVisibility(View.GONE);
            holder.categoryDescription.setText(null);
            holder.categoryDescription.setVisibility(View.GONE);
        }
        holder.foodName.setText(food.getName());
        holder.foodPrice.setText(
                String.format(
                        "%s0 €",
                        food.getPrice()
                ).replace(".", ",")
        );
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), FoodDetails.class);
                i.putExtra("food", foodList.get(position));
                v.getContext().startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView foodName;
        TextView foodPrice;
        TextView categoryName;
        TextView categoryDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.food_name);
            foodPrice = itemView.findViewById(R.id.food_price);
            categoryName = itemView.findViewById(R.id.categoryName);
            categoryDescription = itemView.findViewById(R.id.categoryDescription);
        }
    }
}
