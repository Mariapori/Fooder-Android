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
import fi.jesunmaailma.fooder.android.models.Favourite;
import fi.jesunmaailma.fooder.android.models.Restaurant;
import fi.jesunmaailma.fooder.android.services.FooderDataService;
import fi.jesunmaailma.fooder.android.ui.activities.MainActivity;
import fi.jesunmaailma.fooder.android.ui.activities.RestaurantPage;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> implements Filterable {
    public static final String RESTAURANT_DATA_TAG = "TAG";
    List<Restaurant> restaurantList;
    List<Restaurant> restaurantListFull;
    List<Favourite> favouriteList;
    View view;

    public RestaurantAdapter(List<Restaurant> restaurantList, List<Favourite> favouriteList) {
        this.restaurantList = restaurantList;
        this.favouriteList = favouriteList;
        restaurantListFull = new ArrayList<>(restaurantList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.restaurant_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);

        holder.restaurantName.setText(restaurant.getName());
        holder.restaurantCity.setText(restaurant.getCity());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), RestaurantPage.class);
                i.putExtra("restaurant", restaurantList.get(position));
                i.putExtra("isFavorite",isFavorite(restaurant));

                v.getContext().startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    @Override
    public Filter getFilter() {
        return restaurantFilter;
    }

    private Filter restaurantFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Restaurant> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(restaurantListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Restaurant restaurant : restaurantListFull) {
                    if (restaurant.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(restaurant);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            restaurantList.clear();
            restaurantList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView restaurantName;
        TextView restaurantCity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            restaurantName = itemView.findViewById(R.id.restaurant_name);
            restaurantCity = itemView.findViewById(R.id.restaurant_city);
        }
    }

    public boolean isFavorite(Restaurant restaurant){
        for (int i = 0; i < favouriteList.size(); i++) {
            Favourite fav = favouriteList.get(i);
            if(fav.getRestaurantId() == restaurant.getId()){
                return true;
            }
        }
        return false;
    }
}
