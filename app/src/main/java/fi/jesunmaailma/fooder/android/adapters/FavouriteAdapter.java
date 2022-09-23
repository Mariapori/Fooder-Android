package fi.jesunmaailma.fooder.android.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fi.jesunmaailma.fooder.android.R;
import fi.jesunmaailma.fooder.android.models.Favourite;
import fi.jesunmaailma.fooder.android.models.Restaurant;
import fi.jesunmaailma.fooder.android.services.FooderDataService;
import fi.jesunmaailma.fooder.android.ui.activities.MainActivity;
import fi.jesunmaailma.fooder.android.ui.activities.RestaurantPage;

public class FavouriteAdapter extends RecyclerView.Adapter<FavouriteAdapter.ViewHolder> {
    // VAIN virheenjäljitystä varten.
    //public static final String RESTAURANT_DATA_TAG = "TAG";
    List<Favourite> favouriteList;
    Restaurant restaurant;
    View view;

    public FavouriteAdapter(List<Favourite> favouriteList) {
        this.favouriteList = favouriteList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.favourite_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Favourite favourite = favouriteList.get(position);
        holder.restaurantName.setText(favourite.getRestaurantName());
        holder.restaurantCity.setText(favourite.getRestaurantCity());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), RestaurantPage.class);
                Favourite fav = favouriteList.get(position);
                for (int j = 0; j < MainActivity.restaurantList.size(); j++) {
                    Restaurant res = MainActivity.restaurantList.get(j);
                    if (res.getId() == fav.getRestaurantId()) {
                        restaurant = MainActivity.restaurantList.get(j);
                        i.putExtra("isFavorite", true);
                        break;
                    }
                }
                i.putExtra("restaurant", restaurant);
                v.getContext().startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favouriteList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView restaurantName;
        TextView restaurantCity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            restaurantName = itemView.findViewById(R.id.txtRavintolanimi);
            restaurantCity = itemView.findViewById(R.id.txtKaupunki);
        }
    }
}
