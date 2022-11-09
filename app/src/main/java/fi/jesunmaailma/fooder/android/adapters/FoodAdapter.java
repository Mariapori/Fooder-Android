package fi.jesunmaailma.fooder.android.adapters;

import android.content.Intent;
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

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> implements Filterable {
    List<Food> foodList;
    List<Food> foodListFull;
    View view;

    Food food;

    public FoodAdapter(List<Food> foodList) {
        this.foodList = foodList;
        foodListFull = new ArrayList<>(foodList);
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

        if(food.getListPos() == 0){
            holder.categoryName.setEnabled(true);
            holder.categoryName.setText(food.getCategory());
        }else{
            holder.categoryName.setText("");
            holder.categoryName.setEnabled(false);
        }
        holder.foodName.setText(food.getName());
        holder.foodPrice.setText(
            String.format(
                "%s0 €",
                food.getPrice()
            )
        )
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

    @Override
    public Filter getFilter() {
        return foodFilter;
    }

    private Filter foodFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Food> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(foodListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Food food : foodListFull) {
                    if (food.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(food);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            foodList.clear();
            foodList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView foodName;
        TextView foodPrice;
        TextView categoryName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.food_name);
            foodPrice = itemView.findViewById(R.id.food_price);
            categoryName = itemView.findViewById(R.id.categoryName);
        }
    }
}
